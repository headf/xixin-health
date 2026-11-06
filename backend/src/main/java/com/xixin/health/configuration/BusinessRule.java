package com.xixin.health.configuration;

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
@Table(name = "t_business_rule")
public class BusinessRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String ruleCode;

    @Column(nullable = false, length = 128)
    private String ruleName;

    @Column(nullable = false, length = 64)
    private String ruleType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String ruleContent;

    @Column(nullable = false)
    private int versionNo = 1;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false, length = 32)
    private String reviewStatus = "APPROVED";

    private Long reviewerId;

    private LocalDateTime reviewedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    protected BusinessRule() {
    }

    public static BusinessRule create(
        String ruleCode,
        String ruleName,
        String ruleType,
        String ruleContent,
        boolean enabled
    ) {
        BusinessRule rule = new BusinessRule();
        rule.ruleCode = ruleCode;
        rule.ruleName = ruleName;
        rule.ruleType = ruleType;
        rule.ruleContent = ruleContent;
        rule.enabled = enabled;
        return rule;
    }

    public void update(String ruleCode, String ruleName, String ruleType, String ruleContent, boolean enabled) {
        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
        this.ruleType = ruleType;
        this.ruleContent = ruleContent;
        this.enabled = enabled;
        this.versionNo++;
    }

    public void markPendingReview() {
        this.reviewStatus = "PENDING_REVIEW";
        this.reviewerId = null;
        this.reviewedAt = null;
        this.enabled = false;
    }

    public void approveReview(Long reviewerId) {
        this.reviewStatus = "APPROVED";
        this.reviewerId = reviewerId;
        this.reviewedAt = LocalDateTime.now();
        this.enabled = true;
    }

    public void rejectReview(Long reviewerId) {
        this.reviewStatus = "REJECTED";
        this.reviewerId = reviewerId;
        this.reviewedAt = LocalDateTime.now();
        this.enabled = false;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

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

    public String getRuleCode() {
        return ruleCode;
    }

    public String getRuleName() {
        return ruleName;
    }

    public String getRuleType() {
        return ruleType;
    }

    public String getRuleContent() {
        return ruleContent;
    }

    public int getVersionNo() {
        return versionNo;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
}
