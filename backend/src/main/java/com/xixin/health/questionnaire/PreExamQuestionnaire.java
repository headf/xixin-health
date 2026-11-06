package com.xixin.health.questionnaire;

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
@Table(name = "t_pre_exam_questionnaire")
public class PreExamQuestionnaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long appointmentId;

    @Column(nullable = false)
    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String familyDiseaseHistory;

    @Column(columnDefinition = "TEXT")
    private String pastMedicalHistory;

    @Column(columnDefinition = "TEXT")
    private String medicationHistory;

    @Column(length = 32)
    private String smokingHistory;

    @Column(length = 32)
    private String drinkingHistory;

    @Column(length = 32)
    private String exerciseHabit;

    @Column(length = 32)
    private String dietHabit;

    @Column(length = 32)
    private String sleepQuality;

    @Column(columnDefinition = "TEXT")
    private String menstrualHistory;

    @Column(columnDefinition = "TEXT")
    private String allergicHistory;

    @Column(columnDefinition = "TEXT")
    private String surgicalHistory;

    @Column(columnDefinition = "TEXT")
    private String otherInfo;

    @Column(nullable = false, length = 32)
    private String status = "PENDING";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected PreExamQuestionnaire() {
    }

    public PreExamQuestionnaire(Long appointmentId, Long userId) {
        this.appointmentId = appointmentId;
        this.userId = userId;
        this.status = "PENDING";
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

    public void markCompleted() {
        this.status = "COMPLETED";
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFamilyDiseaseHistory() {
        return familyDiseaseHistory;
    }

    public void setFamilyDiseaseHistory(String familyDiseaseHistory) {
        this.familyDiseaseHistory = familyDiseaseHistory;
    }

    public String getPastMedicalHistory() {
        return pastMedicalHistory;
    }

    public void setPastMedicalHistory(String pastMedicalHistory) {
        this.pastMedicalHistory = pastMedicalHistory;
    }

    public String getMedicationHistory() {
        return medicationHistory;
    }

    public void setMedicationHistory(String medicationHistory) {
        this.medicationHistory = medicationHistory;
    }

    public String getSmokingHistory() {
        return smokingHistory;
    }

    public void setSmokingHistory(String smokingHistory) {
        this.smokingHistory = smokingHistory;
    }

    public String getDrinkingHistory() {
        return drinkingHistory;
    }

    public void setDrinkingHistory(String drinkingHistory) {
        this.drinkingHistory = drinkingHistory;
    }

    public String getExerciseHabit() {
        return exerciseHabit;
    }

    public void setExerciseHabit(String exerciseHabit) {
        this.exerciseHabit = exerciseHabit;
    }

    public String getDietHabit() {
        return dietHabit;
    }

    public void setDietHabit(String dietHabit) {
        this.dietHabit = dietHabit;
    }

    public String getSleepQuality() {
        return sleepQuality;
    }

    public void setSleepQuality(String sleepQuality) {
        this.sleepQuality = sleepQuality;
    }

    public String getMenstrualHistory() {
        return menstrualHistory;
    }

    public void setMenstrualHistory(String menstrualHistory) {
        this.menstrualHistory = menstrualHistory;
    }

    public String getAllergicHistory() {
        return allergicHistory;
    }

    public void setAllergicHistory(String allergicHistory) {
        this.allergicHistory = allergicHistory;
    }

    public String getSurgicalHistory() {
        return surgicalHistory;
    }

    public void setSurgicalHistory(String surgicalHistory) {
        this.surgicalHistory = surgicalHistory;
    }

    public String getOtherInfo() {
        return otherInfo;
    }

    public void setOtherInfo(String otherInfo) {
        this.otherInfo = otherInfo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
