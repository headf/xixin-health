package com.xixin.health.exam;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExamRecordRepository extends JpaRepository<ExamRecord, Long> {

    List<ExamRecord> findByBranchIdOrderByCreatedAtDesc(Long branchId);

    List<ExamRecord> findByBranchIdAndStatusOrderByCheckedInAtAsc(Long branchId, ExamStatus status);

    List<ExamRecord> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ExamRecord> findByBranchIdAndStatusAndCheckedInAtBetweenOrderByCheckedInAtAsc(Long branchId, ExamStatus status, java.time.LocalDateTime start, java.time.LocalDateTime end);

    List<ExamRecord> findByBranchIdAndStatusInAndCheckedInAtBetweenOrderByCheckedInAtAsc(Long branchId, java.util.List<ExamStatus> statuses, java.time.LocalDateTime start, java.time.LocalDateTime end);

    long countByCheckedInAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);

    @Query("SELECT COUNT(e) FROM ExamRecord e WHERE (:branchId IS NULL OR e.branchId = :branchId) AND e.checkedInAt BETWEEN :start AND :end")
    long countByBranchIdAndCheckedInAtBetween(@Param("branchId") Long branchId, @Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);

    Optional<ExamRecord> findByAppointmentId(Long appointmentId);
}
