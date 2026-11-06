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
 * Tracks whether a nurse has confirmed all exam items are complete before the patient leaves.
 * 体检收尾确认 — 护士核对所有检查项目无遗漏
 */
@Entity
@Table(name = "t_exam_completion")
public class ExamCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long examRecordId;

    @Column(nullable = false)
    private boolean allItemsChecked;

    @Column(nullable = false)
    private boolean specimenCollected;

    @Column(nullable = false)
    private boolean vitalSignsTaken;

    @Column(nullable = false)
    private boolean nurseConfirmed;

    @Column(nullable = false, columnDefinition = "TINYINT(1) NOT NULL DEFAULT 0")
    private boolean needsReview;

    private Long confirmedBy;

    private LocalDateTime confirmedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected ExamCompletion() {}

    public ExamCompletion(Long examRecordId) {
        this.examRecordId = examRecordId;
        this.allItemsChecked = false;
        this.specimenCollected = false;
        this.vitalSignsTaken = false;
        this.nurseConfirmed = false;
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

    public void confirm(Long nurseId) {
        this.nurseConfirmed = true;
        this.confirmedBy = nurseId;
        this.confirmedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public Long getExamRecordId() { return examRecordId; }
    public boolean isAllItemsChecked() { return allItemsChecked; }
    public boolean isSpecimenCollected() { return specimenCollected; }
    public boolean isVitalSignsTaken() { return vitalSignsTaken; }
    public boolean isNurseConfirmed() { return nurseConfirmed; }
    public boolean isNeedsReview() { return needsReview; }
    public void setNeedsReview(boolean v) { this.needsReview = v; }
    public Long getConfirmedBy() { return confirmedBy; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }

    // Setters
    public void setAllItemsChecked(boolean v) { this.allItemsChecked = v; }
    public void setSpecimenCollected(boolean v) { this.specimenCollected = v; }
    public void setVitalSignsTaken(boolean v) { this.vitalSignsTaken = v; }
    public void setNurseConfirmed(boolean v) { this.nurseConfirmed = v; }
    public void setConfirmedBy(Long v) { this.confirmedBy = v; }
    public void setConfirmedAt(LocalDateTime v) { this.confirmedAt = v; }
}
