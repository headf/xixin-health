package com.xixin.health.report;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamReportRepository extends JpaRepository<ExamReport, Long> {

    List<ExamReport> findByExamRecordId(Long examRecordId);

    List<ExamReport> findByUserIdOrderByGeneratedAtDesc(Long userId);

    List<ExamReport> findByReportStatus(ReportStatus reportStatus);
}
