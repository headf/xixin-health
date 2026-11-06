package com.xixin.health.configuration;

import com.xixin.health.audit.AdminAuditRecorder;
import com.xixin.health.common.api.ApiResponse;
import com.xixin.health.common.error.BusinessException;
import com.xixin.health.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping({"/api/v1/admin/report-templates", "/api/v1/system/report-templates"})
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'OPERATIONS_ADMIN')")
@Deprecated /* /api/v1/system/report-templates 已废弃，请统一使用 /api/v1/admin/report-templates */
public class AdminReportTemplateController {

    private final ReportTemplateRepository reportTemplateRepository;
    private final ExamPackageRepository examPackageRepository;
    private final AdminAuditRecorder auditRecorder;

    public AdminReportTemplateController(
        ReportTemplateRepository reportTemplateRepository,
        ExamPackageRepository examPackageRepository,
        AdminAuditRecorder auditRecorder
    ) {
        this.reportTemplateRepository = reportTemplateRepository;
        this.examPackageRepository = examPackageRepository;
        this.auditRecorder = auditRecorder;
    }

    @GetMapping
    public ApiResponse<List<ReportTemplate>> templates(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Boolean enabled
    ) {
        return ApiResponse.success(reportTemplateRepository.findAll().stream()
            .filter(template -> enabled == null || enabled == template.isEnabled())
            .filter(template -> matches(keyword, template.getTemplateCode(), template.getTemplateName()))
            .sorted(Comparator.comparing(ReportTemplate::getId))
            .toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<ReportTemplate> template(@PathVariable Long id) {
        return ApiResponse.success(findTemplate(id));
    }

    @GetMapping("/{id}/preview")
    public ApiResponse<PreviewResponse> preview(
        @PathVariable Long id,
        @RequestParam(required = false) String summary,
        @RequestParam(required = false) String advice,
        @RequestParam(required = false) String reviewer
    ) {
        ReportTemplate template = findTemplate(id);
        return ApiResponse.success(new PreviewResponse(template.preview(summary, advice, reviewer)));
    }

    @PostMapping
    @Transactional
    public ApiResponse<ReportTemplate> createTemplate(
        @Valid @RequestBody TemplateRequest request,
        HttpServletRequest webRequest
    ) {
        ensurePackageExists(request.packageId());
        ensureCodeAvailable(request.templateCode(), null);
        ReportTemplate saved = reportTemplateRepository.save(ReportTemplate.create(
            request.templateCode(),
            request.templateName(),
            request.packageId(),
            request.templateContent(),
            request.enabledOrDefault()
        ));
        auditRecorder.record("CONFIG_CREATE", "t_report_template", saved.getId(), webRequest);
        return ApiResponse.success(saved);
    }

    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<ReportTemplate> updateTemplate(
        @PathVariable Long id,
        @Valid @RequestBody TemplateRequest request,
        HttpServletRequest webRequest
    ) {
        ensurePackageExists(request.packageId());
        ensureCodeAvailable(request.templateCode(), id);
        ReportTemplate template = findTemplate(id);
        template.update(
            request.templateCode(),
            request.templateName(),
            request.packageId(),
            request.templateContent(),
            request.enabledOrDefault()
        );
        template.markPendingReview();
        auditRecorder.record("CONFIG_UPDATE", "t_report_template", template.getId(), webRequest);
        return ApiResponse.success(template);
    }

    @PatchMapping("/{id}/status")
    @Transactional
    public ApiResponse<ReportTemplate> setTemplateStatus(
        @PathVariable Long id,
        @Valid @RequestBody StatusRequest request,
        HttpServletRequest webRequest
    ) {
        ReportTemplate template = findTemplate(id);
        template.setEnabled(request.enabled());
        auditRecorder.record("CONFIG_STATUS", "t_report_template", template.getId(), webRequest);
        return ApiResponse.success(template);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<ReportTemplate> deleteTemplate(@PathVariable Long id, HttpServletRequest webRequest) {
        ReportTemplate template = findTemplate(id);
        reportTemplateRepository.delete(template);
        auditRecorder.record("CONFIG_DELETE", "t_report_template", template.getId(), webRequest);
        return ApiResponse.success(template);
    }

    private ReportTemplate findTemplate(Long id) {
        return reportTemplateRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Report template not found"));
    }

    private void ensurePackageExists(Long packageId) {
        if (packageId != null && !examPackageRepository.existsById(packageId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Package not found");
        }
    }

    private void ensureCodeAvailable(String code, Long currentId) {
        boolean duplicate = reportTemplateRepository.findAll().stream()
            .anyMatch(template -> template.getTemplateCode().equals(code)
                && !Objects.equals(template.getId(), currentId));
        if (duplicate) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "Template code already exists");
        }
    }

    private boolean matches(String keyword, String code, String name) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String normalized = keyword.trim().toLowerCase();
        return code.toLowerCase().contains(normalized) || name.toLowerCase().contains(normalized);
    }

    public record TemplateRequest(
        @NotBlank String templateCode,
        @NotBlank String templateName,
        Long packageId,
        @NotBlank String templateContent,
        Boolean enabled
    ) {
        boolean enabledOrDefault() {
            return Objects.requireNonNullElse(enabled, true);
        }
    }

    public record PreviewResponse(String previewText) {
    }

    public record StatusRequest(boolean enabled) {
    }
}
