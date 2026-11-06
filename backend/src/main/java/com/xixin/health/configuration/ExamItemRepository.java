package com.xixin.health.configuration;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamItemRepository extends JpaRepository<ExamItem, Long> {
    List<ExamItem> findByIdIn(List<Long> ids);
    List<ExamItem> findByEnabledTrue();
}
