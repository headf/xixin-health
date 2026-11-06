package com.xixin.health.analysis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_health_analysis")
public class HealthAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reportId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private int abnormalCount;

    @Column(nullable = false)
    private int criticalCount;

    @Column(columnDefinition = "TEXT")
    private String trendData;

    @Column(nullable = false, length = 32)
    private String riskLevel;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected HealthAnalysis() {
    }

    public HealthAnalysis(Long reportId, Long userId, int abnormalCount, int criticalCount, String trendData, String riskLevel) {
        this.reportId = reportId;
        this.userId = userId;
        this.abnormalCount = abnormalCount;
        this.criticalCount = criticalCount;
        this.trendData = trendData;
        this.riskLevel = riskLevel;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getReportId() {
        return reportId;
    }

    public Long getUserId() {
        return userId;
    }

    public int getAbnormalCount() {
        return abnormalCount;
    }

    public int getCriticalCount() {
        return criticalCount;
    }

    public String getTrendData() {
        return trendData;
    }

    public String getRiskLevel() {
        return riskLevel;
    }
}
