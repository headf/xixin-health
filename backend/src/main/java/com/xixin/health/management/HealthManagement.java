package com.xixin.health.management;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_health_management")
public class HealthManagement {

    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_FOLLOW_UP_PLANNED = "FOLLOW_UP_PLANNED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private Long reportId;

    @Column(nullable = false, length = 32)
    private String riskLevel;

    @Column(nullable = false, length = 32)
    private String managementStatus;

    @Column(length = 512)
    private String followUpPlan;

    @Column(columnDefinition = "TEXT")
    private String interventionPlan;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected HealthManagement() {
    }

    public static HealthManagement open(Long userId, Long reportId, String riskLevel) {
        HealthManagement management = new HealthManagement();
        management.userId = userId;
        management.reportId = reportId;
        management.riskLevel = riskLevel;
        management.managementStatus = STATUS_OPEN;
        return management;
    }

    public void planFollowUp(String followUpPlan, String interventionPlan) {
        this.followUpPlan = followUpPlan;
        this.interventionPlan = interventionPlan;
        this.managementStatus = STATUS_FOLLOW_UP_PLANNED;
    }

    /** 直接设置随访计划和干预方案（不改变管理状态） */
    public void setFollowUpPlan(String followUpPlan) { this.followUpPlan = followUpPlan; }
    public void setInterventionPlan(String interventionPlan) { this.interventionPlan = interventionPlan; }
    public void setManagementStatus(String managementStatus) { this.managementStatus = managementStatus; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getReportId() {
        return reportId;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public String getManagementStatus() {
        return managementStatus;
    }

    public String getFollowUpPlan() {
        return followUpPlan;
    }

    public String getInterventionPlan() {
        return interventionPlan;
    }
}
