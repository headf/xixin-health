package com.xixin.health.management;

import com.xixin.health.audit.AdminAuditRecorder;
import com.xixin.health.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Deprecated /* 同时暴露 /api/v1/admin/health-management 和 /api/v1/medical/health-management，后者已废弃，请统一使用 /api/v1/admin/health-management */
public class HealthManagementController {

    private final HealthManagementService healthManagementService;
    private final AdminAuditRecorder auditRecorder;

    public HealthManagementController(
        HealthManagementService healthManagementService,
        AdminAuditRecorder auditRecorder
    ) {
        this.healthManagementService = healthManagementService;
        this.auditRecorder = auditRecorder;
    }

    @GetMapping({"/api/v1/admin/health-management", "/api/v1/medical/health-management"})
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'OPERATIONS_ADMIN', 'REVIEW_DOCTOR')")
    public ApiResponse<List<HealthManagementService.HealthManagementVO>> list() {
        return ApiResponse.success(healthManagementService.listWithUserInfo());
    }

    @PostMapping({"/api/v1/admin/health-management", "/api/v1/medical/health-management"})
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'OPERATIONS_ADMIN', 'REVIEW_DOCTOR')")
    public ApiResponse<HealthManagement> create(
        @Valid @RequestBody CreateRequest request,
        HttpServletRequest webRequest
    ) {
        HealthManagement management = healthManagementService.create(
            request.userId(), request.riskLevel(),
            request.managementStatus(), request.followUpPlan(), request.interventionPlan()
        );
        auditRecorder.record("HEALTH_CREATE", "t_health_management", management.getId(), webRequest);
        return ApiResponse.success(management);
    }

    @PostMapping({"/api/v1/admin/health-management/{id}/follow-up", "/api/v1/medical/health-management/{id}/follow-up"})
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'OPERATIONS_ADMIN', 'REVIEW_DOCTOR')")
    public ApiResponse<HealthManagement> planFollowUp(
        @PathVariable Long id,
        @Valid @RequestBody FollowUpRequest request,
        HttpServletRequest webRequest
    ) {
        HealthManagement management = healthManagementService.planFollowUp(
            id,
            request.followUpPlan(),
            request.interventionPlan()
        );
        auditRecorder.record("HEALTH_FOLLOW_UP", "t_health_management", id, webRequest);
        return ApiResponse.success(management);
    }

    @PutMapping({"/api/v1/admin/health-management/{id}", "/api/v1/medical/health-management/{id}"})
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'OPERATIONS_ADMIN', 'REVIEW_DOCTOR')")
    public ApiResponse<HealthManagement> update(
        @PathVariable Long id,
        @Valid @RequestBody UpdateRequest request
    ) {
        return ApiResponse.success(healthManagementService.update(id, request.riskLevel(), request.managementStatus(),
            request.followUpPlan(), request.interventionPlan()));
    }

    public record FollowUpRequest(
        @NotBlank String followUpPlan,
        @NotBlank String interventionPlan
    ) {
    }

    public record CreateRequest(
        @NotNull Long userId,
        @NotBlank String riskLevel,
        String managementStatus,
        String followUpPlan,
        String interventionPlan
    ) {
    }

    public record UpdateRequest(
        String riskLevel,
        String managementStatus,
        String followUpPlan,
        String interventionPlan
    ) {
    }
}
