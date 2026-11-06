package com.xixin.health.report;

import com.xixin.health.common.api.ApiResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/reports")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'OPERATIONS_ADMIN')")
public class AdminReportController {

    private final JdbcTemplate jdbc;

    public AdminReportController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/warnings")
    public ApiResponse<List<Map<String, Object>>> getWarnings() {
        List<Map<String, Object>> warnings = jdbc.queryForList(
            "SELECT r.id, r.report_no AS reportNo, r.exam_record_id AS examRecordId, " +
            "r.user_id AS userId, COALESCE(u.display_name, CONCAT('用户#', r.user_id)) AS patientName, " +
            "r.abnormal_status AS abnormalStatus, r.summary, " +
            "r.generated_at AS generatedAt FROM t_exam_report r " +
            "LEFT JOIN sys_user u ON r.user_id = u.id " +
            "WHERE r.abnormal_status IN ('ABNORMAL', 'CRITICAL') ORDER BY r.generated_at DESC");
        return ApiResponse.success(warnings);
    }
}
