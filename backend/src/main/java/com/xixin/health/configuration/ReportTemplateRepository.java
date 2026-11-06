package com.xixin.health.configuration;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportTemplateRepository extends JpaRepository<ReportTemplate, Long> {

    Optional<ReportTemplate> findFirstByPackageIdAndEnabledTrueOrderByVersionNoDesc(Long packageId);
}
