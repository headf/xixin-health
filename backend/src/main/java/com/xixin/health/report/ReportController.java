package com.xixin.health.report;

import com.xixin.health.common.api.ApiResponse;
import com.xixin.health.configuration.Department;
import com.xixin.health.configuration.DepartmentRepository;
import com.xixin.health.exam.ExamRecord;
import com.xixin.health.exam.ExamRecordRepository;
import com.xixin.health.iam.UserAccount;
import com.xixin.health.iam.UserAccountRepository;
import com.xixin.health.profile.HealthProfile;
import com.xixin.health.profile.HealthProfileRepository;
import com.xixin.health.result.CheckResult;
import com.xixin.health.result.CheckResultRepository;
import com.xixin.health.result.AbnormalLevel;
import jakarta.validation.Valid;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.*;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;
    private final ExamRecordRepository examRecordRepository;
    private final CheckResultRepository checkResultRepository;
    private final UserAccountRepository userAccountRepository;
    private final DepartmentRepository departmentRepository;
    private final TrendAnalysisService trendAnalysisService;
    private final ReportAnalysisService reportAnalysisService;
    private final JdbcTemplate jdbc;
    private final HealthProfileRepository healthProfileRepository;

    public ReportController(ReportService reportService,
                            ExamRecordRepository examRecordRepository,
                            CheckResultRepository checkResultRepository,
                            UserAccountRepository userAccountRepository,
                            DepartmentRepository departmentRepository,
                            TrendAnalysisService trendAnalysisService,
                            ReportAnalysisService reportAnalysisService,
                            JdbcTemplate jdbc,
                            HealthProfileRepository healthProfileRepository) {
        this.reportService = reportService;
        this.examRecordRepository = examRecordRepository;
        this.checkResultRepository = checkResultRepository;
        this.userAccountRepository = userAccountRepository;
        this.departmentRepository = departmentRepository;
        this.trendAnalysisService = trendAnalysisService;
        this.reportAnalysisService = reportAnalysisService;
        this.jdbc = jdbc;
        this.healthProfileRepository = healthProfileRepository;
    }

    // ========== 生成报告 (自动) ==========

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('REVIEW_DOCTOR', 'EXAM_DOCTOR')")
    public ApiResponse<ExamReport> generate(@Valid @RequestBody GenerateReportRequest request) {
        return ApiResponse.success(reportService.generateReport(
            request.examRecordId(), request.reviewerId()));
    }

    // ========== 填写诊断总结 (Review Doctor) ==========

    @PutMapping("/{reportId}/diagnosis")
    @PreAuthorize("hasRole('REVIEW_DOCTOR')")
    public ApiResponse<ExamReport> writeDiagnosis(@PathVariable Long reportId,
                                                   @Valid @RequestBody DiagnosisRequest request) {
        return ApiResponse.success(reportService.writeDiagnosis(
            reportId, request.summary(), request.doctorAdvice()));
    }

    // ========== 终审签发 (Review Doctor) ==========

    @PostMapping("/{reportId}/publish")
    @PreAuthorize("hasRole('REVIEW_DOCTOR')")
    public ApiResponse<ExamReport> publish(@PathVariable Long reportId,
                                            @RequestBody(required = false) PublishRequest request) {
        if (request != null) {
            return ApiResponse.success(reportService.publishReport(
                reportId, request.summary(), request.doctorAdvice(), request.followUpSuggestion()));
        }
        return ApiResponse.success(reportService.publishReport(reportId, null, null, null));
    }

    // ========== 综合终审 (主检医生) ==========

    @PutMapping("/{reportId}/comprehensive-audit")
    @PreAuthorize("hasRole('REVIEW_DOCTOR')")
    public ApiResponse<ExamReport> comprehensiveAudit(@PathVariable Long reportId,
                                                       @Valid @RequestBody ComprehensiveAuditRequest request) {
        return ApiResponse.success(reportService.comprehensiveAudit(
            reportId,
            request.summary(),
            request.doctorAdvice(),
            request.overallHealthAssessment(),
            request.lifestyleRecommendations(),
            request.followUpRecommendations(),
            request.classification()));
    }

    // ========== 报告查询 ==========

    @GetMapping("/{reportId}")
    public ApiResponse<ExamReport> getById(@PathVariable Long reportId) {
        return ApiResponse.success(reportService.getReport(reportId));
    }

    @GetMapping("/by-exam/{examRecordId}")
    public ApiResponse<List<ExamReport>> getByExam(@PathVariable Long examRecordId) {
        return ApiResponse.success(reportService.getReportsByExamRecord(examRecordId));
    }

    @GetMapping("/by-user/{userId}")
    public ApiResponse<List<ExamReport>> getByUser(@PathVariable Long userId) {
        return ApiResponse.success(reportService.getReportsByUser(userId));
    }

    @GetMapping("/published")
    @PreAuthorize("hasRole('REVIEW_DOCTOR')")
    public ApiResponse<List<ExamReport>> getPublishedReports() {
        return ApiResponse.success(reportService.getPublishedReports());
    }

    @PostMapping("/{reportId}/send")
    @PreAuthorize("hasRole('REVIEW_DOCTOR')")
    public ApiResponse<ExamReport> sendReport(@PathVariable Long reportId) {
        return ApiResponse.success(reportService.sendReportToUser(reportId));
    }

    // ========== 报告申诉 ==========

    @PostMapping("/{reportId}/appeal")
    @PreAuthorize("hasAnyRole('REVIEW_DOCTOR', 'SYSTEM_ADMIN')")
    public ApiResponse<ExamReport> appeal(@PathVariable Long reportId, @RequestBody Map<String, String> body) {
        return ApiResponse.success(reportService.appealReport(reportId, body.get("reason")));
    }

    // ========== 用户体检指标趋势数据 ==========

    /**
     * 趋势对比规则：
     * - 不足2份 → 返回空
     * - 2份 → 对比2份
     * - 3+份 → 对比最近有数据的3份
     */
    private List<ExamRecord> getLatestRecordsForTrend(Long userId) {
        List<ExamRecord> allRecords = examRecordRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<ExamRecord> validRecords = new ArrayList<>();
        for (ExamRecord rec : allRecords) {
            if (validRecords.size() >= 3) break;
            List<CheckResult> results = checkResultRepository.findByExamRecordId(rec.getId());
            // 只选取包含数值型检查结果的记录（非数值型如影像描述无法做趋势对比）
            boolean hasNumeric = results.stream().anyMatch(r -> r.getNumericValue() != null);
            if (hasNumeric) {
                validRecords.add(rec);
            }
        }
        return validRecords;
    }

    @GetMapping("/trend/{userId}")
    public ApiResponse<List<Map<String, Object>>> getTrend(@PathVariable Long userId) {
        List<ExamRecord> records = getLatestRecordsForTrend(userId);
        if (records.isEmpty()) {
            return ApiResponse.success(List.of());
        }
        // For each exam record, get all check results and organize by item
        List<Map<String, Object>> trendData = new ArrayList<>();
        for (ExamRecord rec : records) {
            List<CheckResult> results = checkResultRepository.findByExamRecordId(rec.getId());
            if (results.isEmpty()) continue;
            Map<String, Object> recordMap = new HashMap<>();
            recordMap.put("examRecordId", rec.getId());
            recordMap.put("examNo", rec.getExamNo());
            recordMap.put("date", rec.getCreatedAt() != null ? rec.getCreatedAt().toLocalDate().toString() : "");
            recordMap.put("status", rec.getStatus().name());
            List<Map<String, Object>> items = results.stream().map(r -> {
                Map<String, Object> m = new HashMap<>();
                m.put("itemId", r.getItemId());
                m.put("itemName", r.getItemName());
                m.put("unit", r.getUnit());
                m.put("numericValue", r.getNumericValue() != null ? r.getNumericValue().doubleValue() : null);
                m.put("textValue", r.getTextValue());
                m.put("abnormalLevel", r.getAbnormalLevel().name());
                m.put("referenceMin", r.getReferenceMin() != null ? r.getReferenceMin().doubleValue() : null);
                m.put("referenceMax", r.getReferenceMax() != null ? r.getReferenceMax().doubleValue() : null);
                return m;
            }).collect(Collectors.toList());
            recordMap.put("items", items);
            trendData.add(recordMap);
        }
        java.util.Collections.reverse(trendData);
        return ApiResponse.success(trendData);
    }

    @GetMapping("/trend/by-item/{userId}")
    public ApiResponse<List<Map<String, Object>>> getTrendByItem(@PathVariable Long userId) {
        List<ExamRecord> records = getLatestRecordsForTrend(userId);
        if (records.isEmpty()) {
            return ApiResponse.success(List.of());
        }
        // Group results by item across all exam records
        Map<Long, Map<String, Object>> itemMap = new LinkedHashMap<>();
        for (ExamRecord rec : records) {
            List<CheckResult> results = checkResultRepository.findByExamRecordId(rec.getId());
            String date = rec.getCheckedInAt() != null ? rec.getCheckedInAt().toLocalDate().toString()
                : (rec.getCreatedAt() != null ? rec.getCreatedAt().toLocalDate().toString() : "");
            for (CheckResult r : results) {
                if (r.getNumericValue() == null) continue; // only numeric items
                Long itemId = r.getItemId();
                Map<String, Object> entry = itemMap.computeIfAbsent(itemId, k -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("itemId", itemId);
                    m.put("itemName", r.getItemName());
                    m.put("unit", r.getUnit() != null ? r.getUnit() : "");
                    m.put("referenceMin", r.getReferenceMin() != null ? r.getReferenceMin().doubleValue() : null);
                    m.put("referenceMax", r.getReferenceMax() != null ? r.getReferenceMax().doubleValue() : null);
                    m.put("values", new ArrayList<Map<String, Object>>());
                    return m;
                });
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> values = (List<Map<String, Object>>) entry.get("values");
                Map<String, Object> point = new HashMap<>();
                point.put("date", date);
                point.put("value", r.getNumericValue().doubleValue());
                point.put("abnormalLevel", r.getAbnormalLevel().name());
                values.add(point);
            }
        }
        // Reverse values to chronological order (earliest to latest)
        for (Map<String, Object> entry : itemMap.values()) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> vals = (List<Map<String, Object>>) entry.get("values");
            java.util.Collections.reverse(vals);
        }
        return ApiResponse.success(new ArrayList<>(itemMap.values()));
    }

    // ========== 报告详情（含用户信息、按科室分组的结果） ==========

    @GetMapping("/{reportId}/detail")
    public ApiResponse<Map<String, Object>> getReportDetail(@PathVariable Long reportId) {
        ExamReport report = reportService.getReport(reportId);
        Map<String, Object> result = new LinkedHashMap<>();

        // 1. 报告基本信息
        result.put("id", report.getId());
        result.put("reportNo", report.getReportNo());
        result.put("examRecordId", report.getExamRecordId());
        result.put("abnormalStatus", report.getAbnormalStatus().name());
        result.put("reportStatus", report.getReportStatus().name());
        result.put("generatedAt", report.getGeneratedAt() != null ? report.getGeneratedAt().toString() : null);
        result.put("publishedAt", report.getPublishedAt() != null ? report.getPublishedAt().toString() : null);
        result.put("classification", report.getClassification());
        result.put("summary", report.getSummary());
        result.put("doctorAdvice", report.getDoctorAdvice());
        result.put("overallHealthAssessment", report.getOverallHealthAssessment());
        result.put("lifestyleRecommendations", report.getLifestyleRecommendations());
        result.put("followUpRecommendations", report.getFollowUpRecommendations());
        result.put("signedAt", report.getSignedAt() != null ? report.getSignedAt().toString() : null);

        // 2. 用户信息
        ExamRecord examRecord = examRecordRepository.findById(report.getExamRecordId()).orElse(null);
        if (examRecord != null) {
            result.put("examNo", examRecord.getExamNo());
            result.put("checkedInAt", examRecord.getCheckedInAt() != null ? examRecord.getCheckedInAt().toString() : null);
            result.put("packageName", examRecord.getPackageName());

            // 就诊人信息：从 HealthProfile（预约时选择的就诊人）获取，而非当前登录用户
            HealthProfile profile = healthProfileRepository.findById(examRecord.getProfileId()).orElse(null);
            if (profile != null) {
                Map<String, Object> userInfo = new LinkedHashMap<>();
                userInfo.put("id", profile.getId());
                userInfo.put("name", profile.getName());
                userInfo.put("phone", profile.getPhone());
                userInfo.put("idCard", profile.getIdCardNo());
                // 性别：从身份证推算
                String gender = "未知";
                if (profile.getIdCardNo() != null && profile.getIdCardNo().length() >= 18) {
                    int digit17 = Integer.parseInt(profile.getIdCardNo().substring(16, 17));
                    gender = (digit17 % 2 == 1) ? "男" : "女";
                }
                userInfo.put("gender", gender);
                // 年龄：从身份证推算
                int age = 0;
                if (profile.getIdCardNo() != null && profile.getIdCardNo().length() >= 18) {
                    try {
                        int birthYear = Integer.parseInt(profile.getIdCardNo().substring(6, 10));
                        age = java.time.LocalDate.now().getYear() - birthYear;
                    } catch (Exception ignored) {}
                }
                userInfo.put("age", age);
                result.put("userInfo", userInfo);
            }
        }

        // 3. 检查结果 - 按科室分组
        List<CheckResult> allResults = checkResultRepository.findByExamRecordId(report.getExamRecordId());
        List<Department> allDepts = departmentRepository.findAll();

        // 建立 itemId → departmentId 映射（从进度表反向查）
        Map<Long, Long> itemDeptMap = new HashMap<>();
        try {
            jdbc.query("SELECT DISTINCT item_id, department_id FROM t_exam_progress WHERE exam_record_id = ?",
                new Object[]{report.getExamRecordId()},
                (rs) -> {
                    itemDeptMap.put(rs.getLong("item_id"), rs.getLong("department_id"));
                }
            );
        } catch (Exception ignored) {}
        // 如果进度表没有数据，从 t_exam_item 表查
        if (itemDeptMap.isEmpty()) {
            try {
                jdbc.query("SELECT id, department_id FROM t_exam_item WHERE id IN (SELECT item_id FROM t_check_result WHERE exam_record_id = ?)",
                    new Object[]{report.getExamRecordId()},
                    (rs) -> {
                        itemDeptMap.put(rs.getLong("id"), rs.getLong("department_id"));
                    }
                );
            } catch (Exception ignored2) {}
        }

        // 按部门分组
        Map<Long, List<Map<String, Object>>> deptResults = new LinkedHashMap<>();
        Map<Long, String> deptNames = new HashMap<>();
        for (Department d : allDepts) {
            deptNames.put(d.getId(), d.getDepartmentName());
        }

        for (CheckResult cr : allResults) {
            Long deptId = itemDeptMap.get(cr.getItemId());
            if (deptId == null) deptId = 0L; // 未分类

            Map<String, Object> itemMap = new LinkedHashMap<>();
            itemMap.put("id", cr.getId());
            itemMap.put("itemId", cr.getItemId());
            itemMap.put("itemName", cr.getItemName());
            itemMap.put("unit", cr.getUnit());
            itemMap.put("numericValue", cr.getNumericValue() != null ? cr.getNumericValue().doubleValue() : null);
            itemMap.put("textValue", cr.getTextValue());
            itemMap.put("abnormalLevel", cr.getAbnormalLevel().name());
            itemMap.put("referenceMin", cr.getReferenceMin() != null ? cr.getReferenceMin().doubleValue() : null);
            itemMap.put("referenceMax", cr.getReferenceMax() != null ? cr.getReferenceMax().doubleValue() : null);

            // 计算异常方向
            String flag = "";
            if (cr.getNumericValue() != null && cr.getReferenceMin() != null && cr.getReferenceMax() != null) {
                if (cr.getAbnormalLevel() == AbnormalLevel.CRITICAL) {
                    flag = "‼";
                } else if (cr.getNumericValue().compareTo(cr.getReferenceMax()) > 0) {
                    flag = "↑";
                } else if (cr.getNumericValue().compareTo(cr.getReferenceMin()) < 0) {
                    flag = "↓";
                }
            }
            itemMap.put("flag", flag);

            deptResults.computeIfAbsent(deptId, k -> new ArrayList<>()).add(itemMap);
        }

        // 构建部门分组列表
        List<Map<String, Object>> departmentGroups = new ArrayList<>();
        // 先未分类
        if (deptResults.containsKey(0L)) {
            Map<String, Object> unknown = new LinkedHashMap<>();
            unknown.put("departmentId", 0);
            unknown.put("departmentName", "其他检查");
            unknown.put("items", deptResults.get(0L));
            departmentGroups.add(unknown);
            deptResults.remove(0L);
        }
        // 已分类的
        for (Map.Entry<Long, List<Map<String, Object>>> entry : deptResults.entrySet()) {
            Map<String, Object> group = new LinkedHashMap<>();
            group.put("departmentId", entry.getKey());
            group.put("departmentName", deptNames.getOrDefault(entry.getKey(), "未知科室"));
            group.put("items", entry.getValue());
            departmentGroups.add(group);
        }
        result.put("departmentGroups", departmentGroups);

        // 4. 统计信息
        long totalItems = allResults.size();
        long abnormalCount = allResults.stream().filter(r -> r.getAbnormalLevel() == AbnormalLevel.ABNORMAL).count();
        long criticalCount = allResults.stream().filter(r -> r.getAbnormalLevel() == AbnormalLevel.CRITICAL).count();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalItems", totalItems);
        stats.put("abnormalCount", abnormalCount);
        stats.put("criticalCount", criticalCount);
        result.put("stats", stats);

        // ========== 5. AI 健康风险评估与分析 ==========
        String riskLevel = reportAnalysisService.computeHealthRiskLevel(allResults);
        result.put("healthRiskLevel", riskLevel);
        result.put("healthRiskAssessment", reportAnalysisService.generateRiskAssessment(allResults, riskLevel));

        // 疾病预警
        result.put("diseaseWarnings", reportAnalysisService.generateDiseaseWarnings(allResults, deptNames));

        // 个性化健康建议（饮食/运动/复查）
        result.put("personalizedRecommendations", reportAnalysisService.generatePersonalizedRecommendations(allResults));

        // 免责声明
        result.put("disclaimer", reportAnalysisService.getDisclaimer());

        // 为每个异常 item 注入风险提示
        for (Map<String, Object> group : departmentGroups) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) group.get("items");
            if (items == null) continue;
            for (Map<String, Object> item : items) {
                String level = (String) item.get("abnormalLevel");
                if (!"NORMAL".equals(level) && item.get("itemId") != null) {
                    // 查找对应的 CheckResult 以生成风险提示
                    Long itemId = item.get("itemId") instanceof Number
                        ? ((Number) item.get("itemId")).longValue() : null;
                    if (itemId != null) {
                        allResults.stream()
                            .filter(r -> r.getItemId().equals(itemId))
                            .findFirst()
                            .ifPresent(r -> item.put("riskPrompt", reportAnalysisService.generateItemRiskPrompt(r)));
                    }
                }
            }
        }

        return ApiResponse.success(result);
    }

    // ========== AI 趋势智能分析 ==========

    @GetMapping("/trend/analysis/{userId}")
    public ApiResponse<Map<String, Object>> getTrendAnalysis(@PathVariable Long userId) {
        return ApiResponse.success(trendAnalysisService.analyzeTrend(userId));
    }

    // ========== 历史结果对比 ==========

    @GetMapping("/{reportId}/history")
    public ApiResponse<List<ExamReport>> getHistory(@PathVariable Long reportId) {
        ExamReport current = reportService.getReport(reportId);
        return ApiResponse.success(reportService.getReportsByUser(current.getUserId()).stream()
            .filter(r -> !r.getId().equals(reportId) && r.getReportStatus() != null)
            .sorted(Comparator.comparing(ExamReport::getGeneratedAt).reversed())
            .limit(3).toList());
    }

    // ==================== Request DTOs ====================

    public record GenerateReportRequest(@NotNull Long examRecordId, @NotNull Long reviewerId) {}

    public record DiagnosisRequest(String summary, String doctorAdvice) {}

    public record PublishRequest(String summary, String doctorAdvice, String followUpSuggestion) {}

    public record ComprehensiveAuditRequest(
        String summary,
        String doctorAdvice,
        @NotNull String overallHealthAssessment,
        @NotNull String lifestyleRecommendations,
        @NotNull String followUpRecommendations,
        @NotNull String classification  // NORMAL / PHYSIOLOGICAL / SUB_HEALTH / NEED_RECHECK / NEED_TREATMENT
    ) {}
}
