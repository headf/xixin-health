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
@Table(name = "t_department_queue")
public class DepartmentQueue {

    public enum QueueStatus {
        WAITING, CALLED, ARRIVED, COMPLETED, SKIPPED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long examRecordId;

    @Column(nullable = false)
    private Long departmentId;

    @Column(nullable = false, length = 32)
    private String queueNo;

    @Column(nullable = false, length = 32)
    private String queueStatus;

    @Column(length = 32)
    private String timeSlot;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected DepartmentQueue() {
    }

    public DepartmentQueue(Long examRecordId, Long departmentId, String queueNo) {
        this.examRecordId = examRecordId;
        this.departmentId = departmentId;
        this.queueNo = queueNo;
        this.queueStatus = QueueStatus.WAITING.name();
    }

    public DepartmentQueue(Long examRecordId, Long departmentId, String queueNo, String timeSlot) {
        this.examRecordId = examRecordId;
        this.departmentId = departmentId;
        this.queueNo = queueNo;
        this.timeSlot = timeSlot;
        this.queueStatus = QueueStatus.WAITING.name();
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

    public void setQueueStatus(QueueStatus status) {
        this.queueStatus = status.name();
    }

    public Long getId() { return id; }
    public Long getExamRecordId() { return examRecordId; }
    public Long getDepartmentId() { return departmentId; }
    public String getQueueNo() { return queueNo; }
    public String getQueueStatus() { return queueStatus; }
    public String getTimeSlot() { return timeSlot; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
}
