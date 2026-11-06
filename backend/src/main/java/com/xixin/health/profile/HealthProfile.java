package com.xixin.health.profile;

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
@Table(name = "t_health_profile")
public class HealthProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ownerUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ProfileType profileType;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(length = 256)
    private String idCardNo;

    @Column(length = 256)
    private String phone;

    @Column(length = 512)
    private String allergyHistory;

    @Column(length = 512)
    private String medicalHistory;

    @Column(length = 64)
    private String emergencyContactName;

    @Column(length = 256)
    private String emergencyContactPhone;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean deleted;

    protected HealthProfile() {
    }

    public HealthProfile(Long ownerUserId, ProfileType profileType) {
        this.ownerUserId = ownerUserId;
        this.profileType = profileType;
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

    public void update(
        String name,
        String idCardNo,
        String phone,
        String allergyHistory,
        String medicalHistory,
        String emergencyContactName,
        String emergencyContactPhone,
        ProfileType profileType
    ) {
        this.name = name;
        this.idCardNo = idCardNo;
        this.phone = phone;
        this.allergyHistory = allergyHistory;
        this.medicalHistory = medicalHistory;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
        if (profileType != null) {
            this.profileType = profileType;
        }
    }

    public Long getId() { return id; }
    public Long getOwnerUserId() { return ownerUserId; }
    public ProfileType getProfileType() { return profileType; }
    public String getName() { return name; }
    public String getIdCardNo() { return idCardNo; }
    public String getPhone() { return phone; }
    public String getAllergyHistory() { return allergyHistory; }
    public String getMedicalHistory() { return medicalHistory; }
    public String getEmergencyContactName() { return emergencyContactName; }
    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
