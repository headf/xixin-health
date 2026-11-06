package com.xixin.health.configuration;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamPackageItemRepository extends JpaRepository<ExamPackageItem, Long> {

    void deleteByItemId(Long itemId);

    void deleteByPackageId(Long packageId);

    List<ExamPackageItem> findByPackageIdOrderBySortNoAsc(Long packageId);
}
