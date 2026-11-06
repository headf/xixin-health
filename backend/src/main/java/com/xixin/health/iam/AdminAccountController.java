package com.xixin.health.iam;

import com.xixin.health.audit.AdminAuditRecorder;
import com.xixin.health.common.api.ApiResponse;
import com.xixin.health.common.error.BusinessException;
import com.xixin.health.common.error.ErrorCode;
import com.xixin.health.iam.dto.AdminAccountResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping({"/api/v1/admin/accounts", "/api/v1/system/accounts"})
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'OPERATIONS_ADMIN')")
@Deprecated /* /api/v1/system/accounts 已废弃，请统一使用 /api/v1/admin/accounts */
public class AdminAccountController {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminAuditRecorder auditRecorder;

    public AdminAccountController(
        UserAccountRepository userAccountRepository,
        RoleRepository roleRepository,
        PasswordEncoder passwordEncoder,
        AdminAuditRecorder auditRecorder
    ) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditRecorder = auditRecorder;
    }

    @GetMapping
    public ApiResponse<Page<AdminAccountResponse>> accounts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) Long branchId) {
        if (branchId != null) {
            return ApiResponse.success(
                userAccountRepository.findByBranchIdAndDeletedFalse(branchId, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id")))
                    .map(AdminAccountResponse::from));
        }
        return ApiResponse.success(
            userAccountRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id")))
                .map(AdminAccountResponse::from));
    }

    @PostMapping
    @Transactional
    public ApiResponse<AdminAccountResponse> createAccount(@Valid @RequestBody AccountRequest request, HttpServletRequest webRequest) {
        if (request.password() == null || request.password().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Password is required");
        }
        if (userAccountRepository.existsByPhone(request.phone())) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "Phone already exists");
        }
        UserAccount account = new UserAccount(
            request.phone(),
            request.email(),
            request.displayName(),
            passwordEncoder.encode(request.password())
        );
        account.replaceRoles(resolveRoles(request.roleCodes()));
        if (request.branchId() != null) account.setBranchId(request.branchId());
        UserAccount saved = userAccountRepository.save(account);
        auditRecorder.record("IAM_CREATE", "sys_user", saved.getId(), webRequest);
        return ApiResponse.success(AdminAccountResponse.from(saved));
    }

    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<AdminAccountResponse> updateAccount(
        @PathVariable Long id,
        @Valid @RequestBody AccountRequest request,
        HttpServletRequest webRequest
    ) {
        UserAccount account = findAccount(id);
        account.updateAccount(request.phone(), request.email(), request.displayName());
        account.replaceRoles(resolveRoles(request.roleCodes()));
        if (request.password() != null && !request.password().isBlank()) {
            account.changePassword(passwordEncoder.encode(request.password()));
        }
        if (request.branchId() != null) account.setBranchId(request.branchId());
        auditRecorder.record("IAM_UPDATE", "sys_user", account.getId(), webRequest);
        return ApiResponse.success(AdminAccountResponse.from(account));
    }

    @PatchMapping("/{id}/status")
    @Transactional
    public ApiResponse<AdminAccountResponse> setAccountStatus(
        @PathVariable Long id,
        @Valid @RequestBody StatusRequest request,
        HttpServletRequest webRequest
    ) {
        UserAccount account = findAccount(id);
        account.setEnabled(request.enabled());
        auditRecorder.record("IAM_STATUS", "sys_user", account.getId(), webRequest);
        return ApiResponse.success(AdminAccountResponse.from(account));
    }

    @PatchMapping("/{id}/password")
    @Transactional
    public ApiResponse<AdminAccountResponse> changePassword(
        @PathVariable Long id,
        @Valid @RequestBody PasswordRequest request,
        HttpServletRequest webRequest
    ) {
        UserAccount account = findAccount(id);
        account.changePassword(passwordEncoder.encode(request.password()));
        auditRecorder.record("IAM_PASSWORD_RESET", "sys_user", account.getId(), webRequest);
        return ApiResponse.success(AdminAccountResponse.from(account));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> deleteAccount(@PathVariable Long id, HttpServletRequest webRequest) {
        UserAccount account = findAccount(id);
        account.markDeleted();
        auditRecorder.record("IAM_DELETE", "sys_user", account.getId(), webRequest);
        return ApiResponse.success(null);
    }

    private UserAccount findAccount(Long id) {
        return userAccountRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Account not found"));
    }

    private List<Role> resolveRoles(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return List.of();
        }
        Set<String> codes = roleCodes.stream()
            .filter(code -> code != null && !code.isBlank())
            .collect(Collectors.toSet());
        List<Role> roles = roleRepository.findByCodeIn(codes).stream()
            .filter(role -> !role.isDeleted())
            .toList();
        if (roles.size() != codes.size()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Role not found");
        }
        return roles;
    }

    public record AccountRequest(
        @NotBlank String phone,
        String email,
        @NotBlank String displayName,
        String password,
        List<String> roleCodes,
        Long branchId
    ) {
    }

    public record StatusRequest(boolean enabled) {
    }

    public record PasswordRequest(@NotBlank String password) {
    }
}
