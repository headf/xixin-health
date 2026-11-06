package com.xixin.health.management;

import com.xixin.health.common.error.BusinessException;
import com.xixin.health.common.error.ErrorCode;
import com.xixin.health.iam.UserAccount;
import com.xixin.health.iam.UserAccountRepository;
import com.xixin.health.notification.NotificationService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class HealthManagementService {

    private final HealthManagementRepository healthManagementRepository;
    private final UserAccountRepository userAccountRepository;
    private final NotificationService notificationService;
    private final JdbcTemplate jdbc;

    public HealthManagementService(
        HealthManagementRepository healthManagementRepository,
        UserAccountRepository userAccountRepository,
        NotificationService notificationService,
        JdbcTemplate jdbc
    ) {
        this.healthManagementRepository = healthManagementRepository;
        this.userAccountRepository = userAccountRepository;
        this.notificationService = notificationService;
        this.jdbc = jdbc;
    }

    @Transactional
    public HealthManagement openHighRiskRecord(Long userId, Long reportId, String riskLevel) {
        return healthManagementRepository.findByReportId(reportId)
            .orElseGet(() -> healthManagementRepository.save(HealthManagement.open(userId, reportId, riskLevel)));
    }

    @Transactional
    public HealthManagement create(Long userId, String riskLevel, String managementStatus, String followUpPlan, String interventionPlan) {
        HealthManagement management = HealthManagement.open(userId, null, riskLevel);
        if (managementStatus != null && !managementStatus.isBlank()) {
            management.setManagementStatus(managementStatus);
        }
        if (followUpPlan != null && !followUpPlan.isBlank()) {
            management.setFollowUpPlan(followUpPlan);
        }
        if (interventionPlan != null && !interventionPlan.isBlank()) {
            management.setInterventionPlan(interventionPlan);
        }
        management = healthManagementRepository.save(management);
        // 如果状态是已制定随访且有随访计划，自动发送通知
        if (HealthManagement.STATUS_FOLLOW_UP_PLANNED.equals(managementStatus)
            && followUpPlan != null && !followUpPlan.isBlank()) {
            String followUpContent = "随访计划：" + followUpPlan
                + (interventionPlan != null && !interventionPlan.isBlank() ? "\n干预方案：" + interventionPlan : "");
            notificationService.create(
                userId, "HEALTH_FOLLOW_UP", "健康随访提醒", followUpContent
            );
        }
        return management;
    }

    public List<HealthManagementVO> listWithUserInfo() {
        // 排除医护人员和管理员
        Set<Long> staffUserIds = jdbc.queryForList(
            "SELECT DISTINCT ur.user_id FROM sys_user_role ur " +
            "JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE r.role_type IN ('MEDICAL', 'ADMIN', 'BUSINESS')", Long.class)
            .stream().collect(Collectors.toSet());

        List<HealthManagement> records = healthManagementRepository.findAll().stream()
            .filter(r -> !staffUserIds.contains(r.getUserId()))
            .collect(Collectors.toList());
        Set<Long> userIds = records.stream().map(HealthManagement::getUserId).collect(Collectors.toSet());
        Map<Long, UserAccount> userMap = userAccountRepository.findAllById(userIds).stream()
            .collect(Collectors.toMap(UserAccount::getId, Function.identity()));
        return records.stream().map(record -> {
            UserAccount user = userMap.get(record.getUserId());
            return new HealthManagementVO(
                record.getId(),
                record.getUserId(),
                user != null ? user.getDisplayName() : null,
                user != null ? user.getPhone() : null,
                record.getReportId(),
                record.getRiskLevel(),
                record.getManagementStatus(),
                record.getFollowUpPlan(),
                record.getInterventionPlan()
            );
        }).collect(Collectors.toList());
    }

    public record HealthManagementVO(
        Long id, Long userId, String patientName, String patientPhone,
        Long reportId, String riskLevel, String managementStatus,
        String followUpPlan, String interventionPlan
    ) {}

    @Transactional
    public HealthManagement update(Long id, String riskLevel, String managementStatus, String followUpPlan, String interventionPlan) {
        HealthManagement management = healthManagementRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Health management record not found"));
        if (riskLevel != null && !riskLevel.isBlank()) management.setRiskLevel(riskLevel);
        if (managementStatus != null && !managementStatus.isBlank()) management.setManagementStatus(managementStatus);
        if (followUpPlan != null && !followUpPlan.isBlank()) management.setFollowUpPlan(followUpPlan);
        if (interventionPlan != null && !interventionPlan.isBlank()) management.setInterventionPlan(interventionPlan);
        return healthManagementRepository.save(management);
    }

    @Transactional
    public HealthManagement planFollowUp(Long id, String followUpPlan, String interventionPlan) {
        HealthManagement management = healthManagementRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Health management record not found"));
        management.planFollowUp(followUpPlan, interventionPlan);
        String followUpContent = "随访计划：" + followUpPlan
            + (interventionPlan != null && !interventionPlan.isBlank() ? "\n干预方案：" + interventionPlan : "");
        notificationService.create(
            management.getUserId(),
            "HEALTH_FOLLOW_UP",
            "健康随访提醒",
            followUpContent
        );
        return management;
    }
}
