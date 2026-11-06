package com.xixin.health.configuration;

import com.xixin.health.common.api.ApiResponse;
import com.xixin.health.security.SecurityUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/schedules")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'OPERATIONS_ADMIN')")
public class ScheduleController {

    private final DoctorScheduleRepository scheduleRepository;
    private final JdbcTemplate jdbc;
    private final SecurityUtils securityUtils;

    public ScheduleController(DoctorScheduleRepository scheduleRepository, JdbcTemplate jdbc, SecurityUtils securityUtils) {
        this.scheduleRepository = scheduleRepository;
        this.jdbc = jdbc;
        this.securityUtils = securityUtils;
    }

    /**
     * 查询排班列表（含医生、科室、分院名称和余号信息）
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> getAll(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {

        // 分院管理员自动注入branchId，不可查看其他分院
        Long effectiveBranchId = branchId != null ? branchId : securityUtils.getCurrentBranchId();

        StringBuilder countSql = new StringBuilder(
                "SELECT COUNT(*) FROM t_doctor_schedule s WHERE 1=1");
        StringBuilder sql = new StringBuilder(
                "SELECT s.*, u.display_name AS doctor_name, d.department_name, b.branch_name, " +
                "       (s.max_patients - s.booked_count) AS remaining_slots, " +
                "       CASE WHEN s.booked_count >= s.max_patients THEN 'FULL' " +
                "            WHEN (s.max_patients - s.booked_count) <= 3 THEN 'TIGHT' " +
                "            ELSE 'AVAILABLE' END AS slot_status " +
                "FROM t_doctor_schedule s " +
                "LEFT JOIN sys_user u ON u.id = s.doctor_id " +
                "LEFT JOIN t_department d ON d.id = s.department_id " +
                "LEFT JOIN t_branch b ON b.id = s.branch_id WHERE 1=1");

        if (effectiveBranchId != null) {
            sql.append(" AND s.branch_id=").append(effectiveBranchId);
            countSql.append(" AND s.branch_id=").append(effectiveBranchId);
        } else {
            // 默认只查未来30天
            sql.append(" AND s.schedule_date >= CURDATE()");
            countSql.append(" AND s.schedule_date >= CURDATE()");
            sql.append(" AND s.schedule_date <= DATE_ADD(CURDATE(), INTERVAL 30 DAY)");
            countSql.append(" AND s.schedule_date <= DATE_ADD(CURDATE(), INTERVAL 30 DAY)");
        }
        if (date != null && !date.isEmpty()) {
            sql.append(" AND s.schedule_date='").append(date).append("'");
            countSql.append(" AND s.schedule_date='").append(date).append("'");
        }
        sql.append(" ORDER BY s.schedule_date DESC, s.time_slot");
        sql.append(" LIMIT ").append(size).append(" OFFSET ").append(page * size);

        int total = jdbc.queryForObject(countSql.toString(), Integer.class);
        List<Map<String, Object>> list = jdbc.queryForList(sql.toString());
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("content", list);
        result.put("totalElements", total);
        result.put("totalPages", (total + size - 1) / size);
        result.put("number", page);
        result.put("size", size);

        return ApiResponse.success(result);
    }

    /**
     * 获取可选医生列表（有排班权限的医生）
     */
    @GetMapping("/doctors")
    public ApiResponse<List<Map<String, Object>>> getDoctors() {
        List<Map<String, Object>> doctors = jdbc.queryForList(
                "SELECT DISTINCT u.id, u.display_name FROM sys_user u " +
                "INNER JOIN sys_user_role ur ON ur.user_id = u.id " +
                "INNER JOIN sys_role r ON r.id = ur.role_id " +
                "WHERE u.enabled = TRUE AND (r.code LIKE 'EXAM_DOCTOR%' OR r.code = 'REVIEW_DOCTOR') " +
                "ORDER BY u.display_name");
        return ApiResponse.success(doctors);
    }

    /**
     * 创建排班（号源）
     */
    @PostMapping
    @Transactional
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        Long doctorId = Long.valueOf(body.get("doctorId").toString());
        Long branchId = Long.valueOf(body.get("branchId").toString());
        Long departmentId = Long.valueOf(body.get("departmentId").toString());
        LocalDate scheduleDate = LocalDate.parse(body.get("scheduleDate").toString());
        String timeSlot = body.get("timeSlot").toString();
        int maxPatients = Integer.parseInt(body.get("maxPatients").toString());

        // 检查同一分院同一日期同一时段是否已存在排班
        var existing = scheduleRepository.findByBranchIdAndScheduleDateAndTimeSlot(
                branchId, scheduleDate, timeSlot);
        if (existing.isPresent()) {
            return ApiResponse.failure("409", "该分院该日期该时段已存在排班记录");
        }

        DoctorSchedule schedule = new DoctorSchedule(doctorId, branchId, departmentId,
                scheduleDate, timeSlot, maxPatients);
        schedule = scheduleRepository.save(schedule);
        return ApiResponse.success(Map.of("id", schedule.getId()));
    }

    /**
     * 更新排班（时段、最大人数、启用状态）
     */
    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        DoctorSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("排班记录不存在"));

        if (body.containsKey("timeSlot")) {
            schedule.setTimeSlot(body.get("timeSlot").toString());
        }
        if (body.containsKey("maxPatients")) {
            int newMax = Integer.parseInt(body.get("maxPatients").toString());
            if (newMax < schedule.getBookedCount()) {
                return ApiResponse.failure("400",
                        "最大人数不能小于已预约人数(" + schedule.getBookedCount() + ")");
            }
            schedule.setMaxPatients(newMax);
        }
        if (body.containsKey("enabled")) {
            schedule.setEnabled(Boolean.parseBoolean(body.get("enabled").toString()));
        }
        scheduleRepository.save(schedule);
        return ApiResponse.success(null);
    }

    /**
     * 删除排班
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        DoctorSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("排班记录不存在"));
        if (schedule.getBookedCount() > 0) {
            return ApiResponse.failure("400",
                    "该排班已有 " + schedule.getBookedCount() + " 个预约，无法删除");
        }
        scheduleRepository.delete(schedule);
        return ApiResponse.success(null);
    }
}
