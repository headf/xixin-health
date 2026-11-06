package com.xixin.health.exam;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Tracks blood/urine specimens collected during the exam.
 * 标本管理 — 采血/留尿后跟踪条码、送检状态
 */
@Entity
@Table(name = "t_specimen")
public class Specimen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String specimenNo;

    @Column(nullable = false)
    private Long examRecordId;

    @Column(nullable = false, length = 32)
    private String specimenType;  // BLOOD / URINE

    @Column(length = 128)
    private String barcode;

    @Column(nullable = false, length = 32)
    private String status;  // COLLECTED / LABELED / SENT / ANALYZED / REJECTED

    @Column(nullable = false)
    private Long collectedBy;

    @Column(nullable = false)
    private LocalDateTime collectedAt;

    private Long sentToLabBy;

    private LocalDateTime sentToLabAt;

    /** 标本接收人（检验科/实验室人员） */
    private Long receivedBy;

    /** 标本接收时间 */
    private LocalDateTime receivedAt;

    /** 标本拒收原因 */
    @Column(length = 512)
    private String rejectReason;

    /** 标本拒收人 */
    private Long rejectedBy;

    /** 标本拒收时间 */
    private LocalDateTime rejectedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Specimen() {}

    public Specimen(Long examRecordId, String specimenType, Long collectedBy) {
        this.specimenNo = "SP" + System.currentTimeMillis();
        this.examRecordId = examRecordId;
        this.specimenType = specimenType;
        this.collectedBy = collectedBy;
        this.status = "COLLECTED";
        this.collectedAt = LocalDateTime.now();
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

    public void markLabeled(String barcode) {
        this.barcode = barcode;
        this.status = "LABELED";
    }

    public void markSent(Long sentBy) {
        this.sentToLabBy = sentBy;
        this.sentToLabAt = LocalDateTime.now();
        this.status = "SENT";
    }

    /** 标本接收确认（检验科人员接收标本进行检测） */
    public void markReceived(Long receivedBy) {
        this.receivedBy = receivedBy;
        this.receivedAt = LocalDateTime.now();
        this.status = "ANALYZED";
    }

    /** 标本拒收退回（检验科人员发现标本不合格） */
    public void markRejected(Long rejectedBy, String reason) {
        this.rejectedBy = rejectedBy;
        this.rejectedAt = LocalDateTime.now();
        this.rejectReason = reason;
        this.status = "REJECTED";
    }

    // Getters
    public Long getId() { return id; }
    public String getSpecimenNo() { return specimenNo; }
    public Long getExamRecordId() { return examRecordId; }
    public String getSpecimenType() { return specimenType; }
    public String getBarcode() { return barcode; }
    public String getStatus() { return status; }
    public Long getCollectedBy() { return collectedBy; }
    public LocalDateTime getCollectedAt() { return collectedAt; }
    public Long getSentToLabBy() { return sentToLabBy; }
    public LocalDateTime getSentToLabAt() { return sentToLabAt; }
    public Long getReceivedBy() { return receivedBy; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public String getRejectReason() { return rejectReason; }
    public Long getRejectedBy() { return rejectedBy; }
    public LocalDateTime getRejectedAt() { return rejectedAt; }
}
