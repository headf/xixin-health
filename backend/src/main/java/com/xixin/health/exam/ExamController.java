package com.xixin.health.exam;

import com.xixin.health.common.api.ApiResponse;
import com.xixin.health.iam.UserAccount;
import com.xixin.health.iam.UserAccountRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.jdbc.core.JdbcTemplate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/exams")
public class ExamController {

    private final ExamService examService;
    private final UserAccountRepository userAccountRepository;
    private final JdbcTemplate jdbc;

    public ExamController(ExamService examService, UserAccountRepository userAccountRepository, JdbcTemplate jdbc) {
        this.examService = examService;
        this.userAccountRepository = userAccountRepository;
        this.jdbc = jdbc;
    }

    // ==================== 签到 (Nurse / Front Desk) ====================

    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('NURSE', 'FRONT_DESK')")
    public ApiResponse<ExamRecord> checkIn(@Valid @RequestBody CheckInRequest request) {
        return ApiResponse.success(examService.checkIn(
            request.appointmentId(), request.userId(),
            request.profileId(), request.branchId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<ExamRecord> getById(@PathVariable Long id) {
        return ApiResponse.success(examService.getExamRecord(id));
    }

    @GetMapping("/branch/{branchId}")
    public ApiResponse<List<ExamRecord>> getByBranch(@PathVariable Long branchId) {
        return ApiResponse.success(examService.getExamRecordsByBranch(branchId));
    }

    /** 已签到患者列表 — 同时携带患者姓名 */
    @GetMapping("/checked-in/{branchId}")
    public ApiResponse<List<CheckedInRecord>> getCheckedIn(@PathVariable Long branchId) {
        List<ExamRecord> records = examService.getCheckedInRecords(branchId);
        List<CheckedInRecord> result = records.stream().map(r -> {
            String profileName = "未知";
            try {
                profileName = jdbc.queryForObject(
                    "SELECT COALESCE(hp.name, u.display_name) FROM t_exam_record er " +
                    "JOIN t_appointment a ON er.appointment_id = a.id " +
                    "LEFT JOIN t_health_profile hp ON a.profile_id = hp.id " +
                    "JOIN sys_user u ON er.user_id = u.id " +
                    "WHERE er.id = ?", String.class, r.getId());
            } catch (Exception ignored) {}
            return new CheckedInRecord(r.getId(), r.getExamNo(), r.getUserId(), profileName,
                r.getPackageId(), r.getPackageName(), r.getStatus().name(),
                r.getCheckedInAt(), r.getCreatedAt());
        }).toList();
        return ApiResponse.success(result);
    }

    /** 已完成体检患者列表 */
    @GetMapping("/completed/{branchId}")
    public ApiResponse<List<CheckedInRecord>> getCompleted(@PathVariable Long branchId) {
        List<ExamRecord> records = examService.getCompletedRecords(branchId);
        List<CheckedInRecord> result = records.stream().map(r -> {
            String profileName = "未知";
            try {
                profileName = jdbc.queryForObject(
                    "SELECT COALESCE(hp.name, u.display_name) FROM t_exam_record er " +
                    "JOIN t_appointment a ON er.appointment_id = a.id " +
                    "LEFT JOIN t_health_profile hp ON a.profile_id = hp.id " +
                    "JOIN sys_user u ON er.user_id = u.id " +
                    "WHERE er.id = ?", String.class, r.getId());
            } catch (Exception ignored) {}
            return new CheckedInRecord(r.getId(), r.getExamNo(), r.getUserId(), profileName,
                r.getPackageId(), r.getPackageName(), r.getStatus().name(),
                r.getCheckedInAt(), r.getCreatedAt());
        }).toList();
        return ApiResponse.success(result);
    }

    /** 已签到患者详细列表（含生命体征、排队状态） */
    @GetMapping("/checked-in-with-details/{branchId}")
    public ApiResponse<List<CheckedInDetail>> getCheckedInWithDetails(@PathVariable Long branchId) {
        List<ExamRecord> records = examService.getCheckedInRecords(branchId);
        List<CheckedInDetail> details = records.stream().map(r -> {
            boolean hasVitals = examService.hasVitalSigns(r.getId());
            boolean hasQueueInOtherDept = examService.hasActiveQueueInOtherDept(r.getId(), null);
            String pName = "未知";
            try {
                pName = jdbc.queryForObject(
                    "SELECT COALESCE(hp.name, u.display_name) FROM t_exam_record er " +
                    "LEFT JOIN t_appointment a ON er.appointment_id = a.id " +
                    "LEFT JOIN t_health_profile hp ON a.profile_id = hp.id " +
                    "JOIN sys_user u ON er.user_id = u.id " +
                    "WHERE er.id = ?", String.class, r.getId());
            } catch (Exception ignored) {}
            return new CheckedInDetail(r, hasVitals, hasQueueInOtherDept, pName);
        }).toList();
        return ApiResponse.success(details);
    }

    /** 待总检审核列表 */
    @GetMapping("/pending-review/{branchId}")
    public ApiResponse<List<CheckedInRecord>> getPendingReview(@PathVariable Long branchId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
            "SELECT er.id, er.exam_no, er.user_id, er.package_id, er.package_name, er.status, " +
            "  er.checked_in_at, er.created_at, " +
            "  COALESCE(hp.name, u.display_name) AS patient_name " +
            "FROM t_exam_record er " +
            "JOIN t_exam_completion ec ON ec.exam_record_id = er.id " +
            "LEFT JOIN t_appointment a ON er.appointment_id = a.id " +
            "LEFT JOIN t_health_profile hp ON a.profile_id = hp.id " +
            "JOIN sys_user u ON er.user_id = u.id " +
            "WHERE er.branch_id = ? AND er.status = 'IN_PROGRESS' AND ec.needs_review = TRUE " +
            "ORDER BY ec.confirmed_at ASC", branchId);
        List<CheckedInRecord> result = rows.stream().map(r -> new CheckedInRecord(
            ((Number) r.get("id")).longValue(),
            (String) r.get("exam_no"),
            ((Number) r.get("user_id")).longValue(),
            (String) r.get("patient_name"),
            r.get("package_id") != null ? ((Number) r.get("package_id")).longValue() : null,
            (String) r.get("package_name"),
            (String) r.get("status"),
            toLocalDateTime(r.get("checked_in_at")),
            toLocalDateTime(r.get("created_at"))
        )).toList();
        return ApiResponse.success(result);
    }

    /** 将 Object 转为 LocalDateTime（兼容 Timestamp 和 LocalDateTime） */
    private static LocalDateTime toLocalDateTime(Object val) {
        if (val == null) return null;
        if (val instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        if (val instanceof LocalDateTime ldt) return ldt;
        return null;
    }

    public record CheckedInRecord(Long id, String examNo, Long userId, String patientName,
                                  Long packageId, String packageName, String status,
                                  LocalDateTime checkedInAt, LocalDateTime createdAt) {}

    public record CheckedInDetail(ExamRecord record, boolean hasVitalSigns, boolean hasOtherDeptQueue, String patientName) {}

    // ==================== 生命体征 (Nurse) ====================

    @PostMapping("/vital-signs")
    @PreAuthorize("hasRole('NURSE')")
    public ApiResponse<VitalSigns> recordVitalSigns(@Valid @RequestBody VitalSignsRequest request) {
        return ApiResponse.success(examService.recordVitalSigns(
            request.examRecordId(), request.recordedBy(),
            request.height(), request.weight(),
            request.bloodPressureSystolic(), request.bloodPressureDiastolic(),
            request.heartRate(), request.temperature(),
            request.visionLeft(), request.visionRight(),
            request.waistline()));
    }

    @GetMapping("/{examRecordId}/vital-signs")
    public ApiResponse<VitalSigns> getVitalSigns(@PathVariable Long examRecordId) {
        return ApiResponse.success(examService.getVitalSigns(examRecordId));
    }

    // ==================== 排队叫号 (Nurse) ====================

    @PostMapping("/queue/add")
    @PreAuthorize("hasAnyRole('NURSE', 'EXAM_DOCTOR')")
    public ApiResponse<DepartmentQueue> addToQueue(@Valid @RequestBody QueueAddRequest request) {
        return ApiResponse.success(examService.addToQueue(request.examRecordId(), request.departmentId()));
    }

    @GetMapping("/queue/{departmentId}")
    public ApiResponse<List<DepartmentQueue>> getQueue(@PathVariable Long departmentId) {
        return ApiResponse.success(examService.getQueueByDepartment(departmentId));
    }

    @GetMapping("/queue/with-names/{departmentId}")
    public ApiResponse<List<Map<String, Object>>> getQueueWithNames(@PathVariable Long departmentId) {
        List<DepartmentQueue> queue = examService.getQueueByDepartment(departmentId);
        List<Map<String, Object>> result = queue.stream().map(q -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", q.getId());
            m.put("examRecordId", q.getExamRecordId());
            m.put("departmentId", q.getDepartmentId());
            m.put("queueNo", q.getQueueNo());
            m.put("queueStatus", q.getQueueStatus());
            m.put("createdAt", q.getCreatedAt());
            String patientName = "未知";
            try {
                var er = examService.getExamRecord(q.getExamRecordId());
                if (er != null) {
                    m.put("userId", er.getUserId());
                    patientName = jdbc.queryForObject(
                        "SELECT COALESCE(hp.name, u.display_name) FROM t_exam_record er " +
                        "JOIN t_appointment a ON er.appointment_id = a.id " +
                        "LEFT JOIN t_health_profile hp ON a.profile_id = hp.id " +
                        "JOIN sys_user u ON er.user_id = u.id " +
                        "WHERE er.id = ?", String.class, er.getId());
                }
            } catch (Exception ignored) {}
            m.put("patientName", patientName);
            return m;
        }).toList();
        return ApiResponse.success(result);
    }

    @GetMapping("/queue/{departmentId}/waiting")
    public ApiResponse<List<DepartmentQueue>> getWaitingQueue(@PathVariable Long departmentId) {
        return ApiResponse.success(examService.getWaitingQueue(departmentId));
    }

    @GetMapping("/queue/{departmentId}/waiting-count")
    public ApiResponse<Map<String, Long>> getWaitingCount(@PathVariable Long departmentId) {
        return ApiResponse.success(Map.of("count", examService.getWaitingCount(departmentId)));
    }

    @GetMapping("/queue/stats/{departmentId}")
    public ApiResponse<Map<String, Object>> getDepartmentStats(@PathVariable Long departmentId) {
        return ApiResponse.success(examService.getDepartmentStats(departmentId));
    }

    @GetMapping("/queue/stats/all")
    public ApiResponse<Map<Long, Map<String, Object>>> getAllDepartmentStats() {
        return ApiResponse.success(examService.getAllDepartmentStats());
    }

    @GetMapping("/queue/today")
    public ApiResponse<List<DepartmentQueue>> getTodayQueueRegistrations() {
        return ApiResponse.success(examService.getTodayQueueRegistrations());
    }

    @PostMapping("/queue/call-next/{departmentId}")
    @PreAuthorize("hasAnyRole('NURSE', 'EXAM_DOCTOR')")
    public ApiResponse<DepartmentQueue> callNext(@PathVariable Long departmentId) {
        return ApiResponse.success(examService.callNext(departmentId));
    }

    @PutMapping("/queue/{queueId}/arrived")
    @PreAuthorize("hasAnyRole('NURSE', 'EXAM_DOCTOR')")
    public ApiResponse<DepartmentQueue> markArrived(@PathVariable Long queueId) {
        return ApiResponse.success(examService.markArrived(queueId));
    }

    @PutMapping("/queue/{queueId}/complete")
    @PreAuthorize("hasAnyRole('NURSE', 'EXAM_DOCTOR')")
    public ApiResponse<DepartmentQueue> completeQueue(@PathVariable Long queueId) {
        return ApiResponse.success(examService.completeQueue(queueId));
    }

    @PutMapping("/queue/complete-by-exam/{examRecordId}/{departmentId}")
    @PreAuthorize("hasAnyRole('NURSE', 'EXAM_DOCTOR')")
    public ApiResponse<DepartmentQueue> completeQueueByExam(@PathVariable Long examRecordId, @PathVariable Long departmentId) {
        return ApiResponse.success(examService.completeQueueByExamAndDept(examRecordId, departmentId));
    }

    @PutMapping("/queue/{queueId}/re-queue")
    @PreAuthorize("hasAnyRole('NURSE', 'EXAM_DOCTOR')")
    public ApiResponse<DepartmentQueue> reQueue(@PathVariable Long queueId) {
        return ApiResponse.success(examService.reQueue(queueId));
    }

    // ==================== 体检进度 (Doctor / Nurse) ====================

    @PostMapping("/progress/init")
    @PreAuthorize("hasAnyRole('NURSE', 'EXAM_DOCTOR')")
    public ApiResponse<List<ExamProgress>> initProgress(@Valid @RequestBody InitProgressRequest request) {
        return ApiResponse.success(examService.initProgress(
            request.examRecordId(), request.itemIds(), request.departmentId()));
    }

    @GetMapping("/{examRecordId}/progress")
    public ApiResponse<List<ExamService.ExamProgressVO>> getProgress(@PathVariable Long examRecordId) {
        return ApiResponse.success(examService.getExamProgress(examRecordId));
    }

    @GetMapping("/progress/pending/{departmentId}")
    @PreAuthorize("hasAnyRole('EXAM_DOCTOR', 'NURSE')")
    public ApiResponse<List<ExamProgress>> getPendingProgress(@PathVariable Long departmentId) {
        return ApiResponse.success(examService.getDepartmentPendingProgress(departmentId));
    }

    @PutMapping("/progress/{progressId}/{action}")
    @PreAuthorize("hasAnyRole('EXAM_DOCTOR', 'NURSE')")
    public ApiResponse<ExamProgress> updateProgress(@PathVariable Long progressId, @PathVariable String action) {
        return ApiResponse.success(examService.updateProgressStatus(progressId, action));
    }

    @PostMapping("/{examRecordId}/complete")
    @PreAuthorize("hasAnyRole('EXAM_DOCTOR', 'NURSE')")
    public ApiResponse<ExamRecord> completeExam(@PathVariable Long examRecordId) {
        return ApiResponse.success(examService.completeExam(examRecordId));
    }

    // ==================== 标本管理 (Nurse: 采血/留尿/送检) ====================

    @PostMapping("/specimen/collect")
    @PreAuthorize("hasRole('NURSE')")
    public ApiResponse<Specimen> collectSpecimen(@Valid @RequestBody CollectSpecimenRequest request) {
        return ApiResponse.success(examService.collectSpecimen(
            request.examRecordId(), request.specimenType(), request.collectedBy()));
    }

    @PutMapping("/specimen/{specimenId}/label")
    @PreAuthorize("hasRole('NURSE')")
    public ApiResponse<Specimen> labelSpecimen(@PathVariable Long specimenId, @RequestBody Map<String, String> body) {
        return ApiResponse.success(examService.labelSpecimen(specimenId, body.get("barcode")));
    }

    @PutMapping("/specimen/{specimenId}/send")
    @PreAuthorize("hasRole('NURSE')")
    public ApiResponse<Specimen> sendSpecimen(@PathVariable Long specimenId, @RequestBody Map<String, Long> body) {
        return ApiResponse.success(examService.sendSpecimenToLab(specimenId, body.get("sentBy")));
    }

    // ==================== 标本接收确认 (检验科医生/护士) ====================

    @PutMapping("/specimen/{specimenId}/receive")
    @PreAuthorize("hasAnyRole('EXAM_DOCTOR_LAB', 'NURSE')")
    public ApiResponse<Specimen> receiveSpecimen(@PathVariable Long specimenId, @RequestBody Map<String, Long> body) {
        return ApiResponse.success(examService.receiveSpecimen(specimenId, body.get("receivedBy")));
    }

    @PutMapping("/specimen/{specimenId}/reject")
    @PreAuthorize("hasAnyRole('EXAM_DOCTOR_LAB', 'NURSE')")
    public ApiResponse<Specimen> rejectSpecimen(@PathVariable Long specimenId, @RequestBody Map<String, Object> body) {
        Long rejectedBy = Long.valueOf(body.get("rejectedBy").toString());
        String reason = (String) body.get("reason");
        return ApiResponse.success(examService.rejectSpecimen(specimenId, rejectedBy, reason));
    }

    @GetMapping("/{examRecordId}/specimens")
    public ApiResponse<List<Specimen>> getSpecimens(@PathVariable Long examRecordId) {
        return ApiResponse.success(examService.getSpecimensByExam(examRecordId));
    }

    @GetMapping("/specimens/pending")
    public ApiResponse<List<Specimen>> getPendingSpecimens() {
        return ApiResponse.success(examService.getPendingSpecimens());
    }

    // ==================== 收尾确认 (Nurse: 核对无漏检) ====================

    @PostMapping("/{examRecordId}/confirm-completion")
    @PreAuthorize("hasRole('NURSE')")
    public ApiResponse<ExamCompletion> confirmCompletion(
            @PathVariable Long examRecordId, @RequestBody Map<String, Long> body) {
        return ApiResponse.success(examService.confirmCompletion(examRecordId, body.get("nurseId")));
    }

    /** 强制完成（跳过所有校验，仅供测试） */
    @PostMapping("/{examRecordId}/force-complete")
    @PreAuthorize("hasRole('NURSE')")
    public ApiResponse<ExamCompletion> forceComplete(
            @PathVariable Long examRecordId, @RequestBody Map<String, Long> body) {
        return ApiResponse.success(examService.forceComplete(examRecordId, body.get("nurseId")));
    }

    @GetMapping("/{examRecordId}/completion-status")
    public ApiResponse<ExamCompletion> getCompletionStatus(@PathVariable Long examRecordId) {
        return ApiResponse.success(examService.getCompletionStatus(examRecordId));
    }

    // ==================== 科室结论 (Doctor: 写单项结论) ====================

    @PutMapping("/progress/{progressId}/conclusion")
    @PreAuthorize("hasRole('EXAM_DOCTOR')")
    public ApiResponse<ExamProgress> writeDepartmentConclusion(
            @PathVariable Long progressId, @RequestBody Map<String, String> body) {
        return ApiResponse.success(examService.writeDepartmentConclusion(progressId, body.get("conclusion")));
    }

    // ==================== 统计概览 ====================

    @GetMapping("/dashboard/{branchId}")
    public ApiResponse<Map<String, Object>> dashboard(@PathVariable Long branchId) {
        return ApiResponse.success(examService.getDashboardStats(branchId));
    }

    // ==================== Request DTOs ====================

    public record CheckInRequest(
        @NotNull Long appointmentId,
        @NotNull Long userId,
        @NotNull Long profileId,
        @NotNull Long branchId
    ) {}

    public record VitalSignsRequest(
        @NotNull Long examRecordId,
        @NotNull Long recordedBy,
        BigDecimal height,
        BigDecimal weight,
        Integer bloodPressureSystolic,
        Integer bloodPressureDiastolic,
        Integer heartRate,
        BigDecimal temperature,
        BigDecimal visionLeft,
        BigDecimal visionRight,
        BigDecimal waistline
    ) {}

    public record QueueAddRequest(
        @NotNull Long examRecordId,
        @NotNull Long departmentId
    ) {}

    public record InitProgressRequest(
        @NotNull Long examRecordId,
        @NotNull List<Long> itemIds,
        Long departmentId
    ) {}

    public record CollectSpecimenRequest(
        @NotNull Long examRecordId,
        @NotNull String specimenType,  // BLOOD / URINE
        @NotNull Long collectedBy
    ) {}
}
