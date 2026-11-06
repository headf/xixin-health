package com.xixin.health.configuration;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackageItemRepository extends JpaRepository<PackageItem, Long> {
    List<PackageItem> findByPackageIdOrderBySortNo(Long packageId);
}
