package com.xixin.health.exam;

import com.xixin.health.common.api.ApiResponse;
import com.xixin.health.security.SecurityUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'OPERATIONS_ADMIN')")
public class AdminQueueController {

    private final JdbcTemplate jdbc;
    private final SecurityUtils securityUtils;

    public AdminQueueController(JdbcTemplate jdbc, SecurityUtils securityUtils) {
        this.jdbc = jdbc;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/departments/{deptId}/queue")
    public ApiResponse<List<Map<String, Object>>> getDepartmentQueue(@PathVariable Long deptId) {
        List<Map<String, Object>> queue = jdbc.queryForList(
            "SELECT dq.id, dq.exam_record_id AS examRecordId, dq.department_id AS departmentId, " +
            "dq.queue_no AS queueNo, dq.queue_status AS queueStatus, dq.created_at AS createdAt, " +
            "u.display_name AS patientName, u.phone AS patientPhone " +
            "FROM t_department_queue dq " +
            "LEFT JOIN t_exam_record er ON er.id = dq.exam_record_id " +
            "LEFT JOIN sys_user u ON u.id = er.user_id " +
            "WHERE dq.department_id = ? " +
            "ORDER BY dq.created_at ASC", deptId);
        return ApiResponse.success(queue);
    }

    @GetMapping("/departments/queue/stats")
    public ApiResponse<List<Map<String, Object>>> getQueueStats() {
        Long branchId = securityUtils.getCurrentBranchId();
        String sql;
        if (branchId != null) {
            sql = "SELECT d.id AS departmentId, d.department_name AS departmentName, " +
                "b.branch_name AS branchName, " +
                "COALESCE(SUM(CASE WHEN dq.queue_status='WAITING' THEN 1 ELSE 0 END),0) AS waiting, " +
                "COALESCE(SUM(CASE WHEN dq.queue_status='CALLED' THEN 1 ELSE 0 END),0) AS called, " +
                "COALESCE(SUM(CASE WHEN dq.queue_status='COMPLETED' THEN 1 ELSE 0 END),0) AS completed " +
                "FROM t_department d " +
                "LEFT JOIN t_branch b ON b.id = d.branch_id " +
                "LEFT JOIN t_department_queue dq ON dq.department_id = d.id " +
                "WHERE d.branch_id = ? " +
                "GROUP BY d.id, d.department_name, b.branch_name " +
                "ORDER BY d.id";
            List<Map<String, Object>> stats = jdbc.queryForList(sql, branchId);
            return ApiResponse.success(stats);
        } else {
            sql = "SELECT d.id AS departmentId, d.department_name AS departmentName, " +
                "b.branch_name AS branchName, " +
                "COALESCE(SUM(CASE WHEN dq.queue_status='WAITING' THEN 1 ELSE 0 END),0) AS waiting, " +
                "COALESCE(SUM(CASE WHEN dq.queue_status='CALLED' THEN 1 ELSE 0 END),0) AS called, " +
                "COALESCE(SUM(CASE WHEN dq.queue_status='COMPLETED' THEN 1 ELSE 0 END),0) AS completed " +
                "FROM t_department d " +
                "LEFT JOIN t_branch b ON b.id = d.branch_id " +
                "LEFT JOIN t_department_queue dq ON dq.department_id = d.id " +
                "GROUP BY d.id, d.department_name, b.branch_name " +
                "ORDER BY d.id";
            List<Map<String, Object>> stats = jdbc.queryForList(sql);
            return ApiResponse.success(stats);
        }
    }

    @PatchMapping("/department-queues/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateQueueStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("queueStatus");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.failure("VALIDATION_ERROR", "queueStatus is required"));
        }
        int rows = jdbc.update(
            "UPDATE t_department_queue SET queue_status = ? WHERE id = ?", status, id);
        if (rows == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
