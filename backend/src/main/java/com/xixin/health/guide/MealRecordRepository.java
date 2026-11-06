package com.xixin.health.guide;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MealRecordRepository extends JpaRepository<MealRecord, Long> {

    Optional<MealRecord> findByExamRecordId(Long examRecordId);
}
