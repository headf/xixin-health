package com.xixin.health.appointment;

import com.xixin.health.order.Order;
import com.xixin.health.order.OrderService;
import com.xixin.health.order.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class AppointmentOrderFlowTest {

    @Autowired
    AppointmentService appointmentService;

    @Autowired
    OrderService orderService;

    @Test
    void createsAppointmentAndMarksPaidAfterSimulatedPayment() {
        Appointment appointment = appointmentService.createAppointment(1L, 1L, 1L, 1L, LocalDate.now().plusDays(2), "AM");
        Order order = orderService.createOrderForAppointment(appointment.getId(), 29900);

        orderService.simulatePaymentSuccess(order.getOrderNo(), "PAY-001");

        Order paid = orderService.getByOrderNo(order.getOrderNo());
        Appointment reloaded = appointmentService.getById(appointment.getId());
        assertThat(paid.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(reloaded.getStatus()).isEqualTo(AppointmentStatus.PAID);
    }
}
