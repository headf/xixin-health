package com.xixin.health.order;

import com.xixin.health.appointment.Appointment;
import com.xixin.health.appointment.AppointmentService;
import com.xixin.health.common.error.BusinessException;
import com.xixin.health.common.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final AppointmentService appointmentService;
    private final RefundRecordRepository refundRecordRepository;

    public OrderService(
        OrderRepository orderRepository,
        PaymentRecordRepository paymentRecordRepository,
        AppointmentService appointmentService,
        RefundRecordRepository refundRecordRepository
    ) {
        this.orderRepository = orderRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.appointmentService = appointmentService;
        this.refundRecordRepository = refundRecordRepository;
    }

    @Transactional
    public Order createOrderForAppointment(Long appointmentId, int amountCent) {
        Appointment appointment = appointmentService.getById(appointmentId);
        if (appointment.getStatus() == com.xixin.health.appointment.AppointmentStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "预约已取消，无法创建订单");
        }
        // 检查是否已有订单，防止重复支付
        if (orderRepository.findByAppointmentId(appointmentId).isPresent()) {
            Order existing = orderRepository.findByAppointmentId(appointmentId).get();
            if (existing.getStatus() == OrderStatus.PAID) {
                throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "该预约已支付，无需重复支付");
            }
            if (existing.getStatus() == OrderStatus.PENDING_PAYMENT) {
                // 已有待支付订单，返回该订单
                return existing;
            }
        }
        return orderRepository.save(new Order(appointment.getId(), appointment.getUserId(), amountCent));
    }

    @Transactional
    public void simulatePaymentSuccess(String orderNo, String paymentNo) {
        Order order = getByOrderNo(orderNo);
        // 支付前检查预约是否已取消
        Appointment appointment = appointmentService.getById(order.getAppointmentId());
        if (appointment.getStatus() == com.xixin.health.appointment.AppointmentStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "预约已取消，无法支付");
        }
        order.markPaid();
        orderRepository.save(order);
        paymentRecordRepository.save(new PaymentRecord(order.getId(), paymentNo, "SIMULATED", order.getAmountCent(), PaymentStatus.SUCCESS));
        appointmentService.markPaid(order.getAppointmentId());
    }

    @Transactional(readOnly = true)
    public Order getByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Order not found"));
    }

    @Transactional(readOnly = true)
    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Order> listForUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // ========== Payment flow ==========

    public record PaymentInitiation(String orderNo, int amountCent, String channel, LocalDateTime initiatedAt) {}

    @Transactional
    public PaymentInitiation initiatePayment(String orderNo, String channel) {
        Order order = getByOrderNo(orderNo);
        return new PaymentInitiation(order.getOrderNo(), order.getAmountCent(), channel, LocalDateTime.now());
    }

    @Transactional
    public void handlePaymentCallback(String orderNo, String paymentNo, boolean success) {
        if (!success) return;
        simulatePaymentSuccess(orderNo, paymentNo);
    }

    @Transactional
    public RefundRecord requestRefund(String orderNo, int amountCent, String reason) {
        Order order = getByOrderNo(orderNo);
        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "Only paid orders can be refunded");
        }

        // 查找关联的预约
        Appointment appointment = appointmentService.getById(order.getAppointmentId());
        java.time.LocalDate aptDate = appointment.getAppointmentDate();

        // 退款规则：预约日前一天晚上8点前可免费退款
        java.time.LocalDateTime deadline = aptDate.atTime(20, 0).minusDays(1);
        if (java.time.LocalDateTime.now().isAfter(deadline)) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT,
                "The appointment can only be refunded before 8pm one day prior to the scheduled date");
        }

        // 更新订单状态
        order.markRefundRequested();
        orderRepository.save(order);

        // 创建退款记录
        RefundRecord refund = new RefundRecord(order.getId(), amountCent, reason);
        return refundRecordRepository.save(refund);
    }
}
