package com.xixin.health.result;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CriticalAlertRepository extends JpaRepository<CriticalAlert, Long> {

    List<CriticalAlert> findByAlertStatus(String alertStatus);

    List<CriticalAlert> findByResultId(Long resultId);

    List<CriticalAlert> findByUserId(Long userId);
}
