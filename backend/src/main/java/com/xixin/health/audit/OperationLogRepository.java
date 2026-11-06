package com.xixin.health.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    Optional<OperationLog> findTopByOrderByIdDesc();

    List<OperationLog> findAllByOrderByIdAsc();
}
