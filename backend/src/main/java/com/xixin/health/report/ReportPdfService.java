package com.xixin.health.report;

import com.xixin.health.exam.ExamRecord;
import com.xixin.health.exam.ExamRecordRepository;
import com.xixin.health.iam.UserAccount;
import com.xixin.health.iam.UserAccountRepository;
import com.xixin.health.result.AbnormalLevel;
import com.xixin.health.result.CheckResult;
import com.xixin.health.result.CheckResultRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 体检报告 PDF 生成服务
 * 使用 Apache PDFBox 生成带中文的正式 PDF 报告
 */
@Service
public class ReportPdfService {

    // 页面尺寸：A4
    private static final float PAGE_W = PDRectangle.A4.getWidth();
    private static final float PAGE_H = PDRectangle.A4.getHeight();
    private static final float MARGIN = 50;
    private static final float CONTENT_W = PAGE_W - 2 * MARGIN;
    private static final float LEADING = 18f;

    private final ExamReportRepository examReportRepository;
    private final CheckResultRepository checkResultRepository;
    private final ExamRecordRepository examRecordRepository;
    private final UserAccountRepository userAccountRepository;

    private PDType0Font fontNormal;

    public ReportPdfService(ExamReportRepository examReportRepository,
                            CheckResultRepository checkResultRepository,
                            ExamRecordRepository examRecordRepository,
                            UserAccountRepository userAccountRepository) {
        this.examReportRepository = examReportRepository;
        this.checkResultRepository = checkResultRepository;
        this.examRecordRepository = examRecordRepository;
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * 生成体检报告 PDF
     */
    public byte[] generatePdf(Long reportId) throws IOException {
        ExamReport report = examReportRepository.findById(reportId)
                .orElseThrow(() -> new NoSuchElementException("报告不存在: " + reportId));
        List<CheckResult> allResults = checkResultRepository.findByExamRecordId(report.getExamRecordId());

        try (PDDocument doc = new PDDocument()) {
            loadChineseFont(doc);
            Map<String, Object> detail = buildDetailData(report, allResults);

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float y = PAGE_H - MARGIN;

            y = drawHeader(cs, y, detail);
            y = drawStatusBanner(cs, y, detail);
            y = drawSection(cs, y, "个人信息", false);
            y = drawUserInfo(cs, y, detail);
            y = drawSection(cs, y, "检查结果摘要", false);
            y = drawStats(cs, y, detail);

            if (detail.containsKey("healthRiskLevel") && detail.get("healthRiskLevel") != null) {
                y = drawSection(cs, y, "AI 健康风险评估", true);
                y = drawHealthRisk(cs, y, detail);
            }

            y = drawSection(cs, y, "检查项目明细", false);
            y = drawResults(cs, y, detail);

            if (detail.get("summary") != null && !((String) detail.get("summary")).isEmpty()) {
                y = drawSection(cs, y, "诊断总结", false);
                y = drawTextBlock(cs, y, (String) detail.get("summary"));
            }

            if (detail.get("doctorAdvice") != null && !((String) detail.get("doctorAdvice")).isEmpty()) {
                y = drawSection(cs, y, "医生建议", true);
                y = drawTextBlock(cs, y, (String) detail.get("doctorAdvice"));
            }

            if (detail.containsKey("personalizedRecommendations") && detail.get("personalizedRecommendations") != null) {
                y = drawSection(cs, y, "AI 个性化健康建议", true);
                y = drawRecommendations(cs, y, detail);
            }

            y = drawDisclaimer(cs, y, detail);

            cs.close();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    // ==================== 字体加载 ====================

    private void loadChineseFont(PDDocument doc) {
        fontNormal = null;
        List<File> fontFiles = new ArrayList<>();
        fontFiles.add(new File("C:/Windows/Fonts/simhei.ttf"));
        fontFiles.add(new File("C:/Windows/Fonts/simkai.ttf"));
        fontFiles.add(new File("C:/Windows/Fonts/simfang.ttf"));
        fontFiles.add(new File("C:/Windows/Fonts/simsunb.ttf"));
        fontFiles.add(new File("/usr/share/fonts/truetype/wqy/wqy-microhei.ttf"));
        fontFiles.add(new File("/System/Library/Fonts/STHeiti Light.ttf"));

        for (File fontFile : fontFiles) {
            if (!fontFile.exists() || !fontFile.isFile()) continue;
            try (InputStream is = new java.io.FileInputStream(fontFile)) {
                fontNormal = PDType0Font.load(doc, is);
                if (fontNormal != null) return;
            } catch (Exception ignored) {}
        }
        throw new RuntimeException("找不到中文字体，请确保系统包含 simhei.ttf");
    }

    // ==================== 数据构建 ====================

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildDetailData(ExamReport report, List<CheckResult> results) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reportNo", report.getReportNo());
        String generatedAt = report.getGeneratedAt() != null ? report.getGeneratedAt().toString() : "";
        data.put("generatedAt", generatedAt);
        data.put("abnormalStatus", report.getAbnormalStatus().name());
        data.put("summary", report.getSummary());
        data.put("doctorAdvice", report.getDoctorAdvice());
        data.put("disclaimer", "本报告仅供健康参考，不作为临床诊断依据");

        // 统计
        long total = results.size();
        long abnormal = results.stream().filter(r -> r.getAbnormalLevel() == AbnormalLevel.ABNORMAL).count();
        long critical = results.stream().filter(r -> r.getAbnormalLevel() == AbnormalLevel.CRITICAL).count();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalItems", total);
        stats.put("abnormalCount", abnormal);
        stats.put("criticalCount", critical);
        data.put("stats", stats);

        // ===== 用户信息 =====
        String userName = "-", userGender = "-", userPhone = "-", userIdCard = "-";
        int userAge = 0;
        String packageName = "-";

        ExamRecord examRecord = examRecordRepository.findById(report.getExamRecordId()).orElse(null);
        if (examRecord != null) {
            packageName = examRecord.getPackageName() != null ? examRecord.getPackageName() : "-";
            UserAccount user = userAccountRepository.findById(examRecord.getUserId()).orElse(null);
            if (user != null) {
                userName = user.getDisplayName() != null ? user.getDisplayName() : "-";
                userPhone = user.getPhone() != null ? user.getPhone() : "-";
                userIdCard = user.getIdCard() != null ? user.getIdCard() : "-";
                // 性别优先从数据库 gender 字段取
                String rawGender = user.getGender();
                if (rawGender != null && !rawGender.isEmpty()) {
                    if ("M".equalsIgnoreCase(rawGender) || "MALE".equalsIgnoreCase(rawGender) || "男".equals(rawGender)) {
                        userGender = "男";
                    } else if ("F".equalsIgnoreCase(rawGender) || "FEMALE".equalsIgnoreCase(rawGender) || "女".equals(rawGender)) {
                        userGender = "女";
                    } else {
                        userGender = rawGender;
                    }
                }
                // 若数据库没有性别字段，从身份证第17位推算
                if ("-".equals(userGender) || userGender == null || userGender.isEmpty()) {
                    if (user.getIdCard() != null && user.getIdCard().length() >= 18) {
                        int digit17 = Integer.parseInt(user.getIdCard().substring(16, 17));
                        userGender = (digit17 % 2 == 1) ? "男" : "女";
                    }
                }
                // 从身份证推算年龄
                if (user.getIdCard() != null && user.getIdCard().length() >= 18) {
                    try {
                        int birthYear = Integer.parseInt(user.getIdCard().substring(6, 10));
                        userAge = LocalDate.now().getYear() - birthYear;
                    } catch (Exception ignored) {}
                }
            }
        }
        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("name", userName);
        userInfo.put("gender", userGender);
        userInfo.put("phone", userPhone);
        userInfo.put("idCard", userIdCard);
        userInfo.put("age", userAge);
        userInfo.put("packageName", packageName);
        data.put("userInfo", userInfo);

        // 检查结果
        List<Map<String, Object>> itemList = new ArrayList<>();
        for (CheckResult r : results) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("itemName", r.getItemName());
            item.put("unit", r.getUnit());
            item.put("numericValue", r.getNumericValue());
            item.put("textValue", r.getTextValue());
            item.put("abnormalLevel", r.getAbnormalLevel().name());
            item.put("referenceMin", r.getReferenceMin());
            item.put("referenceMax", r.getReferenceMax());

            String flag = "";
            if (r.getNumericValue() != null && r.getReferenceMin() != null && r.getReferenceMax() != null) {
                if (r.getAbnormalLevel() == AbnormalLevel.CRITICAL) flag = "‼";
                else if (r.getNumericValue().compareTo(r.getReferenceMax()) > 0) flag = "↑";
                else if (r.getNumericValue().compareTo(r.getReferenceMin()) < 0) flag = "↓";
            }
            item.put("flag", flag);
            itemList.add(item);
        }
        data.put("items", itemList);

        return data;
    }

    // ==================== 绘图方法 ====================

    private float drawTextBlock(PDPageContentStream cs, float y, String text) throws IOException {
        if (text == null || text.isEmpty()) return y;
        float fontSize = 10;
        float lineHeight = fontSize + 4;
        int charsPerLine = (int) (CONTENT_W / (fontSize * 0.5));
        for (int i = 0; i < text.length(); i += charsPerLine) {
            int end = Math.min(i + charsPerLine, text.length());
            String line = text.substring(i, end);
            cs.beginText();
            cs.setFont(fontNormal, fontSize);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText(line);
            cs.endText();
            y -= lineHeight;
        }
        return y - 4;
    }

    // ---- 标题 ----

    private float drawHeader(PDPageContentStream cs, float y, Map<String, Object> detail) throws IOException {
        // 医院名称
        cs.beginText();
        cs.setFont(fontNormal, 12);
        cs.newLineAtOffset(MARGIN + CONTENT_W / 2 - 50, y);
        cs.showText("熙心健康体检中心");
        cs.endText();
        y -= 28;
        // 报告标题
        cs.beginText();
        cs.setFont(fontNormal, 22);
        cs.newLineAtOffset(MARGIN + CONTENT_W / 2 - 60, y);
        cs.showText("体  检  报  告");
        cs.endText();
        y -= 30;
        // 分割线
        cs.setStrokingColor(200 / 255f, 200 / 255f, 200 / 255f);
        cs.setLineWidth(0.5f);
        cs.moveTo(MARGIN, y);
        cs.lineTo(MARGIN + CONTENT_W, y);
        cs.stroke();
        y -= 16;
        // 编号 + 日期
        String reportNo = (String) detail.getOrDefault("reportNo", "");
        cs.beginText();
        cs.setFont(fontNormal, 9);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText("报告编号：" + reportNo);
        cs.endText();
        String date = detail.getOrDefault("generatedAt", "").toString();
        if (date.length() > 10) date = date.substring(0, 10);
        if (date.isEmpty()) date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        cs.beginText();
        cs.setFont(fontNormal, 9);
        cs.newLineAtOffset(MARGIN + CONTENT_W - 100, y);
        cs.showText("报告日期：" + date);
        cs.endText();
        y -= 20;
        return y;
    }

    // ---- 状态横幅（已修复对齐） ----

    private float drawStatusBanner(PDPageContentStream cs, float y, Map<String, Object> detail) throws IOException {
        String status = (String) detail.getOrDefault("abnormalStatus", "NORMAL");
        String label, desc;
        float r, g, b;
        switch (status) {
            case "CRITICAL":
                label = "危急"; desc = "存在危急值，请立即就医";
                r = 0.85f; g = 0.25f; b = 0.25f; break;
            case "ABNORMAL":
                label = "异常"; desc = "部分指标异常，建议复查";
                r = 0.85f; g = 0.55f; b = 0.15f; break;
            default:
                label = "正常"; desc = "各项指标均在正常范围内";
                r = 0.3f; g = 0.5f; b = 0.95f; break;
        }
        float bannerH = 35;
        cs.setNonStrokingColor(r, g, b);
        cs.addRect(MARGIN, y - bannerH, CONTENT_W, bannerH);
        cs.fill();
        // 文字垂直居中
        cs.setNonStrokingColor(1, 1, 1);
        cs.beginText();
        cs.setFont(fontNormal, 14);
        cs.newLineAtOffset(MARGIN + 12, y - 17);
        cs.showText(label);
        cs.endText();
        cs.beginText();
        cs.setFont(fontNormal, 10);
        cs.newLineAtOffset(MARGIN + 60, y - 15);
        cs.showText(desc);
        cs.endText();
        y -= (bannerH + 7);
        return y;
    }

    // ---- 小标题 ----

    private float drawSection(PDPageContentStream cs, float y, String title, boolean isWarning) throws IOException {
        y -= 6;
        cs.setStrokingColor(isWarning ? 0.85f : 0.4f, isWarning ? 0.4f : 0.6f, isWarning ? 0.4f : 0.95f);
        cs.setLineWidth(3);
        cs.moveTo(MARGIN, y);
        cs.lineTo(MARGIN, y - 14);
        cs.stroke();
        cs.beginText();
        cs.setFont(fontNormal, 12);
        cs.setNonStrokingColor(0.1f, 0.2f, 0.3f);
        cs.newLineAtOffset(MARGIN + 8, y - 12);
        cs.showText(title);
        cs.endText();
        y -= 26;
        return y;
    }

    // ---- 用户信息 ----

    @SuppressWarnings("unchecked")
    private float drawUserInfo(PDPageContentStream cs, float y, Map<String, Object> detail) throws IOException {
        Map<String, Object> ui = (Map<String, Object>) detail.get("userInfo");
        String name = ui != null ? (String) ui.getOrDefault("name", "-") : "-";
        String gender = ui != null ? (String) ui.getOrDefault("gender", "-") : "-";
        String phone = ui != null ? (String) ui.getOrDefault("phone", "-") : "-";
        String idCard = ui != null ? (String) ui.getOrDefault("idCard", "-") : "-";
        int age = ui != null ? ((Number) ui.getOrDefault("age", 0)).intValue() : 0;
        String pkg = ui != null ? (String) ui.getOrDefault("packageName", "-") : "-";

        String[][] fields = {
            {"姓    名", name},
            {"性    别", gender + "    " + age + "岁"},
            {"手机号码", phone},
            {"证件号码", idCard},
            {"套餐名称", pkg},
        };
        float rowH = 20, bgH = 6 + fields.length * rowH;

        cs.setNonStrokingColor(0.95f, 0.97f, 0.98f);
        cs.addRect(MARGIN, y - bgH, CONTENT_W, bgH);
        cs.fill();

        for (int i = 0; i < fields.length; i++) {
            cs.beginText();
            cs.setFont(fontNormal, 9);
            cs.setNonStrokingColor(0.5f, 0.6f, 0.7f);
            cs.newLineAtOffset(MARGIN + 8, y - 6 - i * rowH);
            cs.showText(fields[i][0]);
            cs.endText();
            cs.beginText();
            cs.setFont(fontNormal, 9);
            cs.setNonStrokingColor(0.1f, 0.15f, 0.2f);
            cs.newLineAtOffset(MARGIN + 80, y - 6 - i * rowH);
            cs.showText(fields[i][1]);
            cs.endText();
        }
        y -= bgH + 4;
        return y;
    }

    // ---- 统计 ----

    @SuppressWarnings("unchecked")
    private float drawStats(PDPageContentStream cs, float y, Map<String, Object> detail) throws IOException {
        Map<String, Object> stats = (Map<String, Object>) detail.get("stats");
        if (stats == null) return y;
        long total = ((Number) stats.getOrDefault("totalItems", 0)).longValue();
        long abnormal = ((Number) stats.getOrDefault("abnormalCount", 0)).longValue();
        long critical = ((Number) stats.getOrDefault("criticalCount", 0)).longValue();
        String text = "共 " + total + " 项检查";
        if (abnormal > 0) text += "  |  异常 " + abnormal + " 项";
        if (critical > 0) text += "  |  危急 " + critical + " 项";
        cs.beginText();
        cs.setFont(fontNormal, 10);
        cs.setNonStrokingColor(0.2f, 0.3f, 0.4f);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(text);
        cs.endText();
        y -= 22;
        return y;
    }

    // ---- 健康风险 ----

    @SuppressWarnings("unchecked")
    private float drawHealthRisk(PDPageContentStream cs, float y, Map<String, Object> detail) throws IOException {
        String level = (String) detail.get("healthRiskLevel");
        String assessment = (String) detail.get("healthRiskAssessment");
        if (level != null) {
            cs.beginText();
            cs.setFont(fontNormal, 10);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText("健康风险等级：" + level);
            cs.endText();
            y -= 18;
        }
        if (assessment != null && !assessment.isEmpty()) {
            y = drawTextBlock(cs, y, assessment);
        }
        List<Map<String, String>> warnings = (List<Map<String, String>>) detail.get("diseaseWarnings");
        if (warnings != null && !warnings.isEmpty()) {
            y -= 6;
            for (Map<String, String> w : warnings) {
                String warning = w.get("warning");
                String suggestion = w.get("suggestion");
                if (warning != null) {
                    cs.beginText();
                    cs.setFont(fontNormal, 9);
                    cs.setNonStrokingColor(0.85f, 0.2f, 0.2f);
                    cs.newLineAtOffset(MARGIN + 6, y);
                    cs.showText("⚠ " + warning);
                    cs.endText();
                    y -= 16;
                }
                if (suggestion != null && !suggestion.isEmpty()) {
                    y = drawTextBlock(cs, y, suggestion);
                }
            }
        }
        return y;
    }

    // ---- 检查结果表格 ----

    @SuppressWarnings("unchecked")
    private float drawResults(PDPageContentStream cs, float y, Map<String, Object> detail) throws IOException {
        List<Map<String, Object>> items = (List<Map<String, Object>>) detail.get("items");
        if (items == null || items.isEmpty()) {
            cs.beginText();
            cs.setFont(fontNormal, 10);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText("暂无检查数据");
            cs.endText();
            return y - 20;
        }
        float[] colW = {CONTENT_W * 0.30f, CONTENT_W * 0.20f, CONTENT_W * 0.12f, CONTENT_W * 0.38f};
        float[] colX = {MARGIN, MARGIN + colW[0], MARGIN + colW[0] + colW[1], MARGIN + colW[0] + colW[1] + colW[2]};
        String[] headers = {"检查项目", "结果", "提示", "参考范围"};
        float rowH = 20;

        cs.setNonStrokingColor(0.9f, 0.93f, 0.95f);
        cs.addRect(MARGIN, y - rowH, CONTENT_W, rowH);
        cs.fill();
        cs.setNonStrokingColor(0.4f, 0.5f, 0.6f);
        for (int i = 0; i < headers.length; i++) {
            cs.beginText();
            cs.setFont(fontNormal, 9);
            cs.newLineAtOffset(colX[i] + 4, y - 14);
            cs.showText(headers[i]);
            cs.endText();
        }
        y -= rowH;

        for (Map<String, Object> item : items) {
            String name = (String) item.getOrDefault("itemName", "");
            String flag = (String) item.getOrDefault("flag", "");
            String value = item.get("numericValue") != null ? item.get("numericValue").toString()
                    : (String) item.getOrDefault("textValue", "-");
            String ref;
            if (item.get("referenceMin") != null) {
                ref = item.get("referenceMin") + " ~ " + item.get("referenceMax")
                        + " " + (item.get("unit") != null ? item.get("unit") : "");
            } else {
                ref = (String) item.getOrDefault("unit", "-");
            }
            String level = (String) item.getOrDefault("abnormalLevel", "NORMAL");
            boolean isAbnormal = "ABNORMAL".equals(level) || "CRITICAL".equals(level);

            if (isAbnormal) {
                cs.setNonStrokingColor(1.0f, 0.97f, 0.92f);
                cs.addRect(MARGIN, y - rowH, CONTENT_W, rowH);
                cs.fill();
            } else if (y % 40 < 20) {
                cs.setNonStrokingColor(0.99f, 0.99f, 0.99f);
                cs.addRect(MARGIN, y - rowH, CONTENT_W, rowH);
                cs.fill();
            }

            cs.setNonStrokingColor(isAbnormal ? 0.85f : 0.1f, isAbnormal ? 0.5f : 0.15f, isAbnormal ? 0.15f : 0.2f);
            cs.beginText();
            cs.setFont(fontNormal, 9);
            cs.newLineAtOffset(colX[0] + 4, y - 14);
            cs.showText(truncate(name, 16));
            cs.endText();
            cs.beginText();
            cs.setFont(fontNormal, 9);
            cs.newLineAtOffset(colX[1] + 4, y - 14);
            cs.showText(truncate(value, 10));
            cs.endText();
            cs.beginText();
            cs.setFont(fontNormal, 9);
            cs.newLineAtOffset(colX[2] + 4, y - 14);
            cs.showText(flag.isEmpty() ? "-" : flag);
            cs.endText();
            cs.beginText();
            cs.setFont(fontNormal, 8);
            cs.setNonStrokingColor(0.5f, 0.6f, 0.7f);
            cs.newLineAtOffset(colX[3] + 4, y - 14);
            cs.showText(truncate(ref, 20));
            cs.endText();
            y -= rowH;
        }

        cs.setStrokingColor(0.85f, 0.85f, 0.85f);
        cs.setLineWidth(0.5f);
        cs.moveTo(MARGIN, y);
        cs.lineTo(MARGIN + CONTENT_W, y);
        cs.stroke();
        return y - 12;
    }

    // ---- 个性化建议 ----

    @SuppressWarnings("unchecked")
    private float drawRecommendations(PDPageContentStream cs, float y, Map<String, Object> detail) throws IOException {
        Map<String, List<String>> recs = (Map<String, List<String>>) detail.get("personalizedRecommendations");
        if (recs == null) return y;
        String[][] categories = {{"diet", "饮食建议"}, {"exercise", "运动建议"}, {"followUp", "复查建议"}};
        for (String[] cat : categories) {
            List<String> items = recs.get(cat[0]);
            if (items != null && !items.isEmpty()) {
                cs.beginText();
                cs.setFont(fontNormal, 10);
                cs.setNonStrokingColor(0.3f, 0.4f, 0.6f);
                cs.newLineAtOffset(MARGIN + 4, y);
                cs.showText(cat[1]);
                cs.endText();
                y -= 18;
                for (String item : items) {
                    cs.beginText();
                    cs.setFont(fontNormal, 9);
                    cs.setNonStrokingColor(0.2f, 0.3f, 0.4f);
                    cs.newLineAtOffset(MARGIN + 10, y);
                    cs.showText("• " + item);
                    cs.endText();
                    y -= 16;
                }
                y -= 4;
            }
        }
        return y;
    }

    // ---- 免责声明 ----

    private float drawDisclaimer(PDPageContentStream cs, float y, Map<String, Object> detail) throws IOException {
        y -= 20;
        cs.setStrokingColor(0.85f, 0.85f, 0.85f);
        cs.setLineWidth(0.5f);
        cs.moveTo(MARGIN + 40, y);
        cs.lineTo(MARGIN + CONTENT_W - 40, y);
        cs.stroke();
        y -= 14;
        String disclaimer = (String) detail.getOrDefault("disclaimer", "本报告仅供健康参考，不作为临床诊断依据");
        cs.beginText();
        cs.setFont(fontNormal, 8);
        cs.setNonStrokingColor(0.7f, 0.75f, 0.8f);
        float textW = disclaimer.length() * 4;
        cs.newLineAtOffset(MARGIN + (CONTENT_W - textW) / 2, y);
        cs.showText(disclaimer);
        cs.endText();
        return y - 14;
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        int len = 0, idx = 0;
        for (char c : s.toCharArray()) {
            len += (c > 127) ? 2 : 1;
            if (len > maxLen * 2) break;
            idx++;
        }
        return s.substring(0, idx);
    }
}
