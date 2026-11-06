package com.xixin.health.result;

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
@Table(name = "t_critical_alert")
public class CriticalAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long resultId;

    private Long examRecordId;

    private Long userId;

    @Column(nullable = false, length = 32)
    private String alertStatus; // INITIAL / REVIEWED / CONFIRMED / FOLLOWED_UP

    @Column(nullable = false)
    private LocalDateTime alertedAt;

    private Long reviewedBy;

    private LocalDateTime reviewedAt;

    @Column(length = 512)
    private String reviewComment;

    private LocalDateTime patientConfirmedAt;

    @Column(length = 1024)
    private String followUpPlan;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected CriticalAlert() {
    }

    public CriticalAlert(Long resultId, Long examRecordId, Long userId) {
        this.resultId = resultId;
        this.examRecordId = examRecordId;
        this.userId = userId;
        this.alertStatus = "INITIAL";
        this.alertedAt = LocalDateTime.now();
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

    // ========== Business methods ==========

    public void review(Long reviewerId, String comment) {
        this.alertStatus = "REVIEWED";
        this.reviewedBy = reviewerId;
        this.reviewedAt = LocalDateTime.now();
        this.reviewComment = comment;
    }

    public void confirm() {
        this.alertStatus = "CONFIRMED";
        this.patientConfirmedAt = LocalDateTime.now();
    }

    public void setFollowUpPlan(String plan) {
        this.alertStatus = "FOLLOWED_UP";
        this.followUpPlan = plan;
    }

    // ========== Getters ==========

    public Long getId() {
        return id;
    }

    public Long getResultId() {
        return resultId;
    }

    public Long getExamRecordId() {
        return examRecordId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getAlertStatus() {
        return alertStatus;
    }

    public LocalDateTime getAlertedAt() {
        return alertedAt;
    }

    public Long getReviewedBy() {
        return reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public LocalDateTime getPatientConfirmedAt() {
        return patientConfirmedAt;
    }

    public String getFollowUpPlan() {
        return followUpPlan;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
