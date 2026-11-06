package com.xixin.health.iam;

import com.xixin.health.audit.AdminAuditRecorder;
import com.xixin.health.common.api.ApiResponse;
import com.xixin.health.common.error.BusinessException;
import com.xixin.health.common.error.ErrorCode;
import com.xixin.health.iam.dto.RoleResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/v1/admin/roles", "/api/v1/system/roles"})
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'OPERATIONS_ADMIN')")
@Deprecated /* /api/v1/system/roles 已废弃，请统一使用 /api/v1/admin/roles */
public class RoleController {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserAccountRepository userAccountRepository;
    private final AdminAuditRecorder auditRecorder;

    public RoleController(
        RoleRepository roleRepository,
        PermissionRepository permissionRepository,
        UserAccountRepository userAccountRepository,
        AdminAuditRecorder auditRecorder
    ) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userAccountRepository = userAccountRepository;
        this.auditRecorder = auditRecorder;
    }

    @GetMapping
    public ApiResponse<List<RoleResponse>> roles() {
        return ApiResponse.success(roleRepository.findAll().stream()
            .filter(role -> !role.isDeleted())
            .map(RoleResponse::from)
            .toList());
    }

    @PostMapping
    @Transactional
    public ApiResponse<RoleResponse> createRole(@Valid @RequestBody RoleRequest request, HttpServletRequest webRequest) {
        ensureRoleCodeAvailable(request.code(), null);
        Role role = new Role(request.code(), request.name(), request.roleType(), false);
        role.replacePermissions(resolvePermissions(request.permissionCodes()));
        Role saved = roleRepository.save(role);
        auditRecorder.record("IAM_CREATE", "sys_role", saved.getId(), webRequest);
        return ApiResponse.success(RoleResponse.from(saved));
    }

    @PutMapping("/{code}")
    @Transactional
    public ApiResponse<RoleResponse> updateRole(
        @PathVariable String code,
        @Valid @RequestBody RoleRequest request,
        HttpServletRequest webRequest
    ) {
        Role role = findRoleByCode(code);
        if (role.isSystemBuiltIn()) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN, "Built-in roles cannot be modified");
        }
        ensureRoleCodeAvailable(request.code(), role.getId());
        role.update(request.code(), request.name(), request.roleType());
        role.replacePermissions(resolvePermissions(request.permissionCodes()));
        auditRecorder.record("IAM_UPDATE", "sys_role", role.getId(), webRequest);
        return ApiResponse.success(RoleResponse.from(role));
    }

    @DeleteMapping("/{code}")
    @Transactional
    public ApiResponse<Void> deleteRole(@PathVariable String code, HttpServletRequest webRequest) {
        Role role = findRoleByCode(code);
        if (role.isSystemBuiltIn()) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN, "Built-in roles cannot be deleted");
        }
        if (userAccountRepository.existsByRoles_IdAndDeletedFalse(role.getId())) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "Role is assigned to active accounts");
        }
        role.markDeleted();
        auditRecorder.record("IAM_DELETE", "sys_role", role.getId(), webRequest);
        return ApiResponse.success(null);
    }

    private Role findRoleByCode(String code) {
        return roleRepository.findByCode(code)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Role not found: " + code));
    }

    private List<Permission> resolvePermissions(List<String> permissionCodes) {
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            return List.of();
        }
        Set<String> codes = permissionCodes.stream()
            .filter(code -> code != null && !code.isBlank())
            .collect(Collectors.toSet());
        List<Permission> permissions = permissionRepository.findByCodeIn(codes);
        if (permissions.size() != codes.size()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Permission not found");
        }
        return permissions;
    }

    private void ensureRoleCodeAvailable(String code, Long currentId) {
        roleRepository.findByCode(code)
            .filter(role -> !role.getId().equals(currentId))
            .ifPresent(role -> {
                throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "Role code already exists");
            });
    }

    public record RoleRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotBlank String roleType,
        List<String> permissionCodes
    ) {
    }
}
