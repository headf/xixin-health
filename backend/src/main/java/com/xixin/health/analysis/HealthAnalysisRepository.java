package com.xixin.health.analysis;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HealthAnalysisRepository extends JpaRepository<HealthAnalysis, Long> {

    List<HealthAnalysis> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<HealthAnalysis> findFirstByReportIdOrderByCreatedAtDesc(Long reportId);
}
