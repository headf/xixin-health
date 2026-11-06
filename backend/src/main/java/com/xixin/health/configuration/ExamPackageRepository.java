package com.xixin.health.configuration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface ExamPackageRepository extends JpaRepository<ExamPackage, Long> {

    List<ExamPackage> findByEnabledTrue();

    List<ExamPackage> findByEnabledTrue(Sort sort);
}
