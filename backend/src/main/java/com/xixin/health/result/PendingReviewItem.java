package com.xixin.health.result;

import java.math.BigDecimal;

/**
 * 待审核结果DTO，包含患者姓名和分院ID
 */
public class PendingReviewItem {
    private Long id;
    private String resultNo;
    private Long examRecordId;
    private String patientName;
    private Long branchId;
    private Long itemId;
    private String itemName;
    private String unit;
    private BigDecimal numericValue;
    private String textValue;
    private BigDecimal referenceMin;
    private BigDecimal referenceMax;
    private BigDecimal criticalMax;
    private String abnormalLevel;
    private String reviewStatus;
    private Long recorderId;

    public PendingReviewItem() {}

    public PendingReviewItem(Long id, String resultNo, Long examRecordId, String patientName,
                             Long branchId, Long itemId, String itemName, String unit,
                             BigDecimal numericValue, String textValue,
                             BigDecimal referenceMin, BigDecimal referenceMax,
                             BigDecimal criticalMax, String abnormalLevel,
                             String reviewStatus, Long recorderId) {
        this.id = id;
        this.resultNo = resultNo;
        this.examRecordId = examRecordId;
        this.patientName = patientName;
        this.branchId = branchId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.unit = unit;
        this.numericValue = numericValue;
        this.textValue = textValue;
        this.referenceMin = referenceMin;
        this.referenceMax = referenceMax;
        this.criticalMax = criticalMax;
        this.abnormalLevel = abnormalLevel;
        this.reviewStatus = reviewStatus;
        this.recorderId = recorderId;
    }

    // Getters
    public Long getId() { return id; }
    public String getResultNo() { return resultNo; }
    public Long getExamRecordId() { return examRecordId; }
    public String getPatientName() { return patientName; }
    public Long getBranchId() { return branchId; }
    public Long getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public String getUnit() { return unit; }
    public BigDecimal getNumericValue() { return numericValue; }
    public String getTextValue() { return textValue; }
    public BigDecimal getReferenceMin() { return referenceMin; }
    public BigDecimal getReferenceMax() { return referenceMax; }
    public BigDecimal getCriticalMax() { return criticalMax; }
    public String getAbnormalLevel() { return abnormalLevel; }
    public String getReviewStatus() { return reviewStatus; }
    public Long getRecorderId() { return recorderId; }
}
