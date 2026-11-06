package com.xixin.health.appointment;

import com.xixin.health.common.api.ApiResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import com.xixin.health.configuration.BranchRepository;
import com.xixin.health.configuration.ExamPackageRepository;
import com.xixin.health.iam.UserAccountRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/admin/appointments")
public class AdminAppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final UserAccountRepository userAccountRepository;
    private final ExamPackageRepository examPackageRepository;
    private final BranchRepository branchRepository;
    private final JdbcTemplate jdbc;

    public AdminAppointmentController(AppointmentRepository appointmentRepository,
                                       UserAccountRepository userAccountRepository,
                                       ExamPackageRepository examPackageRepository,
                                       BranchRepository branchRepository,
                                       JdbcTemplate jdbc) {
        this.appointmentRepository = appointmentRepository;
        this.userAccountRepository = userAccountRepository;
        this.examPackageRepository = examPackageRepository;
        this.branchRepository = branchRepository;
        this.jdbc = jdbc;
    }

    @GetMapping
    public ApiResponse<Page<AppointmentVO>> listAppointments(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) Long branchId) {
        Page<Appointment> appointmentPage;
        if (branchId != null) {
            appointmentPage = appointmentRepository.findByBranchIdAndDeletedFalse(
                branchId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
        } else {
            appointmentPage = appointmentRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
        }
        return ApiResponse.success(appointmentPage.map(this::toVO));
    }

    private AppointmentVO toVO(Appointment a) {
        String userName = "未知";
        try {
            userName = jdbc.queryForObject(
                "SELECT COALESCE(hp.name, u.display_name) FROM t_appointment a " +
                "LEFT JOIN t_health_profile hp ON a.profile_id = hp.id " +
                "JOIN sys_user u ON a.user_id = u.id WHERE a.id = ?",
                String.class, a.getId());
        } catch (Exception ignored) {}
        String packageName = examPackageRepository.findById(a.getPackageId())
            .map(p -> p.getPackageName())
            .orElse("未知");
        String branchName = branchRepository.findById(a.getBranchId())
            .map(b -> b.getBranchName())
            .orElse("未知");
        return new AppointmentVO(
            a.getId(), a.getAppointmentNo(), a.getUserId(), a.getProfileId(),
            userName, packageName, branchName,
            a.getAppointmentDate().toString(), a.getTimeSlot(),
            a.getStatus().name(),
            a.getCreatedAt() != null ? a.getCreatedAt().toString() : ""
        );
    }

    public record AppointmentVO(
        Long id,
        String appointmentNo,
        Long userId,
        Long profileId,
        String userName,
        String packageName,
        String branchName,
        String appointmentDate,
        String timeSlot,
        String status,
        String createdAt
    ) {}
}
