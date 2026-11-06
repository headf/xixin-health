package com.xixin.health.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_payment_record")
public class PaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false, unique = true, length = 64)
    private String paymentNo;

    @Column(nullable = false, length = 32)
    private String channel;

    @Column(nullable = false)
    private int amountCent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status;

    private LocalDateTime paidAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected PaymentRecord() {
    }

    public PaymentRecord(Long orderId, String paymentNo, String channel, int amountCent, PaymentStatus status) {
        this.orderId = orderId;
        this.paymentNo = paymentNo;
        this.channel = channel;
        this.amountCent = amountCent;
        this.status = status;
        this.paidAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }
}
