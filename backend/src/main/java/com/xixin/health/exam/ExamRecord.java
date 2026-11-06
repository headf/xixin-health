package com.xixin.health.exam;

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
import java.time.LocalDateTime;

@Entity
@Table(name = "t_exam_record")
public class ExamRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String examNo;

    @Column(nullable = false)
    private Long appointmentId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long profileId;

    @Column(nullable = false)
    private Long branchId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ExamStatus status;

    private LocalDateTime checkedInAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private Long packageId;  // which package the patient purchased

    private String packageName;  // filled during checkIn from appointment

    @Column(length = 64)
    private String barcode;  // generated during check-in

    protected ExamRecord() {
    }

    public ExamRecord(String examNo, Long appointmentId, Long userId, Long profileId, Long branchId) {
        this.examNo = examNo;
        this.appointmentId = appointmentId;
        this.userId = userId;
        this.profileId = profileId;
        this.branchId = branchId;
        this.status = ExamStatus.CHECKED_IN;
        this.checkedInAt = LocalDateTime.now();
    }

    public void checkIn() {
        this.status = ExamStatus.CHECKED_IN;
        this.checkedInAt = LocalDateTime.now();
    }

    public void markInProgress() {
        this.status = ExamStatus.IN_PROGRESS;
    }

    public void markCompleted() {
        this.status = ExamStatus.COMPLETED;
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

    public Long getId() { return id; }
    public String getExamNo() { return examNo; }
    public Long getAppointmentId() { return appointmentId; }
    public Long getUserId() { return userId; }
    public Long getProfileId() { return profileId; }
    public Long getBranchId() { return branchId; }
    public Long getPackageId() { return packageId; }
    public String getPackageName() { return packageName; }
    public ExamStatus getStatus() { return status; }
    public LocalDateTime getCheckedInAt() { return checkedInAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setPackageId(Long packageId) { this.packageId = packageId; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
}
