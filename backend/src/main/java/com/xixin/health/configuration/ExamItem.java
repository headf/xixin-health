package com.xixin.health.configuration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_exam_item")
public class ExamItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String itemCode;

    @Column(nullable = false, length = 128)
    private String itemName;

    private Long departmentId;

    @Column(length = 32)
    private String unit;

    private BigDecimal referenceMin;

    private BigDecimal referenceMax;

    private BigDecimal criticalMax;

    private BigDecimal criticalMin;

    @Column(nullable = false)
    private boolean enabled;

    @Transient
    private String reviewStatus = "APPROVED";
    @Transient
    private Long reviewerId;
    @Transient
    private LocalDateTime reviewedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    protected ExamItem() {
    }

    public static ExamItem create(
        String itemCode, String itemName, Long departmentId, String unit,
        BigDecimal referenceMin, BigDecimal referenceMax, BigDecimal criticalMin, BigDecimal criticalMax,
        boolean enabled
    ) {
        ExamItem item = new ExamItem();
        item.update(itemCode, itemName, departmentId, unit, referenceMin, referenceMax, criticalMin, criticalMax, enabled);
        return item;
    }

    public void update(
        String itemCode, String itemName, Long departmentId, String unit,
        BigDecimal referenceMin, BigDecimal referenceMax, BigDecimal criticalMin, BigDecimal criticalMax,
        boolean enabled
    ) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.departmentId = departmentId;
        this.unit = unit;
        this.referenceMin = referenceMin;
        this.referenceMax = referenceMax;
        this.criticalMin = criticalMin;
        this.criticalMax = criticalMax;
        this.enabled = enabled;
    }

    public boolean hasMedicalReferenceChange(BigDecimal refMin, BigDecimal refMax, BigDecimal critMin, BigDecimal critMax) {
        return !java.util.Objects.equals(this.referenceMin, refMin)
            || !java.util.Objects.equals(this.referenceMax, refMax)
            || !java.util.Objects.equals(this.criticalMin, critMin)
            || !java.util.Objects.equals(this.criticalMax, critMax);
    }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void markPendingReview() { this.reviewStatus = "PENDING_REVIEW"; this.reviewerId = null; this.reviewedAt = null; this.enabled = false; }
    public void approveReview(Long reviewerId) { this.reviewStatus = "APPROVED"; this.reviewerId = reviewerId; this.reviewedAt = LocalDateTime.now(); this.enabled = true; }
    public void rejectReview(Long reviewerId) { this.reviewStatus = "REJECTED"; this.reviewerId = reviewerId; this.reviewedAt = LocalDateTime.now(); this.enabled = false; }

    public Long getId() { return id; }
    public String getItemCode() { return itemCode; }
    public String getItemName() { return itemName; }
    public Long getDepartmentId() { return departmentId; }
    public String getUnit() { return unit; }
    public BigDecimal getReferenceMin() { return referenceMin; }
    public BigDecimal getReferenceMax() { return referenceMax; }
    public BigDecimal getCriticalMin() { return criticalMin; }
    public BigDecimal getCriticalMax() { return criticalMax; }
    public boolean getEnabled() { return enabled; }
    public boolean isEnabled() { return enabled; }
    public String getReviewStatus() { return reviewStatus; }
    public Long getReviewerId() { return reviewerId; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
