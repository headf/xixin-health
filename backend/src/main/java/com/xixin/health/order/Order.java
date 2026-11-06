package com.xixin.health.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_order")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String orderNo;

    @Column(nullable = false)
    private Long appointmentId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private int amountCent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Order() {
    }

    public Order(Long appointmentId, Long userId, int amountCent) {
        this.orderNo = "ORD" + System.currentTimeMillis();
        this.appointmentId = appointmentId;
        this.userId = userId;
        this.amountCent = amountCent;
        this.status = OrderStatus.PENDING_PAYMENT;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void markPaid() {
        this.status = OrderStatus.PAID;
    }

    public void markRefundRequested() {
        this.status = OrderStatus.REFUND_REQUESTED;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public int getAmountCent() {
        return amountCent;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Long getUserId() {
        return userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
