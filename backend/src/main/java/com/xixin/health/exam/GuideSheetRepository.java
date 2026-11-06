package com.xixin.health.exam;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuideSheetRepository extends JpaRepository<GuideSheet, Long> {

    Optional<GuideSheet> findByExamRecordId(Long examRecordId);
}
