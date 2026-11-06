package com.xixin.health.report;

import com.xixin.health.result.AbnormalLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_exam_report")
public class ExamReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String reportNo;

    @Column(nullable = false)
    private Long examRecordId;

    @Column(nullable = false)
    private Long userId;

    @Transient
    private String userName;

    private Long templateId;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String doctorAdvice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AbnormalLevel abnormalStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReportStatus reportStatus;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    private Long reviewerId;

    private LocalDateTime publishedAt;

    @Column(length = 512)
    private String fileUrl;

    // ── Comprehensive audit fields (V3) ──
    @Column(columnDefinition = "TEXT")
    private String overallHealthAssessment;     // 总体健康评估

    @Column(columnDefinition = "TEXT")
    private String lifestyleRecommendations;    // 个性化生活建议

    @Column(columnDefinition = "TEXT")
    private String followUpRecommendations;     // 复查建议

    @Column(length = 64)
    private String classification;              // NORMAL / PHYSIOLOGICAL / SUB_HEALTH / NEED_RECHECK / NEED_TREATMENT

    private LocalDateTime signedAt;             // 终审签字时间

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected ExamReport() {
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

    public ExamReport(Long examRecordId, Long userId, Long reviewerId, AbnormalLevel abnormalStatus) {
        this.reportNo = "RPT" + System.currentTimeMillis();
        this.examRecordId = examRecordId;
        this.userId = userId;
        this.reviewerId = reviewerId;
        this.abnormalStatus = abnormalStatus;
        this.reportStatus = ReportStatus.CONFIRMED;
        this.summary = "";
        this.doctorAdvice = "";
        this.generatedAt = LocalDateTime.now();
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setDoctorAdvice(String doctorAdvice) {
        this.doctorAdvice = doctorAdvice;
    }

    public void publish() {
        this.reportStatus = ReportStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void appeal() {
        this.reportStatus = ReportStatus.APPEALED;
    }

    /** Final sign-off by the review doctor — locks the report. */
    public void finalSignOff(String classification) {
        this.reportStatus = ReportStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
        this.classification = classification;
        this.signedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getReportNo() { return reportNo; }
    public Long getExamRecordId() { return examRecordId; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Long getTemplateId() { return templateId; }
    public String getSummary() { return summary; }
    public String getDoctorAdvice() { return doctorAdvice; }
    public AbnormalLevel getAbnormalStatus() { return abnormalStatus; }
    public ReportStatus getReportStatus() { return reportStatus; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public Long getReviewerId() { return reviewerId; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public String getFileUrl() { return fileUrl; }

    // ── Comprehensive audit getters ──
    public String getOverallHealthAssessment() { return overallHealthAssessment; }
    public String getLifestyleRecommendations() { return lifestyleRecommendations; }
    public String getFollowUpRecommendations() { return followUpRecommendations; }
    public String getClassification() { return classification; }
    public LocalDateTime getSignedAt() { return signedAt; }

    // ── Audit getters ──
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setOverallHealthAssessment(String v) { this.overallHealthAssessment = v; }
    public void setLifestyleRecommendations(String v) { this.lifestyleRecommendations = v; }
    public void setFollowUpRecommendations(String v) { this.followUpRecommendations = v; }
    public void setClassification(String v) { this.classification = v; }
    public void setFileUrl(String v) { this.fileUrl = v; }
}
