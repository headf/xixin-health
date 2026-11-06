package com.xixin.health.exam;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExamCompletionRepository extends JpaRepository<ExamCompletion, Long> {
    Optional<ExamCompletion> findByExamRecordId(Long examRecordId);
}
