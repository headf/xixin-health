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
@Table(name = "t_report_template")
public class ReportTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String templateCode;

    @Column(nullable = false, length = 128)
    private String templateName;

    private Long packageId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String templateContent;

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

    protected ReportTemplate() {
    }

    public static ReportTemplate create(
        String templateCode,
        String templateName,
        Long packageId,
        String templateContent,
        boolean enabled
    ) {
        ReportTemplate template = new ReportTemplate();
        template.templateCode = templateCode;
        template.templateName = templateName;
        template.packageId = packageId;
        template.templateContent = templateContent;
        template.enabled = enabled;
        return template;
    }

    public void update(String templateCode, String templateName, Long packageId, String templateContent, boolean enabled) {
        this.templateCode = templateCode;
        this.templateName = templateName;
        this.packageId = packageId;
        this.templateContent = templateContent;
        this.enabled = enabled;
        this.versionNo++;
    }

    public String preview(String summary, String advice, String reviewer) {
        return templateContent
            .replace("{summary}", valueOrBlank(summary))
            .replace("{advice}", valueOrBlank(advice))
            .replace("{reviewer}", valueOrBlank(reviewer));
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    private String valueOrBlank(String value) {
        return value == null ? "" : value;
    }

    public Long getId() {
        return id;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public String getTemplateName() {
        return templateName;
    }

    public Long getPackageId() {
        return packageId;
    }

    public String getTemplateContent() {
        return templateContent;
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
