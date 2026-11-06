package com.xixin.health.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNo(String orderNo);

    Optional<Order> findByAppointmentId(Long appointmentId);

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
}
