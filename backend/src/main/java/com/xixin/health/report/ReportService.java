package com.xixin.health.report;

import com.xixin.health.audit.AuditService;
import com.xixin.health.iam.UserAccount;
import com.xixin.health.iam.UserAccountRepository;
import com.xixin.health.common.error.BusinessException;
import com.xixin.health.common.error.ErrorCode;
import com.xixin.health.management.HealthManagement;
import com.xixin.health.management.HealthManagementRepository;
import com.xixin.health.notification.Notification;
import com.xixin.health.notification.NotificationRepository;
import com.xixin.health.result.AbnormalLevel;
import com.xixin.health.result.CheckResult;
import com.xixin.health.result.CheckResultRepository;
import com.xixin.health.result.ReviewStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class ReportService {

    private final CheckResultRepository checkResultRepository;
    private final ExamReportRepository examReportRepository;
    private final com.xixin.health.exam.ExamProgressRepository examProgressRepository;
    private final NotificationRepository notificationRepository;
    private final AuditService auditService;
    private final HealthManagementRepository healthManagementRepository;
    private final JdbcTemplate jdbc;
    private final UserAccountRepository userAccountRepository;

    public ReportService(CheckResultRepository checkResultRepository,
                         ExamReportRepository examReportRepository,
                         com.xixin.health.exam.ExamProgressRepository examProgressRepository,
                         NotificationRepository notificationRepository,
                         AuditService auditService,
                         HealthManagementRepository healthManagementRepository,
                         JdbcTemplate jdbc,
                         UserAccountRepository userAccountRepository) {
        this.checkResultRepository = checkResultRepository;
        this.examReportRepository = examReportRepository;
        this.examProgressRepository = examProgressRepository;
        this.notificationRepository = notificationRepository;
        this.auditService = auditService;
        this.healthManagementRepository = healthManagementRepository;
        this.jdbc = jdbc;
        this.userAccountRepository = userAccountRepository;
    }

    // ========== 生成报告 (自动，审核通过后) ==========

    @Transactional
    public ExamReport generateReport(Long examRecordId, Long reviewerId) {
        // 防止重复生成：如果该体检记录已有报告，返回最新一份
        List<ExamReport> existing = examReportRepository.findByExamRecordId(examRecordId);
        if (!existing.isEmpty()) {
            return existing.stream()
                .sorted(Comparator.comparing(ExamReport::getGeneratedAt).reversed())
                .findFirst()
                .orElse(existing.get(0));
        }

        List<CheckResult> results = checkResultRepository.findByExamRecordId(examRecordId);
        if (results.isEmpty()) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "No check results for exam record");
        }
        boolean allApproved = results.stream()
            .allMatch(result -> result.getReviewStatus() == ReviewStatus.APPROVED);
        if (!allApproved) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT,
                "All check results must be approved before report generation");
        }
        long pendingCount = examProgressRepository.countByExamRecordIdAndProgressStatus(examRecordId, "PENDING");
        if (pendingCount > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT,
                "Cannot generate report: " + pendingCount + " items still pending");
        }
        // 从体检记录中获取实际用户ID
        Long userId = 1L;
        try {
            var record = jdbc.queryForMap("SELECT user_id FROM t_exam_record WHERE id = ?", examRecordId);
            if (record.get("user_id") != null) {
                userId = ((Number) record.get("user_id")).longValue();
            }
        } catch (Exception ignored) {}
        AbnormalLevel abnormalLevel = results.stream()
            .map(CheckResult::getAbnormalLevel)
            .max(Comparator.comparingInt(this::severity))
            .orElse(AbnormalLevel.NORMAL);
        ExamReport report = examReportRepository.save(new ExamReport(examRecordId, userId, reviewerId, abnormalLevel));

        // 危急值自动预警：如果报告异常级别为 CRITICAL，自动创建通知
        if (abnormalLevel == AbnormalLevel.CRITICAL) {
            notificationRepository.save(new Notification(
                userId,
                "CRITICAL",
                "危急值预警",
                "您的体检结果中发现危急值，请立即联系医生进行详细咨询。"
            ));
        }

        return report;
    }

    @Transactional
    public ExamReport writeDiagnosis(Long reportId, String summary, String doctorAdvice) {
        ExamReport report = examReportRepository.findById(reportId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Report not found"));
        report.setSummary(summary);
        report.setDoctorAdvice(doctorAdvice);
        return examReportRepository.save(report);
    }

    @Transactional
    public ExamReport publishReport(Long reportId, String summary, String doctorAdvice, String followUpSuggestion) {
        ExamReport report = examReportRepository.findById(reportId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Report not found"));
        if (summary != null) report.setSummary(summary);
        if (doctorAdvice != null) report.setDoctorAdvice(doctorAdvice);

        // 如果报告为 ABNORMAL 或 CRITICAL，自动添加复查建议
        if (report.getAbnormalStatus() == AbnormalLevel.ABNORMAL
            || report.getAbnormalStatus() == AbnormalLevel.CRITICAL) {
            String suggestion = followUpSuggestion;
            if (suggestion == null || suggestion.isBlank()) {
                if (report.getAbnormalStatus() == AbnormalLevel.CRITICAL) {
                    suggestion = "您的体检结果中存在危急值，建议立即前往医院复查或就医。";
                } else {
                    suggestion = "您的体检结果中存在异常指标，建议定期复查，具体请咨询医生。";
                }
            }
            report.setFollowUpRecommendations(suggestion);
        } else if (followUpSuggestion != null && !followUpSuggestion.isBlank()) {
            report.setFollowUpRecommendations(followUpSuggestion);
        }

        report.publish();
        ExamReport saved = examReportRepository.save(report);

        // 自动创建健康管理记录（高危人群/随访干预）
        if (report.getAbnormalStatus() == AbnormalLevel.ABNORMAL
            || report.getAbnormalStatus() == AbnormalLevel.CRITICAL) {
            try {
                String riskLevel = report.getAbnormalStatus() == AbnormalLevel.CRITICAL ? "CRITICAL" : "HIGH";
                healthManagementRepository.findByReportId(reportId).orElseGet(() ->
                    healthManagementRepository.save(HealthManagement.open(
                        report.getUserId(), reportId, riskLevel))
                );
            } catch (Exception e) {
                System.out.println("自动创建健康管理记录失败，不影响报告发布: " + e.getMessage());
            }
        }

        return saved;
    }

    @Transactional
    public ExamReport comprehensiveAudit(Long reportId, String summary, String doctorAdvice,
                                          String overallHealthAssessment,
                                          String lifestyleRecommendations,
                                          String followUpRecommendations,
                                          String classification) {
        ExamReport report = examReportRepository.findById(reportId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Report not found"));
        report.setSummary(summary);
        report.setDoctorAdvice(doctorAdvice);
        report.setOverallHealthAssessment(overallHealthAssessment);
        report.setLifestyleRecommendations(lifestyleRecommendations);
        report.setFollowUpRecommendations(followUpRecommendations);
        report.finalSignOff(classification);
        return examReportRepository.save(report);
    }

    // ========== 报告查询 ==========

    @Transactional(readOnly = true)
    public ExamReport getReport(Long reportId) {
        return examReportRepository.findById(reportId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Report not found"));
    }

    private void enrichWithUserName(ExamReport report) {
        if (report != null && report.getUserName() == null && report.getUserId() != null) {
            userAccountRepository.findById(report.getUserId())
                .ifPresent(u -> report.setUserName(u.getDisplayName()));
        }
    }

    private void enrichWithUserNames(List<ExamReport> reports) {
        for (ExamReport r : reports) {
            enrichWithUserName(r);
        }
    }

    @Transactional(readOnly = true)
    public List<ExamReport> getReportsByExamRecord(Long examRecordId) {
        List<ExamReport> reports = examReportRepository.findByExamRecordId(examRecordId);
        enrichWithUserNames(reports);
        return reports;
    }

    @Transactional(readOnly = true)
    public List<ExamReport> getReportsByUser(Long userId) {
        List<ExamReport> reports = examReportRepository.findByUserIdOrderByGeneratedAtDesc(userId);
        enrichWithUserNames(reports);
        return reports;
    }

    @Transactional
    public ExamReport sendReportToUser(Long reportId) {
        ExamReport report = examReportRepository.findById(reportId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Report not found"));
        if (report.getReportStatus() != ReportStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "Only published reports can be sent");
        }
        report.setFileUrl("SENT");
        report = examReportRepository.save(report);
        // Create notification for the user
        jdbc.update(
            "INSERT INTO t_notification (user_id, notification_type, title, content, read_flag, created_at) VALUES (?, 'REPORT', ?, ?, FALSE, NOW())",
            report.getUserId(),
            "体检报告已发送",
            "您的体检报告（编号：" + report.getReportNo() + "）已由医生签发，请前往「体检报告」页面查看。"
        );
        return report;
    }

    @Transactional(readOnly = true)
    public List<ExamReport> getPublishedReports() {
        List<ExamReport> reports = examReportRepository.findByReportStatus(ReportStatus.PUBLISHED);
        enrichWithUserNames(reports);
        return reports;
    }

    // ========== 报告申诉 ==========

    @Transactional
    public ExamReport appealReport(Long reportId, String reason) {
        ExamReport report = examReportRepository.findById(reportId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Report not found"));
        if (report.getReportStatus() != ReportStatus.PUBLISHED
            && !"SENT".equals(report.getFileUrl())) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "Only published/sent reports can be appealed");
        }
        report.appeal();
        // 保存申诉原因到 summary
        report.setSummary((report.getSummary() != null ? report.getSummary() + "\n" : "") + "[申诉]" + reason);
        auditService.record(String.valueOf(report.getUserId()), "REPORT_APPEAL", "t_exam_report", String.valueOf(reportId), "SUCCESS", null, null, null);
        return examReportRepository.save(report);
    }

    private int severity(AbnormalLevel level) {
        return switch (level) {
            case NORMAL -> 0;
            case ABNORMAL -> 1;
            case CRITICAL -> 2;
        };
    }
}
