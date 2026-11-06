package com.xixin.health.report;

import com.xixin.health.result.AbnormalLevel;
import com.xixin.health.result.CheckResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 健康风险评估与分析服务
 *
 * 基于全量体检数据：
 * 1. 计算健康风险等级（低/中/高）
 * 2. 生成与异常项匹配的个性化健康建议（饮食/运动/复查）
 * 3. 生成每项异常指标的风险提示
 * 4. 标注高风险疾病预警
 * 5. 附免责声明
 */
@Service
public class ReportAnalysisService {

    private static final String DISCLAIMER =
        "⚠️ 本报告由系统自动生成，健康风险评估与AI解读仅供参考，不构成医疗诊断建议。"
        + "如有身体不适或指标异常，请及时前往正规医疗机构就诊。";

    /**
     * 计算健康风险等级
     * LOW  — 所有指标正常或仅有轻微异常
     * MEDIUM — 存在多项异常或单项中度异常
     * HIGH  — 存在危急值或多项重度异常
     */
    public String computeHealthRiskLevel(List<CheckResult> results) {
        if (results == null || results.isEmpty()) return "UNKNOWN";

        long criticalCount = results.stream()
            .filter(r -> r.getAbnormalLevel() == AbnormalLevel.CRITICAL).count();
        long abnormalCount = results.stream()
            .filter(r -> r.getAbnormalLevel() == AbnormalLevel.ABNORMAL).count();
        long totalItems = results.size();
        long normalCount = totalItems - criticalCount - abnormalCount;

        // 危急值 → 高风险
        if (criticalCount >= 1) return "HIGH";
        // 异常项占比 ≥ 30% 或 异常项 ≥ 5 项 → 高风险
        if (totalItems > 0 && (double) abnormalCount / totalItems >= 0.3) return "HIGH";
        if (abnormalCount >= 5) return "HIGH";

        // 异常项 ≥ 2 项 或 异常占比 ≥ 10% → 中风险
        if (abnormalCount >= 2) return "MEDIUM";
        if (totalItems > 0 && (double) abnormalCount / totalItems >= 0.1) return "MEDIUM";

        // 异常项 ≤ 1 且占比 < 10% → 低风险
        return "LOW";
    }

    /**
     * 生成健康风险评估文字
     */
    public String generateRiskAssessment(List<CheckResult> results, String riskLevel) {
        if (results == null || results.isEmpty()) return "暂无评估数据。";

        long criticalCount = results.stream()
            .filter(r -> r.getAbnormalLevel() == AbnormalLevel.CRITICAL).count();
        long abnormalCount = results.stream()
            .filter(r -> r.getAbnormalLevel() == AbnormalLevel.ABNORMAL).count();
        long totalItems = results.size();
        long normalCount = totalItems - criticalCount - abnormalCount;

        List<String> abnormalItemNames = results.stream()
            .filter(r -> r.getAbnormalLevel() != AbnormalLevel.NORMAL)
            .map(CheckResult::getItemName)
            .filter(Objects::nonNull)
            .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("本次体检共检查 ").append(totalItems).append(" 项，");

        if ("HIGH".equals(riskLevel)) {
            if (criticalCount > 0) {
                sb.append("其中发现 ").append(criticalCount).append(" 项危急值、")
                  .append(abnormalCount).append(" 项异常指标。");
                sb.append("⚠️ 存在危急值指标，建议立即就医复查。");
            } else {
                sb.append("其中 ").append(abnormalCount).append(" 项指标异常，异常占比较高。");
                sb.append("建议尽快到专科门诊进行系统检查。");
            }
        } else if ("MEDIUM".equals(riskLevel)) {
            sb.append("其中 ").append(abnormalCount).append(" 项指标异常，建议关注并定期复查。");
            if (!abnormalItemNames.isEmpty()) {
                sb.append("需重点关注：").append(String.join("、", abnormalItemNames)).append("。");
            }
        } else {
            sb.append("所有").append(normalCount > 0 ? " " + normalCount + " 项" : "").append("指标基本正常");
            if (abnormalCount > 0) {
                sb.append("，仅 ").append(abnormalCount).append(" 项轻微异常，无需过度担心");
            }
            sb.append("。请保持良好生活习惯。");
        }

        return sb.toString();
    }

    /**
     * 生成按科室/类别分组的疾病预警
     */
    public List<Map<String, Object>> generateDiseaseWarnings(List<CheckResult> results,
                                                               Map<Long, String> deptNameMap) {
        if (results == null || results.isEmpty()) return List.of();

        // 按是否有异常分组统计
        Map<Long, List<CheckResult>> abnormalByDept = new LinkedHashMap<>();
        for (CheckResult r : results) {
            if (r.getAbnormalLevel() != AbnormalLevel.NORMAL) {
                abnormalByDept.computeIfAbsent(r.getItemId(), k -> new ArrayList<>()).add(r);
            }
        }

        // 基于异常指标的疾病预警规则
        List<Map<String, Object>> warnings = new ArrayList<>();
        Set<String> seenWarnings = new HashSet<>();

        for (CheckResult r : results) {
            if (r.getAbnormalLevel() == AbnormalLevel.NORMAL || r.getItemName() == null) continue;

            String warning = mapItemToWarning(r);
            if (warning != null && seenWarnings.add(warning)) {
                Map<String, Object> w = new LinkedHashMap<>();
                w.put("itemName", r.getItemName());
                w.put("warning", warning);
                w.put("level", r.getAbnormalLevel().name());
                w.put("suggestion", mapWarningToSuggestion(warning));
                warnings.add(w);
            }
        }

        return warnings;
    }

    /**
     * 生成个性化健康建议（保证每条建议都对应实际异常项）
     */
    public Map<String, Object> generatePersonalizedRecommendations(List<CheckResult> results) {
        Map<String, Object> recommendations = new LinkedHashMap<>();

        List<CheckResult> abnormalItems = results.stream()
            .filter(r -> r.getAbnormalLevel() != AbnormalLevel.NORMAL)
            .toList();

        List<CheckResult> criticalItems = results.stream()
            .filter(r -> r.getAbnormalLevel() == AbnormalLevel.CRITICAL)
            .toList();

        // 饮食建议 — 基于具体异常项生成
        List<String> dietAdvice = new ArrayList<>();
        Set<String> dietTopics = new HashSet<>();
        for (CheckResult r : abnormalItems) {
            String itemName = r.getItemName();
            if (itemName == null) continue;

            if (itemName.contains("血糖") || itemName.contains("GLU")) {
                dietAdvice.add("🥗 血糖异常：建议低糖饮食，减少精制碳水化合物摄入（白米、白面），增加全谷物和膳食纤维。");
                dietTopics.add("血糖");
            }
            if (itemName.contains("谷丙转氨酶") || itemName.contains("ALT") || itemName.contains("谷草转氨酶") || itemName.contains("AST")) {
                dietAdvice.add("🥗 肝功能指标异常：建议戒酒，减少高脂油炸食物，增加优质蛋白和维生素摄入。");
                dietTopics.add("肝功能");
            }
            if (itemName.contains("总胆固醇") || itemName.contains("TC") || itemName.contains("甘油三酯") || itemName.contains("TG")
                || itemName.contains("低密度脂蛋白") || itemName.contains("LDL") || itemName.contains("高密度脂蛋白") || itemName.contains("HDL")) {
                dietAdvice.add("🥗 血脂异常：建议低脂低胆固醇饮食，减少动物内脏、肥肉摄入，增加深海鱼类和坚果。");
                dietTopics.add("血脂");
            }
            if (itemName.contains("肌酐") || itemName.contains("CR") || itemName.contains("尿素氮") || itemName.contains("BUN")) {
                dietAdvice.add("🥗 肾功能指标异常：建议低盐低蛋白饮食，控制豆制品和肉类摄入量，适量饮水。");
                dietTopics.add("肾功能");
            }
            if (itemName.contains("尿酸") || itemName.contains("UA")) {
                dietAdvice.add("🥗 尿酸偏高：建议低嘌呤饮食，避免动物内脏、海鲜、浓汤，限酒，多饮水促进尿酸排泄。");
                dietTopics.add("尿酸");
            }
            if (itemName.contains("白细胞") || itemName.contains("WBC") || itemName.contains("血常规")) {
                dietAdvice.add("🥗 血常规异常：建议均衡营养，增加富含铁、维生素B12和叶酸的食物（红肉、深绿色蔬菜）。");
                dietTopics.add("血常规");
            }
        }
        if (dietAdvice.isEmpty() && !abnormalItems.isEmpty()) {
            dietAdvice.add("🥗 建议均衡膳食，多摄入蔬菜水果，减少高盐高脂高糖食品。");
        } else if (abnormalItems.isEmpty()) {
            dietAdvice.add("🥗 各项指标正常，建议继续保持均衡饮食习惯，多摄入蔬菜水果和全谷物。");
        }
        recommendations.put("diet", dietAdvice);

        // 运动建议
        List<String> exerciseAdvice = new ArrayList<>();
        boolean hasMetabolic = dietTopics.contains("血糖") || dietTopics.contains("血脂") || dietTopics.contains("尿酸");
        if (hasMetabolic) {
            exerciseAdvice.add("🏃 每周至少进行 150 分钟中等强度有氧运动（快走、慢跑、游泳），有助于改善代谢指标。");
        }
        if (dietTopics.contains("肝功能")) {
            exerciseAdvice.add("🏃 适度运动有助于肝脏健康，建议每周运动 3-5 次，避免剧烈运动加重肝脏负担。");
        }
        if (!abnormalItems.isEmpty()) {
            exerciseAdvice.add("🏃 建议规律运动，每周 3-5 次，每次 30-45 分钟，避免久坐不动。");
        } else {
            exerciseAdvice.add("🏃 保持现有运动习惯，建议每周至少运动 3 次，维持健康体魄。");
        }
        recommendations.put("exercise", exerciseAdvice);

        // 复查建议
        List<String> followUpAdvice = new ArrayList<>();
        if (!criticalItems.isEmpty()) {
            for (CheckResult c : criticalItems) {
                followUpAdvice.add("🔴 " + (c.getItemName() != null ? c.getItemName() : "异常项")
                    + " 出现危急值，建议立即前往医院就诊复查！");
            }
        }
        for (CheckResult r : abnormalItems) {
            if (r.getAbnormalLevel() == AbnormalLevel.CRITICAL) continue;
            String itemName = r.getItemName();
            if (itemName == null) continue;

            if (itemName.contains("血糖") || itemName.contains("GLU")) {
                followUpAdvice.add("📋 血糖异常：建议 1-2 周内复查空腹血糖，必要时做糖耐量试验。");
            } else if (itemName.contains("谷丙转氨酶") || itemName.contains("ALT")) {
                followUpAdvice.add("📋 肝功能异常：建议 2-4 周内复查肝功能全套，必要时做肝脏B超。");
            } else if (itemName.contains("总胆固醇") || itemName.contains("TC") || itemName.contains("甘油三酯") || itemName.contains("TG")) {
                followUpAdvice.add("📋 血脂异常：建议 1-3 个月内复查血脂四项，评估是否需要药物治疗。");
            } else if (itemName.contains("肌酐") || itemName.contains("CR")) {
                followUpAdvice.add("📋 肾功能异常：建议 2-4 周内复查肾功能，同时做尿常规检查。");
            } else if (itemName.contains("尿酸") || itemName.contains("UA")) {
                followUpAdvice.add("📋 尿酸偏高：建议 1 个月后复查血尿酸，如有关节红肿热痛需及时就诊。");
            } else {
                followUpAdvice.add("📋 " + itemName + " 异常，建议 " + (r.getAbnormalLevel() == AbnormalLevel.ABNORMAL ? "1-3 个月后复查" : "及时就医复查") + "。");
            }
        }
        if (followUpAdvice.isEmpty()) {
            followUpAdvice.add("📋 本次体检各项指标基本正常，建议每年进行一次常规体检。");
        }
        recommendations.put("followUp", followUpAdvice);

        return recommendations;
    }

    /**
     * 生成每项异常指标的风险提示文字
     */
    public String generateItemRiskPrompt(CheckResult result) {
        if (result == null || result.getAbnormalLevel() == AbnormalLevel.NORMAL) return "";

        String itemName = result.getItemName();
        if (itemName == null) return "指标异常，建议复查。";

        StringBuilder prompt = new StringBuilder();

        if (result.getAbnormalLevel() == AbnormalLevel.CRITICAL) {
            prompt.append("🔴【危急值】");
        }

        if (itemName.contains("血糖") || itemName.contains("GLU")) {
            prompt.append(result.getNumericValue() != null && result.getNumericValue().compareTo(BigDecimal.valueOf(6.1)) > 0
                ? "血糖偏高，长期高血糖可能增加糖尿病及并发症风险。"
                : "血糖偏低，需注意预防低血糖发生。");
        } else if (itemName.contains("谷丙转氨酶") || itemName.contains("ALT")) {
            prompt.append("谷丙转氨酶升高提示肝细胞损伤风险，可能与脂肪肝、肝炎等因素有关。");
        } else if (itemName.contains("谷草转氨酶") || itemName.contains("AST")) {
            prompt.append("谷草转氨酶升高可能与肝脏或心肌损伤有关，建议结合临床进一步检查。");
        } else if (itemName.contains("总胆固醇") || itemName.contains("TC")) {
            prompt.append("总胆固醇偏高是动脉粥样硬化和心脑血管疾病的危险因素。");
        } else if (itemName.contains("甘油三酯") || itemName.contains("TG")) {
            prompt.append("甘油三酯偏高增加胰腺炎和心血管疾病风险。");
        } else if (itemName.contains("低密度脂蛋白") || itemName.contains("LDL")) {
            prompt.append("低密度脂蛋白胆固醇（坏胆固醇）偏高是心血管疾病的主要危险因素。");
        } else if (itemName.contains("高密度脂蛋白") || itemName.contains("HDL")) {
            prompt.append("高密度脂蛋白胆固醇（好胆固醇）偏低不利于心血管健康。");
        } else if (itemName.contains("肌酐") || itemName.contains("CR")) {
            prompt.append("肌酐升高提示肾功能可能受损，需排除肾前性或肾性因素。");
        } else if (itemName.contains("尿素氮") || itemName.contains("BUN")) {
            prompt.append("尿素氮升高可能与肾功能下降或高蛋白饮食有关。");
        } else if (itemName.contains("尿酸") || itemName.contains("UA")) {
            prompt.append("尿酸偏高增加痛风和肾结石风险。");
        } else if (itemName.contains("白细胞") || itemName.contains("WBC")) {
            prompt.append("白细胞计数异常提示可能存在感染或炎症。");
        } else if (itemName.contains("血红蛋白") || itemName.contains("HGB")) {
            prompt.append("血红蛋白异常提示可能存在贫血或血液系统问题。");
        } else if (itemName.contains("血小板") || itemName.contains("PLT")) {
            prompt.append("血小板计数异常可能涉及凝血功能问题。");
        } else {
            prompt.append("该指标异常，建议结合临床进一步检查。");
        }

        return prompt.toString();
    }

    public String getDisclaimer() {
        return DISCLAIMER;
    }

    // ========== 内部方法 ==========

    private String mapItemToWarning(CheckResult r) {
        String name = r.getItemName();
        if (name == null) return null;

        if (name.contains("血糖") || name.contains("GLU")) {
            return "血糖异常 — 糖尿病风险预警";
        }
        if (name.contains("谷丙转氨酶") || name.contains("ALT")
            || name.contains("谷草转氨酶") || name.contains("AST")) {
            return "肝功能异常 — 脂肪肝/肝炎风险预警";
        }
        if (name.contains("总胆固醇") || name.contains("TC")
            || name.contains("甘油三酯") || name.contains("TG")
            || name.contains("低密度脂蛋白") || name.contains("LDL")) {
            return "血脂异常 — 动脉粥样硬化/心血管疾病风险预警";
        }
        if (name.contains("高密度脂蛋白") || name.contains("HDL")) {
            return "高密度脂蛋白偏低 — 心血管保护不足风险预警";
        }
        if (name.contains("肌酐") || name.contains("CR")
            || name.contains("尿素氮") || name.contains("BUN")) {
            return "肾功能异常 — 慢性肾病风险预警";
        }
        if (name.contains("尿酸") || name.contains("UA")) {
            return "高尿酸血症 — 痛风/肾结石风险预警";
        }
        if (name.contains("白细胞") || name.contains("WBC")) {
            return "白细胞异常 — 感染/炎症风险预警";
        }
        if (name.contains("血红蛋白") || name.contains("HGB")) {
            return "血红蛋白异常 — 贫血/血液系统风险预警";
        }
        return null;
    }

    private String mapWarningToSuggestion(String warning) {
        if (warning == null) return "建议及时就医复查。";
        if (warning.contains("糖尿病")) return "建议内分泌科就诊，做糖耐量试验和糖化血红蛋白检查。";
        if (warning.contains("肝")) return "建议消化内科或肝病科就诊，做肝功能全套和腹部B超。";
        if (warning.contains("心血管") || warning.contains("动脉")) return "建议心血管内科就诊，评估心血管风险。";
        if (warning.contains("肾病")) return "建议肾内科就诊，做肾功能和尿常规检查。";
        if (warning.contains("痛风") || warning.contains("结石")) return "建议风湿免疫科或肾内科就诊。";
        if (warning.contains("感染") || warning.contains("炎症")) return "建议内科就诊，查找感染源。";
        if (warning.contains("贫血") || warning.contains("血液")) return "建议血液科就诊，做血常规和铁蛋白检查。";
        return "建议到医院相关科室就诊复查。";
    }
}
