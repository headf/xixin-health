package com.xixin.health.result;

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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_check_result")
public class CheckResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String resultNo;

    @Column(nullable = false)
    private Long examRecordId;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false, length = 128)
    private String itemName;

    @Column(length = 32)
    private String unit;

    private BigDecimal numericValue;

    @Column(length = 512)
    private String textValue;

    private BigDecimal referenceMin;

    private BigDecimal referenceMax;

    private BigDecimal criticalMax;

    /** 下限危急值：低于该值视为 CRITICAL（危急） */
    private BigDecimal criticalMin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AbnormalLevel abnormalLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReviewStatus reviewStatus;

    @Column(nullable = false)
    private Long recorderId;

    @Column(length = 32)
    private String secondReviewStatus; // PENDING / APPROVED / REJECTED

    private Long secondReviewerId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected CheckResult() {
    }

    public CheckResult(
        Long examRecordId,
        Long itemId,
        String itemName,
        String unit,
        BigDecimal numericValue,
        BigDecimal referenceMin,
        BigDecimal referenceMax,
        BigDecimal criticalMax,
        BigDecimal criticalMin,
        Long recorderId,
        AbnormalLevel abnormalLevel
    ) {
        this.resultNo = "RES" + System.currentTimeMillis();
        this.examRecordId = examRecordId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.unit = unit;
        this.numericValue = numericValue;
        this.referenceMin = referenceMin;
        this.referenceMax = referenceMax;
        this.criticalMax = criticalMax;
        this.criticalMin = criticalMin;
        this.recorderId = recorderId;
        this.abnormalLevel = abnormalLevel;
        this.reviewStatus = ReviewStatus.PENDING;
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

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    public void setNumericValue(BigDecimal numericValue) {
        this.numericValue = numericValue;
    }

    public void setReferenceMin(BigDecimal referenceMin) {
        this.referenceMin = referenceMin;
    }

    public void setReferenceMax(BigDecimal referenceMax) {
        this.referenceMax = referenceMax;
    }

    public void setCriticalMax(BigDecimal criticalMax) {
        this.criticalMax = criticalMax;
    }

    public BigDecimal getCriticalMin() { return criticalMin; }

    public void setCriticalMin(BigDecimal criticalMin) {
        this.criticalMin = criticalMin;
    }

    public void setAbnormalLevel(AbnormalLevel abnormalLevel) {
        this.abnormalLevel = abnormalLevel;
    }

    public void setReviewStatus(ReviewStatus reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public void approve() {
        this.reviewStatus = ReviewStatus.APPROVED;
    }

    public void reject() {
        this.reviewStatus = ReviewStatus.REJECTED;
    }

    public Long getId() {
        return id;
    }

    public Long getExamRecordId() {
        return examRecordId;
    }

    public AbnormalLevel getAbnormalLevel() {
        return abnormalLevel;
    }

    public ReviewStatus getReviewStatus() {
        return reviewStatus;
    }

    // ── Missing getters for JSON serialization ──
    public String getItemName() { return itemName; }
    public String getUnit() { return unit; }
    public BigDecimal getNumericValue() { return numericValue; }
    public String getTextValue() { return textValue; }
    public BigDecimal getReferenceMin() { return referenceMin; }
    public BigDecimal getReferenceMax() { return referenceMax; }
    public BigDecimal getCriticalMax() { return criticalMax; }
    public Long getRecorderId() { return recorderId; }
    public String getResultNo() { return resultNo; }
    public Long getItemId() { return itemId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getSecondReviewStatus() { return secondReviewStatus; }
    public Long getSecondReviewerId() { return secondReviewerId; }

    public void setSecondReviewStatus(String secondReviewStatus) {
        this.secondReviewStatus = secondReviewStatus;
    }

    public void setSecondReviewerId(Long secondReviewerId) {
        this.secondReviewerId = secondReviewerId;
    }
}
