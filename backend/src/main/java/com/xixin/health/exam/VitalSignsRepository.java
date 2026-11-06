package com.xixin.health.exam;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VitalSignsRepository extends JpaRepository<VitalSigns, Long> {
    Optional<VitalSigns> findByExamRecordId(Long examRecordId);
    boolean existsByExamRecordId(Long examRecordId);
}
