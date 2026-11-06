package com.xixin.health.exam;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DepartmentQueueRepository extends JpaRepository<DepartmentQueue, Long> {
    List<DepartmentQueue> findByDepartmentIdAndQueueStatusOrderByCreatedAt(Long departmentId, String queueStatus);
    List<DepartmentQueue> findByDepartmentIdOrderByCreatedAt(Long departmentId);
    Optional<DepartmentQueue> findFirstByDepartmentIdAndQueueStatusOrderByCreatedAt(Long departmentId, String queueStatus);
    Optional<DepartmentQueue> findByExamRecordIdAndDepartmentId(Long examRecordId, Long departmentId);
    long countByDepartmentIdAndQueueStatus(Long departmentId, String queueStatus);
    List<DepartmentQueue> findByExamRecordIdAndQueueStatusNot(Long examRecordId, String queueStatus);
    List<DepartmentQueue> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime after);
}
