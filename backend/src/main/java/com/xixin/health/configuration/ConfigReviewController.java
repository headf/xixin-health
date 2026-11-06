package com.xixin.health.configuration;

import com.xixin.health.audit.AdminAuditRecorder;
import com.xixin.health.common.api.ApiResponse;
import com.xixin.health.common.error.BusinessException;
import com.xixin.health.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'OPERATIONS_ADMIN')")
public class ConfigReviewController {

    private final ExamItemRepository examItemRepository;
    private final BusinessRuleRepository businessRuleRepository;
    private final ReportTemplateRepository reportTemplateRepository;
    private final AdminAuditRecorder auditRecorder;

    public ConfigReviewController(
        ExamItemRepository examItemRepository,
        BusinessRuleRepository businessRuleRepository,
        ReportTemplateRepository reportTemplateRepository,
        AdminAuditRecorder auditRecorder
    ) {
        this.examItemRepository = examItemRepository;
        this.businessRuleRepository = businessRuleRepository;
        this.reportTemplateRepository = reportTemplateRepository;
        this.auditRecorder = auditRecorder;
    }

    @PostMapping("/api/v1/admin/config-reviews/exam-items/{id}")
    @Transactional
    public ApiResponse<ExamItem> reviewExamItem(
        @PathVariable Long id,
        @Valid @RequestBody ReviewRequest request,
        HttpServletRequest webRequest
    ) {
        ExamItem item = examItemRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Exam item not found"));
        if (request.approved()) {
            item.approveReview(request.reviewerId());
        } else {
            item.rejectReview(request.reviewerId());
        }
        auditRecorder.record("CONFIG_REVIEW", "t_exam_item", id, webRequest);
        return ApiResponse.success(item);
    }

    @PostMapping("/api/v1/admin/config-reviews/rules/{id}")
    @Transactional
    public ApiResponse<BusinessRule> reviewRule(
        @PathVariable Long id,
        @Valid @RequestBody ReviewRequest request,
        HttpServletRequest webRequest
    ) {
        BusinessRule rule = businessRuleRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Rule not found"));
        if (request.approved()) {
            rule.approveReview(request.reviewerId());
        } else {
            rule.rejectReview(request.reviewerId());
        }
        auditRecorder.record("CONFIG_REVIEW", "t_business_rule", id, webRequest);
        return ApiResponse.success(rule);
    }

    @PostMapping("/api/v1/admin/config-reviews/report-templates/{id}")
    @Transactional
    public ApiResponse<ReportTemplate> reviewReportTemplate(
        @PathVariable Long id,
        @Valid @RequestBody ReviewRequest request,
        HttpServletRequest webRequest
    ) {
        ReportTemplate template = reportTemplateRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Report template not found"));
        if (request.approved()) {
            template.approveReview(request.reviewerId());
        } else {
            template.rejectReview(request.reviewerId());
        }
        auditRecorder.record("CONFIG_REVIEW", "t_report_template", id, webRequest);
        return ApiResponse.success(template);
    }

    public record ReviewRequest(
        @NotNull Long reviewerId,
        boolean approved,
        String comment
    ) {
    }
}
