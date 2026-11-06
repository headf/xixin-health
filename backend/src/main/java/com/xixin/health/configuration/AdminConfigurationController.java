package com.xixin.health.configuration;

import com.xixin.health.audit.AdminAuditRecorder;
import com.xixin.health.common.api.ApiResponse;
import com.xixin.health.common.error.BusinessException;
import com.xixin.health.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'OPERATIONS_ADMIN')")
@Deprecated /* 本 Controller 同时暴露 /api/v1/admin/* 和 /api/v1/system/* 两套路径，后者已废弃，请统一使用 /api/v1/admin/* */
public class AdminConfigurationController {

    private final ExamItemRepository examItemRepository;
    private final ExamPackageRepository examPackageRepository;
    private final ExamPackageItemRepository examPackageItemRepository;
    private final BranchRepository branchRepository;
    private final DepartmentRepository departmentRepository;
    private final BusinessRuleRepository businessRuleRepository;
    private final AdminAuditRecorder auditRecorder;

    public AdminConfigurationController(
        ExamItemRepository examItemRepository,
        ExamPackageRepository examPackageRepository,
        ExamPackageItemRepository examPackageItemRepository,
        BranchRepository branchRepository,
        DepartmentRepository departmentRepository,
        BusinessRuleRepository businessRuleRepository,
        AdminAuditRecorder auditRecorder
    ) {
        this.examItemRepository = examItemRepository;
        this.examPackageRepository = examPackageRepository;
        this.examPackageItemRepository = examPackageItemRepository;
        this.branchRepository = branchRepository;
        this.departmentRepository = departmentRepository;
        this.businessRuleRepository = businessRuleRepository;
        this.auditRecorder = auditRecorder;
    }

    @GetMapping({"/api/v1/admin/exam-items", "/api/v1/system/exam-items"})
    public ApiResponse<List<ExamItemVO>> examItems() {
        List<ExamItem> items = examItemRepository.findAll();
        Map<Long, String> deptMap = departmentRepository.findAll().stream()
            .collect(Collectors.toMap(Department::getId, Department::getDepartmentName));
        return ApiResponse.success(items.stream().map(item -> new ExamItemVO(
            item.getId(), item.getItemCode(), item.getItemName(),
            item.getDepartmentId(),
            deptMap.getOrDefault(item.getDepartmentId(), String.valueOf(item.getDepartmentId())),
            item.getUnit(), item.getReferenceMin(), item.getReferenceMax(),
            item.getCriticalMin(), item.getCriticalMax(),
            item.getEnabled(), item.getReviewStatus()
        )).toList());
    }

    @PostMapping({"/api/v1/admin/exam-items", "/api/v1/system/exam-items"})
    @Transactional
    public ApiResponse<ExamItem> createExamItem(@Valid @RequestBody ExamItemRequest request, HttpServletRequest webRequest) {
        ExamItem item = ExamItem.create(
            request.itemCode(),
            request.itemName(),
            request.departmentId(),
            request.unit(),
            request.referenceMin(),
            request.referenceMax(),
            request.criticalMin(),
            request.criticalMax(),
            request.enabledOrDefault()
        );
        ExamItem saved = examItemRepository.save(item);
        auditRecorder.record("CONFIG_CREATE", "t_exam_item", saved.getId(), webRequest);
        return ApiResponse.success(saved);
    }

    @PutMapping({"/api/v1/admin/exam-items/{id}", "/api/v1/system/exam-items/{id}"})
    @Transactional
    public ApiResponse<ExamItem> updateExamItem(
        @PathVariable Long id,
        @Valid @RequestBody ExamItemRequest request,
        HttpServletRequest webRequest
    ) {
        ExamItem item = findExamItem(id);
        boolean medicalReferenceChanged = item.hasMedicalReferenceChange(
            request.referenceMin(),
            request.referenceMax(),
            request.criticalMin(),
            request.criticalMax()
        );
        item.update(
            request.itemCode(),
            request.itemName(),
            request.departmentId(),
            request.unit(),
            request.referenceMin(),
            request.referenceMax(),
            request.criticalMin(),
            request.criticalMax(),
            request.enabledOrDefault()
        );
        if (medicalReferenceChanged) {
            item.markPendingReview();
        }
        auditRecorder.record("CONFIG_UPDATE", "t_exam_item", item.getId(), webRequest);
        return ApiResponse.success(item);
    }

    @PatchMapping({"/api/v1/admin/exam-items/{id}/status", "/api/v1/system/exam-items/{id}/status"})
    @Transactional
    public ApiResponse<ExamItem> setExamItemStatus(
        @PathVariable Long id,
        @Valid @RequestBody StatusRequest request,
        HttpServletRequest webRequest
    ) {
        ExamItem item = findExamItem(id);
        item.setEnabled(request.enabled());
        auditRecorder.record("CONFIG_STATUS", "t_exam_item", item.getId(), webRequest);
        return ApiResponse.success(item);
    }

    @DeleteMapping({"/api/v1/admin/exam-items/{id}", "/api/v1/system/exam-items/{id}"})
    @Transactional
    public ApiResponse<ExamItem> deleteExamItem(@PathVariable Long id, HttpServletRequest webRequest) {
        ExamItem item = findExamItem(id);
        item.setEnabled(false);
        auditRecorder.record("CONFIG_DISABLE", "t_exam_item", item.getId(), webRequest);
        return ApiResponse.success(item);
    }

    @GetMapping({"/api/v1/admin/packages", "/api/v1/system/packages"})
    public ApiResponse<List<PackageResponse>> packages() {
        return ApiResponse.success(examPackageRepository.findAll().stream()
            .map(this::packageResponse)
            .toList());
    }

    @PostMapping({"/api/v1/admin/packages", "/api/v1/system/packages"})
    @Transactional
    public ApiResponse<PackageResponse> createPackage(@Valid @RequestBody PackageRequest request, HttpServletRequest webRequest) {
        ExamPackage examPackage = ExamPackage.create(
            request.packageCode(),
            request.packageName(),
            request.category(),
            request.genderLimit(),
            request.ageRange(),
            request.priceCent(),
            request.enabledOrDefault()
        );
        ExamPackage saved = examPackageRepository.save(examPackage);
        replacePackageItems(saved.getId(), request.itemIds());
        auditRecorder.record("CONFIG_CREATE", "t_exam_package", saved.getId(), webRequest);
        return ApiResponse.success(packageResponse(saved));
    }

    @PutMapping({"/api/v1/admin/packages/{id}", "/api/v1/system/packages/{id}"})
    @Transactional
    public ApiResponse<PackageResponse> updatePackage(
        @PathVariable Long id,
        @Valid @RequestBody PackageRequest request,
        HttpServletRequest webRequest
    ) {
        ExamPackage examPackage = findPackage(id);
        examPackage.update(
            request.packageCode(),
            request.packageName(),
            request.category(),
            request.genderLimit(),
            request.ageRange(),
            request.priceCent(),
            request.enabledOrDefault()
        );
        replacePackageItems(id, request.itemIds());
        auditRecorder.record("CONFIG_UPDATE", "t_exam_package", examPackage.getId(), webRequest);
        return ApiResponse.success(packageResponse(examPackage));
    }

    @PatchMapping({"/api/v1/admin/packages/{id}/status", "/api/v1/system/packages/{id}/status"})
    @Transactional
    public ApiResponse<PackageResponse> setPackageStatus(
        @PathVariable Long id,
        @Valid @RequestBody StatusRequest request,
        HttpServletRequest webRequest
    ) {
        ExamPackage examPackage = findPackage(id);
        examPackage.setEnabled(request.enabled());
        auditRecorder.record("CONFIG_STATUS", "t_exam_package", examPackage.getId(), webRequest);
        return ApiResponse.success(packageResponse(examPackage));
    }

    @DeleteMapping({"/api/v1/admin/packages/{id}", "/api/v1/system/packages/{id}"})
    @Transactional
    public ApiResponse<PackageResponse> deletePackage(@PathVariable Long id, HttpServletRequest webRequest) {
        ExamPackage examPackage = findPackage(id);
        examPackage.setEnabled(false);
        auditRecorder.record("CONFIG_DISABLE", "t_exam_package", examPackage.getId(), webRequest);
        return ApiResponse.success(packageResponse(examPackage));
    }

    @GetMapping({"/api/v1/admin/branches", "/api/v1/system/branches"})
    @Cacheable("branches")
    public ApiResponse<List<Branch>> branches() {
        return ApiResponse.success(branchRepository.findAll());
    }

    @PostMapping({"/api/v1/admin/branches", "/api/v1/system/branches"})
    @Transactional
    public ApiResponse<Branch> createBranch(@Valid @RequestBody BranchRequest request, HttpServletRequest webRequest) {
        Branch branch = Branch.create(
            request.branchCode(),
            request.branchName(),
            request.address(),
            request.servicePhone(),
            request.enabledOrDefault()
        );
        Branch saved = branchRepository.save(branch);
        auditRecorder.record("CONFIG_CREATE", "t_branch", saved.getId(), webRequest);
        return ApiResponse.success(saved);
    }

    @PutMapping({"/api/v1/admin/branches/{id}", "/api/v1/system/branches/{id}"})
    @Transactional
    public ApiResponse<Branch> updateBranch(
        @PathVariable Long id,
        @Valid @RequestBody BranchRequest request,
        HttpServletRequest webRequest
    ) {
        Branch branch = findBranch(id);
        branch.update(
            request.branchCode(),
            request.branchName(),
            request.address(),
            request.servicePhone(),
            request.enabledOrDefault()
        );
        auditRecorder.record("CONFIG_UPDATE", "t_branch", branch.getId(), webRequest);
        return ApiResponse.success(branch);
    }

    @PatchMapping({"/api/v1/admin/branches/{id}/status", "/api/v1/system/branches/{id}/status"})
    @Transactional
    public ApiResponse<Branch> setBranchStatus(
        @PathVariable Long id,
        @Valid @RequestBody StatusRequest request,
        HttpServletRequest webRequest
    ) {
        Branch branch = findBranch(id);
        branch.setEnabled(request.enabled());
        auditRecorder.record("CONFIG_STATUS", "t_branch", branch.getId(), webRequest);
        return ApiResponse.success(branch);
    }

    @DeleteMapping({"/api/v1/admin/branches/{id}", "/api/v1/system/branches/{id}"})
    @Transactional
    public ApiResponse<Branch> deleteBranch(@PathVariable Long id, HttpServletRequest webRequest) {
        Branch branch = findBranch(id);
        branch.setEnabled(false);
        auditRecorder.record("CONFIG_DISABLE", "t_branch", branch.getId(), webRequest);
        return ApiResponse.success(branch);
    }

    @GetMapping({"/api/v1/admin/rules", "/api/v1/system/rules"})
    public ApiResponse<List<BusinessRule>> rules() {
        return ApiResponse.success(businessRuleRepository.findAll());
    }

    @PostMapping({"/api/v1/admin/rules", "/api/v1/system/rules"})
    @Transactional
    public ApiResponse<BusinessRule> createRule(@Valid @RequestBody RuleRequest request, HttpServletRequest webRequest) {
        BusinessRule rule = BusinessRule.create(
            request.ruleCode(),
            request.ruleName(),
            request.ruleType(),
            request.ruleContent(),
            request.enabledOrDefault()
        );
        BusinessRule saved = businessRuleRepository.save(rule);
        auditRecorder.record("CONFIG_CREATE", "t_business_rule", saved.getId(), webRequest);
        return ApiResponse.success(saved);
    }

    @PutMapping({"/api/v1/admin/rules/{id}", "/api/v1/system/rules/{id}"})
    @Transactional
    public ApiResponse<BusinessRule> updateRule(
        @PathVariable Long id,
        @Valid @RequestBody RuleRequest request,
        HttpServletRequest webRequest
    ) {
        BusinessRule rule = findRule(id);
        rule.update(
            request.ruleCode(),
            request.ruleName(),
            request.ruleType(),
            request.ruleContent(),
            request.enabledOrDefault()
        );
        if (request.ruleType().startsWith("MEDICAL_")) {
            rule.markPendingReview();
        }
        auditRecorder.record("CONFIG_UPDATE", "t_business_rule", rule.getId(), webRequest);
        return ApiResponse.success(rule);
    }

    @PatchMapping({"/api/v1/admin/rules/{id}/status", "/api/v1/system/rules/{id}/status"})
    @Transactional
    public ApiResponse<BusinessRule> setRuleStatus(
        @PathVariable Long id,
        @Valid @RequestBody StatusRequest request,
        HttpServletRequest webRequest
    ) {
        BusinessRule rule = findRule(id);
        rule.setEnabled(request.enabled());
        auditRecorder.record("CONFIG_STATUS", "t_business_rule", rule.getId(), webRequest);
        return ApiResponse.success(rule);
    }

    @DeleteMapping({"/api/v1/admin/rules/{id}", "/api/v1/system/rules/{id}"})
    @Transactional
    public ApiResponse<BusinessRule> deleteRule(@PathVariable Long id, HttpServletRequest webRequest) {
        BusinessRule rule = findRule(id);
        rule.setEnabled(false);
        auditRecorder.record("CONFIG_DISABLE", "t_business_rule", rule.getId(), webRequest);
        return ApiResponse.success(rule);
    }

    private void replacePackageItems(Long packageId, List<Long> itemIds) {
        examPackageItemRepository.deleteByPackageId(packageId);
        if (itemIds == null || itemIds.isEmpty()) {
            return;
        }
        List<ExamPackageItem> packageItems = IntStream.range(0, itemIds.size())
            .mapToObj(index -> ExamPackageItem.of(packageId, itemIds.get(index), index + 1))
            .toList();
        examPackageItemRepository.saveAll(packageItems);
    }

    private PackageResponse packageResponse(ExamPackage examPackage) {
        List<Long> itemIds = examPackageItemRepository.findByPackageIdOrderBySortNoAsc(examPackage.getId()).stream()
            .map(ExamPackageItem::getItemId)
            .toList();
        return new PackageResponse(
            examPackage.getId(),
            examPackage.getPackageCode(),
            examPackage.getPackageName(),
            examPackage.getCategory(),
            examPackage.getGenderLimit(),
            examPackage.getAgeRange(),
            examPackage.getPriceCent(),
            examPackage.isEnabled(),
            itemIds
        );
    }

    private ExamItem findExamItem(Long id) {
        return examItemRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Exam item not found"));
    }

    private ExamPackage findPackage(Long id) {
        return examPackageRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Package not found"));
    }

    private Branch findBranch(Long id) {
        return branchRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Branch not found"));
    }

    private BusinessRule findRule(Long id) {
        return businessRuleRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Rule not found"));
    }

    public record ExamItemVO(
        Long id,
        String itemCode,
        String itemName,
        Long departmentId,
        String departmentName,
        String unit,
        BigDecimal referenceMin,
        BigDecimal referenceMax,
        BigDecimal criticalMin,
        BigDecimal criticalMax,
        boolean enabled,
        String reviewStatus
    ) {}

    public record ExamItemRequest(
        @NotBlank String itemCode,
        @NotBlank String itemName,
        Long departmentId,
        String unit,
        BigDecimal referenceMin,
        BigDecimal referenceMax,
        BigDecimal criticalMin,
        BigDecimal criticalMax,
        Boolean enabled
    ) {
        boolean enabledOrDefault() {
            return Objects.requireNonNullElse(enabled, true);
        }
    }

    public record PackageRequest(
        @NotBlank String packageCode,
        @NotBlank String packageName,
        String category,
        String genderLimit,
        String ageRange,
        @NotNull Integer priceCent,
        Boolean enabled,
        List<Long> itemIds
    ) {
        boolean enabledOrDefault() {
            return Objects.requireNonNullElse(enabled, true);
        }
    }

    public record PackageResponse(
        Long id,
        String packageCode,
        String packageName,
        String category,
        String genderLimit,
        String ageRange,
        int priceCent,
        boolean enabled,
        List<Long> itemIds
    ) {
    }

    public record BranchRequest(
        @NotBlank String branchCode,
        @NotBlank String branchName,
        String address,
        String servicePhone,
        Boolean enabled
    ) {
        boolean enabledOrDefault() {
            return Objects.requireNonNullElse(enabled, true);
        }
    }

    public record RuleRequest(
        @NotBlank String ruleCode,
        @NotBlank String ruleName,
        @NotBlank String ruleType,
        @NotBlank String ruleContent,
        Boolean enabled
    ) {
        boolean enabledOrDefault() {
            return Objects.requireNonNullElse(enabled, true);
        }
    }

    public record StatusRequest(boolean enabled) {
    }
}
