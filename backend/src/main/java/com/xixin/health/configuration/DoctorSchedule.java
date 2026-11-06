package com.xixin.health.configuration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 医生排班 / 号源管理
 *
 * 每天每个医生在某个科室的某个时段有最大接诊人数限制，
 * booked_count 随预约成功自动增加，达到 max_patients 后该时段不可再约。
 */
@Entity
@Table(name = "t_doctor_schedule")
public class DoctorSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long doctorId;

    @Column(nullable = false)
    private Long branchId;

    @Column(nullable = false)
    private Long departmentId;

    /** 排班日期 */
    @Column(nullable = false)
    private LocalDate scheduleDate;

    /** 时段：MORNING / AFTERNOON / ALL_DAY */
    @Column(nullable = false, length = 32)
    private String timeSlot;

    /** 该时段最大可预约人数 */
    @Column(nullable = false)
    private int maxPatients;

    /** 当前已预约人数（自动累计） */
    @Column(nullable = false)
    private int bookedCount;

    /** 是否启用 */
    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected DoctorSchedule() {}

    public DoctorSchedule(Long doctorId, Long branchId, Long departmentId,
                          LocalDate scheduleDate, String timeSlot, int maxPatients) {
        this.doctorId = doctorId;
        this.branchId = branchId;
        this.departmentId = departmentId;
        this.scheduleDate = scheduleDate;
        this.timeSlot = timeSlot;
        this.maxPatients = maxPatients;
        this.bookedCount = 0;
        this.enabled = true;
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

    // ========== Business ==========

    /** 是否有剩余号源 */
    public boolean hasAvailableSlots() {
        return enabled && bookedCount < maxPatients;
    }

    /** 剩余号源数 */
    public int remainingSlots() {
        return Math.max(0, maxPatients - bookedCount);
    }

    /** 预约成功时增加已预约数 */
    public void incrementBooked() {
        if (!hasAvailableSlots()) {
            throw new IllegalStateException("该时段号源已满，无法继续预约");
        }
        this.bookedCount++;
    }

    /** 取消预约时减少已预约数 */
    public void decrementBooked() {
        if (this.bookedCount > 0) {
            this.bookedCount--;
        }
    }

    // ========== Getters / Setters ==========

    public Long getId() { return id; }
    public Long getDoctorId() { return doctorId; }
    public Long getBranchId() { return branchId; }
    public Long getDepartmentId() { return departmentId; }
    public LocalDate getScheduleDate() { return scheduleDate; }
    public String getTimeSlot() { return timeSlot; }
    public int getMaxPatients() { return maxPatients; }
    public int getBookedCount() { return bookedCount; }
    public boolean isEnabled() { return enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    public void setMaxPatients(int maxPatients) { this.maxPatients = maxPatients; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
