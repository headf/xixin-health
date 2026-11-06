package com.xixin.health.exam;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamProgressRepository extends JpaRepository<ExamProgress, Long> {
    List<ExamProgress> findByExamRecordId(Long examRecordId);
    List<ExamProgress> findByExamRecordIdAndProgressStatus(Long examRecordId, String progressStatus);
    List<ExamProgress> findByDepartmentIdAndProgressStatus(Long departmentId, String progressStatus);
    List<ExamProgress> findByExamRecordIdAndDepartmentId(Long examRecordId, Long departmentId);
    long countByExamRecordIdAndProgressStatus(Long examRecordId, String progressStatus);
}
