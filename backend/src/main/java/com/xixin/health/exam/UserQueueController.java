package com.xixin.health.exam;

import com.xixin.health.common.api.ApiResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/exams")
public class UserQueueController {

    private final JdbcTemplate jdbc;

    public UserQueueController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/queue/my-view")
    public ApiResponse<List<Map<String, Object>>> getMyQueueView(@RequestParam Long userId) {
        // Get all departments under the user's active exam record, along with queue stats
        List<Map<String, Object>> result = jdbc.queryForList(
            "SELECT d.id AS department_id, d.department_name, d.location, d.fasting_required, " +
            "  COALESCE(qs.waiting, 0) AS waiting, COALESCE(qs.completed, 0) AS completed " +
            "FROM t_exam_record er " +
            "JOIN t_exam_package p ON er.package_id = p.id " +
            "JOIN t_exam_package_item epi ON epi.package_id = p.id " +
            "JOIN t_exam_item ei ON epi.item_id = ei.id " +
            "JOIN t_department d ON ei.department_id = d.id " +
            "LEFT JOIN (SELECT department_id, " +
            "  SUM(CASE WHEN queue_status = 'WAITING' THEN 1 ELSE 0 END) AS waiting, " +
            "  SUM(CASE WHEN queue_status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed " +
            "  FROM t_department_queue GROUP BY department_id) qs ON d.id = qs.department_id " +
            "WHERE er.user_id = ? AND er.status = 'CHECKED_IN' " +
            "GROUP BY d.id, d.department_name, d.location, d.fasting_required, qs.waiting, qs.completed " +
            "ORDER BY d.fasting_required DESC, d.id"
        , userId);
        return ApiResponse.success(result);
    }
}
