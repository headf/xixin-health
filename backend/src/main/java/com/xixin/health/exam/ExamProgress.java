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

@Entity
@Table(name = "t_exam_progress")
public class ExamProgress {

    public enum ProgressStatus {
        PENDING, IN_PROGRESS, COMPLETED, SKIPPED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long examRecordId;

    @Column(nullable = false)
    private Long itemId;

    private Long departmentId;

    @Column(nullable = false, length = 32)
    private String progressStatus;

    private LocalDateTime completedAt;

    @Column(length = 1024)
    private String departmentConclusion;  // Each department doctor's written conclusion

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected ExamProgress() {
    }

    public ExamProgress(Long examRecordId, Long itemId, Long departmentId) {
        this.examRecordId = examRecordId;
        this.itemId = itemId;
        this.departmentId = departmentId;
        this.progressStatus = ProgressStatus.PENDING.name();
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

    public void markInProgress() {
        this.progressStatus = ProgressStatus.IN_PROGRESS.name();
    }

    public void markCompleted() {
        this.progressStatus = ProgressStatus.COMPLETED.name();
        this.completedAt = LocalDateTime.now();
    }

    public void markSkipped() {
        this.progressStatus = ProgressStatus.SKIPPED.name();
        this.completedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getExamRecordId() { return examRecordId; }
    public Long getItemId() { return itemId; }
    public Long getDepartmentId() { return departmentId; }
    public String getProgressStatus() { return progressStatus; }
    public String getDepartmentConclusion() { return departmentConclusion; }
    public LocalDateTime getCompletedAt() { return completedAt; }

    public void setDepartmentConclusion(String conclusion) { this.departmentConclusion = conclusion; }
}
