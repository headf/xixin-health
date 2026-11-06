package com.xixin.health.report;

import com.xixin.health.exam.ExamRecord;
import com.xixin.health.exam.ExamRecordRepository;
import com.xixin.health.result.CheckResult;
import com.xixin.health.result.CheckResultRepository;
import com.xixin.health.result.AbnormalLevel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TrendAnalysisService {

    private static final String AI_API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String API_KEY = System.getenv().getOrDefault("DEEPSEEK_API_KEY", "your-deepseek-api-key");

    // 趋势分析缓存：userId → { data, cachedAt }
    private final Map<Long, CacheEntry> analysisCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 3600_000; // 1 hour

    private record CacheEntry(Map<String, Object> data, long cachedAt) {
        boolean isExpired() { return System.currentTimeMillis() - cachedAt > CACHE_TTL_MS; }
    }

    private final ExamRecordRepository examRecordRepository;
    private final CheckResultRepository checkResultRepository;

    public TrendAnalysisService(ExamRecordRepository examRecordRepository,
                                 CheckResultRepository checkResultRepository) {
        this.examRecordRepository = examRecordRepository;
        this.checkResultRepository = checkResultRepository;
    }

    public Map<String, Object> analyzeTrend(Long userId) {
        // Check cache first
        CacheEntry cached = analysisCache.get(userId);
        if (cached != null && !cached.isExpired()) {
            return cached.data();
        }
        // Compute and cache
        Map<String, Object> result = computeAnalysis(userId);
        analysisCache.put(userId, new CacheEntry(result, System.currentTimeMillis()));
        return result;
    }

    /** 预计算并缓存（供 ReportService 在生成报告后调用） */
    public void preCacheAnalysis(Long userId) {
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> result = computeAnalysis(userId);
                analysisCache.put(userId, new CacheEntry(result, System.currentTimeMillis()));
            } catch (Exception e) {
                System.out.println("Pre-cache failed for user " + userId + ": " + e.getMessage());
            }
        });
    }

    private Map<String, Object> computeAnalysis(Long userId) {
        List<ExamRecord> allRecords = examRecordRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (allRecords.isEmpty()) {
            return Map.of("status", "NO_DATA", "message", "暂无体检记录");
        }

        List<ExamRecord> validRecords = new ArrayList<>();
        List<List<CheckResult>> resultsList = new ArrayList<>();
        // 遍历全部记录，按有数值型数据的取最近3份
        for (ExamRecord rec : allRecords) {
            if (validRecords.size() >= 3) break;
            List<CheckResult> results = checkResultRepository.findByExamRecordId(rec.getId());
            // 只选取有数值型检查结果的记录（非数值型如影像描述无法做趋势对比）
            boolean hasNumeric = results.stream().anyMatch(r -> r.getNumericValue() != null);
            if (hasNumeric) {
                validRecords.add(rec);
                resultsList.add(results);
            }
        }

        if (validRecords.size() < 2) {
            return Map.of(
                "status", "INSUFFICIENT_DATA",
                "message", "需要至少2次体检记录才能进行对比分析（目前仅1次）",
                "recordCount", validRecords.size()
            );
        }

        Map<String, Object> result = ruleBasedAnalysis(validRecords, resultsList);
        result.put("aiEnhanced", false);
        result.put("message", "基于规则引擎的快速分析结果");

        // 添加风险等级和免责声明
        List<CheckResult> allFlatResults = resultsList.stream().flatMap(List::stream).toList();
        result.put("healthRiskLevel", computeRiskLevel(allFlatResults));
        result.put("disclaimer", "⚠️ 本报告由系统自动生成，AI解读与健康风险评估仅供参考，不构成医疗诊断建议。如有身体不适或指标异常，请及时前往正规医疗机构就诊。");

        return result;
    }

    /** 简单风险等级计算 */
    private String computeRiskLevel(List<CheckResult> results) {
        if (results == null || results.isEmpty()) return "UNKNOWN";
        long critical = results.stream().filter(r -> r.getAbnormalLevel() == AbnormalLevel.CRITICAL).count();
        long abnormal = results.stream().filter(r -> r.getAbnormalLevel() == AbnormalLevel.ABNORMAL).count();
        long total = results.size();
        if (critical >= 1) return "HIGH";
        if (total > 0 && (double) abnormal / total >= 0.3) return "HIGH";
        if (abnormal >= 5) return "HIGH";
        if (abnormal >= 2) return "MEDIUM";
        if (total > 0 && (double) abnormal / total >= 0.1) return "MEDIUM";
        return "LOW";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callAiApi(List<ExamRecord> records, List<List<CheckResult>> resultsList) {
        try {
            // Build prompt
            StringBuilder prompt = new StringBuilder();

            List<String> dates = new ArrayList<>();
            for (int i = 0; i < records.size(); i++) {
                ExamRecord rec = records.get(i);
                String date = rec.getCreatedAt() != null
                    ? rec.getCreatedAt().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                    : "未知日期";
                dates.add(date);
            }

            prompt.append("请分析以下体检数据的变化趋势，用中文返回JSON格式结果。\n\n");
            prompt.append("注意：这些数据跨越 ").append(records.size()).append(" 次体检，")
                  .append("时间范围从 ").append(dates.get(dates.size() - 1))
                  .append(" 到 ").append(dates.get(0))
                  .append("。请重点分析长期趋势（3~4年）的变化规律。\n\n");

            for (int i = 0; i < records.size(); i++) {
                ExamRecord rec = records.get(i);
                String date = dates.get(i);
                prompt.append("【").append(date).append("】体检结果：\n");
                for (CheckResult r : resultsList.get(i)) {
                    if (r.getNumericValue() != null) {
                        prompt.append("  - ").append(r.getItemName()).append(": ")
                            .append(r.getNumericValue()).append(" ")
                            .append(r.getUnit() != null ? r.getUnit() : "")
                            .append(" (参考范围: ")
                            .append(r.getReferenceMin()).append(" ~ ").append(r.getReferenceMax()).append(")")
                            .append(" 状态: ").append(r.getAbnormalLevel().name())
                            .append("\n");
                    }
                }
                prompt.append("\n");
            }

            prompt.append("请以JSON格式返回分析结果:\n");
            prompt.append("重要要求：\n");
            prompt.append("1. 健康建议（healthAdvice）必须严格基于上述实际异常指标生成，不得给出无关泛化建议\n");
            prompt.append("2. 每条建议需明确标注针对的具体指标名称\n");
            prompt.append("3. 如果所有指标正常，healthAdvice应仅为维持性建议\n\n");
            prompt.append("{\n");
            prompt.append("  \"overallAssessment\": \"总体趋势评估文字\",\n");
            prompt.append("  \"healthAdvice\": \"基于数据的个性化健康建议（3-5条具体建议）\",\n");
            prompt.append("  \"overallIcon\": \"🔴或🟢或📊\",\n");
            prompt.append("  \"improvingCount\": 改善指标数量,\n");
            prompt.append("  \"worseningCount\": 恶化指标数量,\n");
            prompt.append("  \"stableCount\": 稳定指标数量,\n");
            prompt.append("  \"itemAnalyses\": [\n");
            prompt.append("    {\n");
            prompt.append("      \"itemName\": \"指标名称\",\n");
            prompt.append("      \"trend\": \"上升/下降/稳定\",\n");
            prompt.append("      \"trendIcon\": \"📈/📉/➡️\",\n");
            prompt.append("      \"changePct\": 变化百分比,\n");
            prompt.append("      \"analysis\": \"分析说明文字\",\n");
            prompt.append("      \"statusChange\": \"新发异常/已恢复正常/持续异常/正常\",\n");
            prompt.append("      \"isWorsening\": false,\n");
            prompt.append("      \"isImproving\": true,\n");
            prompt.append("      \"currentValue\": 当前值,\n");
            prompt.append("      \"previousValue\": 上次值,\n");
            prompt.append("      \"unit\": \"单位\"\n");
            prompt.append("    }\n");
            prompt.append("  ]\n");
            prompt.append("}\n");
            prompt.append("注意：分析要专业、准确，基于实际数据变化。");

            // Build request body
            String requestBody = "{"
                + "\"model\":\"deepseek-chat\","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"你是专业的健康体检数据分析师，擅长分析体检指标变化趋势。\"},"
                + "{\"role\":\"user\",\"content\":\"" + escapeJson(prompt.toString()) + "\"}"
                + "],"
                + "\"temperature\":0.3,"
                + "\"max_tokens\":2000"
                + "}";

            java.net.URL url = new java.net.URL(AI_API_URL);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(8000);

            try (java.io.OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.out.println("AI API returned: " + responseCode);
                return null;
            }

            StringBuilder response = new StringBuilder();
            try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), "utf-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
            }

            // Parse the response
            String jsonStr = response.toString();
            // Extract content from OpenAI-compatible response
            String content = extractContentFromResponse(jsonStr);
            if (content == null) return null;

            // Parse the JSON content
            return parseAiResponse(content, dates, resultsList);

        } catch (Exception e) {
            System.out.println("AI API error: " + e.getMessage());
            return null;
        }
    }

    private String extractContentFromResponse(String jsonResponse) {
        try {
            // Parse the API response to get the content
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
            java.util.List<Map<String, Object>> choices = (java.util.List<Map<String, Object>>) responseMap.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                if (message != null) {
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            System.out.println("Parse error: " + e.getMessage());
        }
        return null;
    }

    private Map<String, Object> parseAiResponse(String content, List<String> dates,
                                                  List<List<CheckResult>> resultsList) {
        try {
            // Extract JSON from markdown code blocks if present
            if (content.contains("```json")) {
                content = content.substring(content.indexOf("```json") + 7);
                content = content.substring(0, content.indexOf("```"));
            } else if (content.contains("```")) {
                content = content.substring(content.indexOf("```") + 3);
                content = content.substring(0, content.indexOf("```"));
            }
            content = content.trim();

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> parsed = mapper.readValue(content, new TypeReference<Map<String, Object>>() {});
            if (parsed == null) return null;

            // Fill in missing fields
            parsed.putIfAbsent("status", "SUCCESS");
            parsed.putIfAbsent("recordCount", resultsList.size());
            parsed.putIfAbsent("dates", dates);

            // Ensure itemAnalyses has needed fields
            java.util.List<Map<String, Object>> items = (java.util.List<Map<String, Object>>) parsed.get("itemAnalyses");
            if (items != null) {
                int idx = 0;
                for (Map<String, Object> item : items) {
                    // Fill in current/previous values if available
                    for (List<CheckResult> rl : resultsList) {
                        for (CheckResult r : rl) {
                            if (r.getItemName().equals(item.get("itemName"))) {
                                item.putIfAbsent("currentValue", r.getNumericValue().doubleValue());
                                item.putIfAbsent("unit", r.getUnit());
                                break;
                            }
                        }
                    }
                    idx++;
                }
            }

            return parsed;
        } catch (Exception e) {
            System.out.println("Failed to parse AI response: " + e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> ruleBasedAnalysis(List<ExamRecord> validRecords,
                                                    List<List<CheckResult>> resultsList) {
        // Build item-wise trend
        Map<Long, List<CheckResult>> itemResults = new LinkedHashMap<>();
        List<String> dates = new ArrayList<>();
        for (int i = validRecords.size() - 1; i >= 0; i--) {
            ExamRecord rec = validRecords.get(i);
            String date = rec.getCreatedAt() != null
                ? rec.getCreatedAt().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                : "未知日期";
            dates.add(date);
            for (CheckResult r : resultsList.get(i)) {
                if (r.getNumericValue() == null) continue;
                itemResults.computeIfAbsent(r.getItemId(), k -> new ArrayList<>()).add(r);
            }
        }

        List<Map<String, Object>> itemAnalyses = new ArrayList<>();
        int worseningCount = 0, improvingCount = 0, stableCount = 0;

        // 计算时间跨度
        String timeSpanStr = dates.size() >= 2
            ? dates.get(0) + " ~ " + dates.get(dates.size() - 1)
            : dates.get(0);

        for (Map.Entry<Long, List<CheckResult>> entry : itemResults.entrySet()) {
            List<CheckResult> vals = entry.getValue();
            if (vals.size() < 2) continue;

            CheckResult latest = vals.get(vals.size() - 1);
            CheckResult first = vals.get(0);  // 最早的记录（多年对比基准）
            CheckResult previous = vals.size() >= 2 ? vals.get(vals.size() - 2) : latest;

            double currentVal = latest.getNumericValue().doubleValue();
            double overallPrevVal = first.getNumericValue().doubleValue();  // 多年总体变化基准
            double shortPrevVal = previous.getNumericValue().doubleValue(); // 短期变化基准
            double refMin = latest.getReferenceMin() != null ? latest.getReferenceMin().doubleValue() : Double.NaN;
            double refMax = latest.getReferenceMax() != null ? latest.getReferenceMax().doubleValue() : Double.NaN;

            // 多年总体变化率（从最早到最新）
            double overallChangePct = overallPrevVal != 0 ? ((currentVal - overallPrevVal) / overallPrevVal) * 100 : 0;
            // 短期变化率（上次到最新）
            double shortChangePct = shortPrevVal != 0 ? ((currentVal - shortPrevVal) / shortPrevVal) * 100 : 0;

            String trend, trendIcon;
            boolean isWorsening = false, isImproving = false;

            // 以多年总体趋势为主要判定依据
            if (Math.abs(overallChangePct) < 5) {
                trend = "长期稳定"; trendIcon = "➡️"; stableCount++;
            } else if (overallChangePct > 0) {
                boolean inRange = (Double.isNaN(refMax) || currentVal <= refMax);
                if (inRange) { trend = "长期略升"; trendIcon = "📈"; isImproving = true; }
                else { trend = "长期上升"; trendIcon = "🔴"; isWorsening = true; }
            } else {
                boolean inRange = (Double.isNaN(refMin) || currentVal >= refMin);
                if (inRange) { trend = "长期略降"; trendIcon = "📉"; isImproving = true; }
                else { trend = "长期下降"; trendIcon = "🟢"; isWorsening = true; }
            }

            if (isWorsening) worseningCount++;
            else if (isImproving) improvingCount++;
            else stableCount++;

            // 分析说明包含多年总体变化 + 近期变化
            String analysis;
            if (vals.size() >= 3) {
                analysis = String.format("【多年趋势】%s 从 %.1f → %.1f %s（%s），总体变化 %.1f%%。",
                    latest.getItemName(), overallPrevVal, currentVal,
                    latest.getUnit() != null ? latest.getUnit() : "", timeSpanStr, overallChangePct);
                if (Math.abs(shortChangePct) >= 3) {
                    analysis += String.format(" 近期（上次 %.1f 对比）变化 %.1f%%。",
                        shortPrevVal, shortChangePct);
                }
            } else {
                analysis = String.format("%s 从 %.1f 变为 %.1f %s，变化 %.1f%%",
                    latest.getItemName(), overallPrevVal, currentVal,
                    latest.getUnit() != null ? latest.getUnit() : "", overallChangePct);
            }

            String statusChange = "";
            if (vals.size() >= 2) {
                boolean wasAbnormal = first.getAbnormalLevel() != com.xixin.health.result.AbnormalLevel.NORMAL;
                boolean nowAbnormal = latest.getAbnormalLevel() != com.xixin.health.result.AbnormalLevel.NORMAL;
                if (!wasAbnormal && nowAbnormal) statusChange = "新发异常";
                else if (wasAbnormal && !nowAbnormal) statusChange = "已恢复正常";
                else if (wasAbnormal && nowAbnormal) statusChange = "持续异常";
                else statusChange = "长期正常";
            }

            Map<String, Object> itemAnalysis = new LinkedHashMap<>();
            itemAnalysis.put("itemName", latest.getItemName());
            itemAnalysis.put("unit", latest.getUnit());
            itemAnalysis.put("currentValue", currentVal);
            itemAnalysis.put("previousValue", overallPrevVal);
            itemAnalysis.put("changePct", Math.round(overallChangePct * 10) / 10.0);
            itemAnalysis.put("trend", trend);
            itemAnalysis.put("trendIcon", trendIcon);
            itemAnalysis.put("analysis", analysis);
            itemAnalysis.put("statusChange", statusChange);
            itemAnalysis.put("isWorsening", isWorsening);
            itemAnalysis.put("isImproving", isImproving);
            itemAnalyses.add(itemAnalysis);
        }

        String overallAssessment, overallIcon;
        if (worseningCount > improvingCount && worseningCount >= 2) {
            overallAssessment = String.format("【%s】有 %d 项指标在恶化，%d 项改善。建议关注异常指标。", timeSpanStr, worseningCount, improvingCount);
            overallIcon = "🔴";
        } else if (improvingCount > worseningCount) {
            overallAssessment = String.format("【%s】有 %d 项指标在改善，%d 项恶化。整体趋势向好。", timeSpanStr, improvingCount, worseningCount);
            overallIcon = "🟢";
        } else {
            overallAssessment = String.format("【%s】%d 项稳定，%d 项改善，%d 项恶化。整体健康平稳。", timeSpanStr, stableCount, improvingCount, worseningCount);
            overallIcon = "📊";
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "SUCCESS");
        result.put("recordCount", validRecords.size());
        result.put("dates", dates);
        result.put("itemAnalyses", itemAnalyses);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("overallAssessment", overallAssessment);
        String healthAdvice;
        if (worseningCount > improvingCount && worseningCount >= 2) {
            healthAdvice = String.format("基于 %s 的趋势分析，建议及时就医并调整生活方式。1. 饮食：减少高脂高糖，增加蔬果；2. 运动：每周至少150分钟有氧运动；3. 作息：保证充足睡眠；4. 定期复查异常指标。", timeSpanStr);
        } else if (improvingCount > worseningCount) {
            healthAdvice = String.format("基于 %s 的趋势分析，整体趋势向好！继续保持良好的生活习惯：1. 坚持目前的运动和饮食计划；2. 保持规律作息；3. 定期体检；4. 保持积极乐观的心态。", timeSpanStr);
        } else {
            healthAdvice = String.format("基于 %s 的趋势分析，各项指标基本稳定。建议保持均衡饮食和适量运动，每年进行一次常规体检，保持良好的作息习惯。", timeSpanStr);
        }
        summary.put("healthAdvice", healthAdvice);
        summary.put("overallIcon", overallIcon);
        summary.put("improvingCount", improvingCount);
        summary.put("worseningCount", worseningCount);
        summary.put("stableCount", stableCount);
        summary.put("totalItems", itemAnalyses.size());
        result.put("summary", summary);
        return result;
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
