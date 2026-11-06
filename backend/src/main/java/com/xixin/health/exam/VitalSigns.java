package com.xixin.health.exam;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_vital_signs")
public class VitalSigns {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long examRecordId;

    private BigDecimal height;

    private BigDecimal weight;

    private Integer bloodPressureSystolic;

    private Integer bloodPressureDiastolic;

    private Integer heartRate;

    private BigDecimal temperature;

    private BigDecimal visionLeft;

    private BigDecimal visionRight;

    private BigDecimal waistline;

    private BigDecimal bmi;  // Auto-calculated: weight(kg) / (height(m))^2

    @Column(nullable = false)
    private Long recordedBy;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected VitalSigns() {
    }

    public VitalSigns(Long examRecordId, Long recordedBy) {
        this.examRecordId = examRecordId;
        this.recordedBy = recordedBy;
        this.recordedAt = LocalDateTime.now();
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
    public Long getExamRecordId() { return examRecordId; }
    public BigDecimal getHeight() { return height; }
    public BigDecimal getWeight() { return weight; }
    public Integer getBloodPressureSystolic() { return bloodPressureSystolic; }
    public Integer getBloodPressureDiastolic() { return bloodPressureDiastolic; }
    public Integer getHeartRate() { return heartRate; }
    public BigDecimal getTemperature() { return temperature; }
    public BigDecimal getVisionLeft() { return visionLeft; }
    public BigDecimal getVisionRight() { return visionRight; }
    public BigDecimal getWaistline() { return waistline; }
    public BigDecimal getBmi() { return bmi; }
    public Long getRecordedBy() { return recordedBy; }
    public LocalDateTime getRecordedAt() { return recordedAt; }

    public void setHeight(BigDecimal height) { this.height = height; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public void setBloodPressureSystolic(Integer bloodPressureSystolic) { this.bloodPressureSystolic = bloodPressureSystolic; }
    public void setBloodPressureDiastolic(Integer bloodPressureDiastolic) { this.bloodPressureDiastolic = bloodPressureDiastolic; }
    public void setHeartRate(Integer heartRate) { this.heartRate = heartRate; }
    public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }
    public void setVisionLeft(BigDecimal visionLeft) { this.visionLeft = visionLeft; }
    public void setVisionRight(BigDecimal visionRight) { this.visionRight = visionRight; }
    public void setWaistline(BigDecimal waistline) { this.waistline = waistline; }
    public void setBmi(BigDecimal bmi) { this.bmi = bmi; }

    /**
     * Auto-calculate BMI from height(cm) and weight(kg).
     * BMI = weight / (height/100)^2
     */
    public void calculateAndSetBmi() {
        if (height != null && weight != null && height.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal heightInMeters = height.divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
            this.bmi = weight.divide(heightInMeters.multiply(heightInMeters), 2, java.math.RoundingMode.HALF_UP);
        }
    }
}
