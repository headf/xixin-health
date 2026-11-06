package com.xixin.health.appointment;

import com.xixin.health.common.api.ApiResponse;
import com.xixin.health.exam.ExamRecordRepository;
import com.xixin.health.exam.ExamStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final AppointmentRepository appointmentRepository;
    private final ExamRecordRepository examRecordRepository;

    public DashboardController(AppointmentRepository appointmentRepository,
                                ExamRecordRepository examRecordRepository) {
        this.appointmentRepository = appointmentRepository;
        this.examRecordRepository = examRecordRepository;
    }

    @GetMapping("/nurse/overview")
    public ApiResponse<Map<String, Object>> nurseOverview() {
        LocalDate today = LocalDate.now();

        long totalAppointments = appointmentRepository.countByAppointmentDate(today);
        long paidAppointments = appointmentRepository.countByAppointmentDateAndStatus(today, AppointmentStatus.PAID);
        long checkedInAppointments = appointmentRepository.countByAppointmentDateAndStatus(today, AppointmentStatus.CHECKED_IN);

        return ApiResponse.success(Map.of(
            "totalAppointments", totalAppointments,
            "paidAppointments", paidAppointments,
            "checkedInAppointments", checkedInAppointments,
            "date", today.toString()
        ));
    }
}
