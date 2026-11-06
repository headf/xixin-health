package com.xixin.health.configuration;

import com.xixin.health.audit.AdminAuditRecorder;
import com.xixin.health.common.api.ApiResponse;
import com.xixin.health.common.error.BusinessException;
import com.xixin.health.common.error.ErrorCode;
import com.xixin.health.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@RequestMapping({"/api/v1/admin/departments", "/api/v1/system/departments"})
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'OPERATIONS_ADMIN')")
@Deprecated /* /api/v1/system/departments 路径已废弃，请统一使用 /api/v1/admin/departments */
public class AdminDepartmentController {

    private final DepartmentRepository departmentRepository;
    private final BranchRepository branchRepository;
    private final AdminAuditRecorder auditRecorder;
    private final SecurityUtils securityUtils;

    public AdminDepartmentController(
        DepartmentRepository departmentRepository,
        BranchRepository branchRepository,
        AdminAuditRecorder auditRecorder,
        SecurityUtils securityUtils
    ) {
        this.departmentRepository = departmentRepository;
        this.branchRepository = branchRepository;
        this.auditRecorder = auditRecorder;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public ApiResponse<List<Department>> departments(
        @RequestParam(required = false) Long branchId,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Boolean enabled
    ) {
        Long effectiveBranchId = branchId != null ? branchId : securityUtils.getCurrentBranchId();
        Long finalBranchId = effectiveBranchId;
        return ApiResponse.success(departmentRepository.findAll().stream()
            .filter(department -> finalBranchId == null || finalBranchId.equals(department.getBranchId()))
            .filter(department -> enabled == null || enabled == department.isEnabled())
            .filter(department -> matches(keyword, department.getDepartmentCode(), department.getDepartmentName()))
            .sorted(Comparator.comparing(Department::getId))
            .toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<Department> department(@PathVariable Long id) {
        return ApiResponse.success(findDepartment(id));
    }

    @PostMapping
    @Transactional
    public ApiResponse<Department> createDepartment(
        @Valid @RequestBody DepartmentRequest request,
        HttpServletRequest webRequest
    ) {
        ensureBranchExists(request.branchId());
        ensureCodeAvailable(request.departmentCode(), null);
        Department saved = departmentRepository.save(Department.create(
            request.branchId(),
            request.departmentCode(),
            request.departmentName(),
            request.location(),
            request.capacityOrDefault(),
            request.fastingRequiredOrDefault(),
            request.enabledOrDefault()
        ));
        auditRecorder.record("CONFIG_CREATE", "t_department", saved.getId(), webRequest);
        return ApiResponse.success(saved);
    }

    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<Department> updateDepartment(
        @PathVariable Long id,
        @Valid @RequestBody DepartmentRequest request,
        HttpServletRequest webRequest
    ) {
        ensureBranchExists(request.branchId());
        ensureCodeAvailable(request.departmentCode(), id);
        Department department = findDepartment(id);
        department.update(
            request.branchId(),
            request.departmentCode(),
            request.departmentName(),
            request.location(),
            request.capacityOrDefault(),
            request.fastingRequiredOrDefault(),
            request.enabledOrDefault()
        );
        auditRecorder.record("CONFIG_UPDATE", "t_department", department.getId(), webRequest);
        return ApiResponse.success(department);
    }

    @PatchMapping("/{id}/status")
    @Transactional
    public ApiResponse<Department> setDepartmentStatus(
        @PathVariable Long id,
        @Valid @RequestBody StatusRequest request,
        HttpServletRequest webRequest
    ) {
        Department department = findDepartment(id);
        department.setEnabled(request.enabled());
        auditRecorder.record("CONFIG_STATUS", "t_department", department.getId(), webRequest);
        return ApiResponse.success(department);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Department> deleteDepartment(@PathVariable Long id, HttpServletRequest webRequest) {
        Department department = findDepartment(id);
        department.setEnabled(false);
        auditRecorder.record("CONFIG_DISABLE", "t_department", department.getId(), webRequest);
        return ApiResponse.success(department);
    }

    private Department findDepartment(Long id) {
        return departmentRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Department not found"));
    }

    private void ensureBranchExists(Long branchId) {
        if (!branchRepository.existsById(branchId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Branch not found");
        }
    }

    private void ensureCodeAvailable(String code, Long currentId) {
        boolean duplicate = departmentRepository.findAll().stream()
            .anyMatch(department -> department.getDepartmentCode().equals(code)
                && !Objects.equals(department.getId(), currentId));
        if (duplicate) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "Department code already exists");
        }
    }

    private boolean matches(String keyword, String code, String name) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String normalized = keyword.trim().toLowerCase();
        return code.toLowerCase().contains(normalized) || name.toLowerCase().contains(normalized);
    }

    public record DepartmentRequest(
        @NotNull Long branchId,
        @NotBlank String departmentCode,
        @NotBlank String departmentName,
        String location,
        Integer capacity,
        Boolean fastingRequired,
        Boolean enabled
    ) {
        int capacityOrDefault() {
            return Objects.requireNonNullElse(capacity, 0);
        }

        boolean fastingRequiredOrDefault() {
            return Objects.requireNonNullElse(fastingRequired, false);
        }

        boolean enabledOrDefault() {
            return Objects.requireNonNullElse(enabled, true);
        }
    }

    public record StatusRequest(boolean enabled) {
    }
}
