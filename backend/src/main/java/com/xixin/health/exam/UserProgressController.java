package com.xixin.health.exam;

import com.xixin.health.common.api.ApiResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/exams")
public class UserProgressController {

    private final JdbcTemplate jdbc;

    public UserProgressController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/my-progress")
    public ApiResponse<List<Map<String, Object>>> getMyProgress(@RequestParam Long userId) {
        // Get all active exam records for this user, grouped by appointment
        List<Map<String, Object>> result = jdbc.queryForList(
            "SELECT \n" +
            "  er.id AS exam_record_id,\n" +
            "  er.appointment_id,\n" +
            "  COALESCE(hp.name, u.display_name) AS profile_name,\n" +
            "  er.status AS exam_status,\n" +
            "  p.package_name,\n" +
            "  a.appointment_date,\n" +
            "  a.time_slot,\n" +
            "  d.id AS department_id,\n" +
            "  d.department_name,\n" +
            "  d.location,\n" +
            "  d.fasting_required,\n" +
            "  COALESCE((SELECT SUM(CASE WHEN dq.queue_status='WAITING' THEN 1 ELSE 0 END) FROM t_department_queue dq WHERE dq.department_id=d.id),0) AS waiting,\n" +
            "  COALESCE((SELECT SUM(CASE WHEN dq.queue_status='COMPLETED' THEN 1 ELSE 0 END) FROM t_department_queue dq WHERE dq.department_id=d.id),0) AS queue_completed,\n" +
            "  CASE \n" +
            "    WHEN COUNT(ep.id) = 0 THEN 'PENDING'\n" +
            "    WHEN SUM(CASE WHEN ep.progress_status = 'COMPLETED' THEN 1 ELSE 0 END) = COUNT(ep.id) THEN 'DONE'\n" +
            "    WHEN SUM(CASE WHEN ep.progress_status = 'COMPLETED' THEN 1 ELSE 0 END) > 0 THEN 'IN_PROGRESS'\n" +
            "    ELSE 'PENDING'\n" +
            "  END AS dept_status,\n" +
            "  COUNT(ep.id) AS total_items,\n" +
            "  SUM(CASE WHEN ep.progress_status = 'COMPLETED' THEN 1 ELSE 0 END) AS done_items\n" +
            "FROM t_exam_record er\n" +
            "JOIN t_appointment a ON er.appointment_id = a.id\n" +
            "JOIN sys_user u ON er.user_id = u.id\n" +
            "LEFT JOIN t_health_profile hp ON a.profile_id = hp.id\n" +
            "JOIN t_exam_package p ON er.package_id = p.id\n" +
            "JOIN t_exam_package_item epi ON epi.package_id = p.id\n" +
            "JOIN t_exam_item ei ON epi.item_id = ei.id\n" +
            "JOIN t_department d ON ei.department_id = d.id\n" +
            "LEFT JOIN t_exam_progress ep ON ep.exam_record_id = er.id AND ep.item_id = ei.id\n" +
            "WHERE er.user_id = ? AND er.status != 'COMPLETED'\n" +
            "GROUP BY er.id, a.id, hp.name, u.display_name, p.package_name, a.appointment_date, a.time_slot, d.id, d.department_name, d.location, d.fasting_required\n" +
            "ORDER BY a.appointment_date, d.fasting_required DESC, d.location"
        , userId);

        return ApiResponse.success(result);
    }
}
