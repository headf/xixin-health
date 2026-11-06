package com.xixin.health.guide;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 营养餐/早餐领取记录
 */
@Entity
@Table(name = "t_meal_record")
public class MealRecord {

    public enum MealStatus {
        PENDING,    // 未满足领取条件
        ELIGIBLE,   // 可领取
        CLAIMED,    // 已领取
        SKIPPED     // 已跳过
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long examRecordId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 32)
    private String mealStatus;

    private LocalDateTime eligibleAt;

    private LocalDateTime claimedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected MealRecord() {
    }

    public MealRecord(Long examRecordId, Long userId) {
        this.examRecordId = examRecordId;
        this.userId = userId;
        this.mealStatus = MealStatus.PENDING.name();
    }

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public void markEligible() {
        this.mealStatus = MealStatus.ELIGIBLE.name();
        this.eligibleAt = LocalDateTime.now();
    }

    public void markClaimed() {
        this.mealStatus = MealStatus.CLAIMED.name();
        this.claimedAt = LocalDateTime.now();
    }

    public void markSkipped() {
        this.mealStatus = MealStatus.SKIPPED.name();
    }

    public Long getId() { return id; }
    public Long getExamRecordId() { return examRecordId; }
    public Long getUserId() { return userId; }
    public String getMealStatus() { return mealStatus; }
    public LocalDateTime getEligibleAt() { return eligibleAt; }
    public LocalDateTime getClaimedAt() { return claimedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
