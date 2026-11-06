package com.xixin.health.appointment;

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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_appointment")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String appointmentNo;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long profileId;

    @Column(nullable = false)
    private Long packageId;

    @Column(nullable = false)
    private Long branchId;

    @Column(nullable = false)
    private LocalDate appointmentDate;

    @Column(nullable = false, length = 32)
    private String timeSlot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AppointmentStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean deleted;

    protected Appointment() {
    }

    public Appointment(Long userId, Long profileId, Long packageId, Long branchId, LocalDate appointmentDate, String timeSlot) {
        this.appointmentNo = "APT" + System.currentTimeMillis();
        this.userId = userId;
        this.profileId = profileId;
        this.packageId = packageId;
        this.branchId = branchId;
        this.appointmentDate = appointmentDate;
        this.timeSlot = timeSlot;
        this.status = AppointmentStatus.CREATED;
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

    public void markPaid() {
        this.status = AppointmentStatus.PAID;
    }

    public void markCheckedIn() {
        this.status = AppointmentStatus.CHECKED_IN;
    }

    public void markCancelled() {
        this.status = AppointmentStatus.CANCELLED;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }


    public Long getId() {
        return id;
    }

    public String getAppointmentNo() {
        return appointmentNo;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getPackageId() {
        return packageId;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public Long getProfileId() {
        return profileId;
    }

    public Long getBranchId() {
        return branchId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
