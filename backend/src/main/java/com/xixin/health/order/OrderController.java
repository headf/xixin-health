package com.xixin.health.order;

import com.xixin.health.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<Order>> getUserOrders(@PathVariable Long userId) {
        return ApiResponse.success(orderService.getUserOrders(userId));
    }

    @PostMapping
    public ApiResponse<Order> create(@Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.success(orderService.createOrderForAppointment(request.appointmentId(), request.amountCent()));
    }

    @PostMapping("/simulate-payment")
    public ApiResponse<Void> simulatePayment(@Valid @RequestBody SimulatePaymentRequest request) {
        orderService.simulatePaymentSuccess(request.orderNo(), request.paymentNo());
        return ApiResponse.success(null);
    }

    @GetMapping
    public ApiResponse<List<Order>> list(@RequestParam Long userId) {
        return ApiResponse.success(orderService.listForUser(userId));
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<Order> detail(@PathVariable String orderNo) {
        return ApiResponse.success(orderService.getByOrderNo(orderNo));
    }

    @PostMapping("/{orderNo}/pay")
    public ApiResponse<OrderService.PaymentInitiation> pay(
        @PathVariable String orderNo,
        @Valid @RequestBody PaymentRequest request
    ) {
        return ApiResponse.success(orderService.initiatePayment(orderNo, request.channel()));
    }

    @PostMapping("/payment-callback")
    public ApiResponse<Void> paymentCallback(@Valid @RequestBody PaymentCallbackRequest request) {
        orderService.handlePaymentCallback(request.orderNo(), request.paymentNo(), request.success());
        return ApiResponse.success(null);
    }

    @PostMapping("/{orderNo}/refund")
    public ApiResponse<RefundRecord> refund(
        @PathVariable String orderNo,
        @Valid @RequestBody RefundRequest request
    ) {
        return ApiResponse.success(orderService.requestRefund(orderNo, request.amountCent(), request.reason()));
    }

    public record CreateOrderRequest(@NotNull Long appointmentId, @Positive int amountCent) {
    }

    public record SimulatePaymentRequest(@NotBlank String orderNo, @NotBlank String paymentNo) {
    }

    public record PaymentRequest(String channel) {
    }

    public record PaymentCallbackRequest(@NotBlank String orderNo, @NotBlank String paymentNo, boolean success) {
    }

    public record RefundRequest(@Positive int amountCent, String reason) {
    }
}
