package com.xixin.health.result;

import com.xixin.health.common.api.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/critical-alerts")
public class CriticalAlertController {

    private final CriticalAlertService criticalAlertService;

    public CriticalAlertController(CriticalAlertService criticalAlertService) {
        this.criticalAlertService = criticalAlertService;
    }

    /**
     * 创建危急值预警
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('EXAM_DOCTOR', 'REVIEW_DOCTOR')")
    public ApiResponse<CriticalAlert> createAlert(@RequestBody @NotNull CreateAlertRequest request) {
        return ApiResponse.success(criticalAlertService.createAlert(
                request.resultId(), request.examRecordId(), request.userId()));
    }

    /**
     * 医生复核危急值
     */
    @PutMapping("/{id}/review")
    @PreAuthorize("hasRole('REVIEW_DOCTOR')")
    public ApiResponse<CriticalAlert> reviewAlert(@PathVariable Long id,
                                                   @RequestBody @NotNull ReviewAlertRequest request) {
        return ApiResponse.success(criticalAlertService.reviewAlert(
                id, request.reviewerId(), request.comment()));
    }

    /**
     * 患者确认收到危急值
     */
    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('REVIEW_DOCTOR', 'EXAM_DOCTOR')")
    public ApiResponse<CriticalAlert> confirmAlert(@PathVariable Long id) {
        return ApiResponse.success(criticalAlertService.confirmAlert(id));
    }

    /**
     * 制定随访计划
     */
    @PutMapping("/{id}/follow-up")
    @PreAuthorize("hasAnyRole('REVIEW_DOCTOR', 'EXAM_DOCTOR')")
    public ApiResponse<CriticalAlert> setFollowUp(@PathVariable Long id,
                                                   @RequestBody @NotNull FollowUpRequest request) {
        return ApiResponse.success(criticalAlertService.setFollowUp(id, request.plan()));
    }

    /**
     * 获取待处理预警
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('REVIEW_DOCTOR', 'SYSTEM_ADMIN')")
    public ApiResponse<List<CriticalAlert>> getPendingAlerts() {
        return ApiResponse.success(criticalAlertService.getPendingAlerts());
    }

    /**
     * 获取用户预警
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('REVIEW_DOCTOR', 'SYSTEM_ADMIN')")
    public ApiResponse<List<CriticalAlert>> getUserAlerts(@PathVariable Long userId) {
        return ApiResponse.success(criticalAlertService.getUserAlerts(userId));
    }

    // ==================== Request DTOs ====================

    public record CreateAlertRequest(
            @NotNull Long resultId,
            Long examRecordId,
            Long userId
    ) {}

    public record ReviewAlertRequest(
            @NotNull Long reviewerId,
            String comment
    ) {}

    public record FollowUpRequest(
            @NotBlank String plan
    ) {}
}
