package com.xixin.health.exam;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpecimenRepository extends JpaRepository<Specimen, Long> {
    List<Specimen> findByExamRecordId(Long examRecordId);
    List<Specimen> findByStatus(String status);
    List<Specimen> findByStatusIn(List<String> statuses);
}
