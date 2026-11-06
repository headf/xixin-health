package com.xixin.health.management;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HealthManagementRepository extends JpaRepository<HealthManagement, Long> {

    Optional<HealthManagement> findByReportId(Long reportId);
}
