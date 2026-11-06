package com.xixin.health.exam;

import com.xixin.health.common.api.ApiResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/addons")
public class AddonController {
    private final JdbcTemplate jdbc;

    public AddonController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        jdbc.update("INSERT INTO t_addon_order (exam_record_id, item_id, amount_cent) VALUES (?,?,?)",
                body.get("examRecordId"), body.get("itemId"), body.get("amountCent"));
        return ApiResponse.success(Map.of("id", jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class)));
    }

    @GetMapping("/by-exam/{examRecordId}")
    public ApiResponse<List<Map<String, Object>>> getByExam(@PathVariable Long examRecordId) {
        return ApiResponse.success(jdbc.queryForList(
                "SELECT a.*, ei.item_name FROM t_addon_order a LEFT JOIN t_exam_item ei ON ei.id = a.item_id WHERE a.exam_record_id = ?",
                examRecordId));
    }
}
