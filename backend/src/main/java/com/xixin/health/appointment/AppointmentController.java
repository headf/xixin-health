package com.xixin.health.appointment;

import com.xixin.health.common.api.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import com.xixin.health.configuration.BranchRepository;
import com.xixin.health.configuration.ExamPackageRepository;
import com.xixin.health.iam.UserAccountRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentRepository appointmentRepository;
    private final UserAccountRepository userAccountRepository;
    private final ExamPackageRepository examPackageRepository;
    private final BranchRepository branchRepository;
    private final JdbcTemplate jdbc;

    public AppointmentController(AppointmentService appointmentService,
                                  AppointmentRepository appointmentRepository,
                                  UserAccountRepository userAccountRepository,
                                  ExamPackageRepository examPackageRepository,
                                  BranchRepository branchRepository,
                                       JdbcTemplate jdbc) {
        this.appointmentService = appointmentService;
        this.appointmentRepository = appointmentRepository;
        this.userAccountRepository = userAccountRepository;
        this.examPackageRepository = examPackageRepository;
        this.branchRepository = branchRepository;
        this.jdbc = jdbc;
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<Appointment>> getUserAppointments(@PathVariable Long userId) {
        return ApiResponse.success(appointmentService.getUserAppointments(userId));
    }

    @PostMapping
    public ApiResponse<Appointment> create(@Valid @RequestBody CreateAppointmentRequest request) {
        return ApiResponse.success(appointmentService.createAppointment(
            request.userId(),
            request.profileId(),
            request.packageId(),
            request.branchId(),
            request.appointmentDate(),
            request.timeSlot()
        ));
    }

    public record CreateAppointmentRequest(
        @NotNull Long userId,
        @NotNull Long profileId,
        @NotNull Long packageId,
        @NotNull Long branchId,
        @FutureOrPresent LocalDate appointmentDate,
        @NotBlank String timeSlot
    ) {
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/weekly-trend")
    public ApiResponse<List<AppointmentService.DailyTrend>> weeklyTrend(
            @RequestParam(required = false) Long branchId) {
        return ApiResponse.success(appointmentService.getWeeklyTrend(branchId));
    }

    @GetMapping("/list")
    public ApiResponse<Page<AppointmentVO>> listAppointments(
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        List<Appointment> all = appointmentRepository.findAll().stream()
            .filter(a -> !a.isDeleted())
            .filter(a -> branchId == null || branchId.equals(a.getBranchId()))
            .sorted(Comparator.comparing(Appointment::getAppointmentDate).reversed()
                .thenComparing(Comparator.comparing(Appointment::getId).reversed()))
            .collect(Collectors.toList());

        int start = (int) PageRequest.of(page, size).getOffset();
        int end = Math.min(start + size, all.size());
        List<AppointmentVO> voList = all.subList(Math.min(start, all.size()), end).stream()
            .map(this::toVO)
            .collect(Collectors.toList());

        return ApiResponse.success(new PageImpl<>(voList, PageRequest.of(page, size), all.size()));
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
            userName, packageName, branchName, a.getBranchId(),
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
        Long branchId,
        String appointmentDate,
        String timeSlot,
        String status,
        String createdAt
    ) {
        public AppointmentVO(Long id, String appointmentNo, String userName, String packageName, String branchName, Long branchId, String appointmentDate, String timeSlot, String status, String createdAt) {
            this(id, appointmentNo, null, null, userName, packageName, branchName, branchId, appointmentDate, timeSlot, status, createdAt);
        }
    }
}
