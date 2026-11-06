package com.xixin.health.exam;

import com.xixin.health.common.api.ApiResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/guide-sheets")
public class GuideSheetController {

    private final JdbcTemplate jdbc;

    public GuideSheetController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 查询指定套餐包含的体检科室ID列表 */
    private List<Long> getDepartmentIdsByPackage(Long packageId) {
        if (packageId == null) return List.of();
        return jdbc.queryForList(
            "SELECT DISTINCT ei.department_id FROM t_exam_package_item epi " +
            "JOIN t_exam_item ei ON epi.item_id = ei.id " +
            "WHERE epi.package_id = ? AND ei.department_id IS NOT NULL",
            packageId
        ).stream().map(row -> ((Number) row.get("department_id")).longValue())
         .filter(Objects::nonNull)
         .distinct()
         .collect(Collectors.toList());
    }

    /**
     * 查询或自动生成导检单
     *
     * SECURITY: 使用 JdbcTemplate 进行只读查询，所有参数均通过 ? 占位符传入，
     * 有效防止 SQL 注入。该方法不做任何写操作（INSERT/UPDATE/DELETE）。
     *
     * TODO: 后续可考虑创建 GuideSheet JPA Entity 和 Repository 替代裸 SQL 查询，
     * 以获得类型安全和更好的可维护性。
     */
    @GetMapping("/by-exam/{examRecordId}")
    public ApiResponse<Map<String, Object>> getByExamRecord(@PathVariable Long examRecordId) {
        // 先查已有导检单
        List<Map<String, Object>> existing = jdbc.queryForList(
            "SELECT gs.id, gs.guide_no, gs.route_text, gs.created_at," +
            "  er.id AS exam_record_id, er.exam_no," +
            "  COALESCE(hp.name, u.display_name) AS patient_name," +
            "  p.id AS package_id, p.package_name " +
            "FROM t_guide_sheet gs " +
            "JOIN t_exam_record er ON gs.exam_record_id = er.id " +
            "LEFT JOIN t_appointment a ON er.appointment_id = a.id " +
            "LEFT JOIN t_health_profile hp ON a.profile_id = hp.id " +
            "LEFT JOIN sys_user u ON er.user_id = u.id " +
            "LEFT JOIN t_exam_package p ON er.package_id = p.id " +
            "WHERE gs.exam_record_id = ?", examRecordId);
        if (!existing.isEmpty()) {
            Map<String, Object> result = new LinkedHashMap<>(existing.get(0));
            Object pid = result.get("package_id");
            result.put("departmentIds", pid != null ? getDepartmentIdsByPackage(((Number) pid).longValue()) : List.of());
            return ApiResponse.success(result);
        }

        // 不存在则自动查询体检记录信息返回（不创建导检单记录）
        List<Map<String, Object>> rows = jdbc.queryForList(
            "SELECT er.id AS exam_record_id, er.exam_no," +
            "  COALESCE(hp.name, u.display_name) AS patient_name," +
            "  p.id AS package_id, p.package_name " +
            "FROM t_exam_record er " +
            "LEFT JOIN t_appointment a ON er.appointment_id = a.id " +
            "LEFT JOIN t_health_profile hp ON a.profile_id = hp.id " +
            "LEFT JOIN sys_user u ON er.user_id = u.id " +
            "LEFT JOIN t_exam_package p ON er.package_id = p.id " +
            "WHERE er.id = ?", examRecordId);

        if (rows.isEmpty()) return ApiResponse.success(null);

        Map<String, Object> result = new LinkedHashMap<>(rows.get(0));
        result.put("guide_no", result.getOrDefault("exam_no", ""));
        Object pid2 = result.get("package_id");
        result.put("departmentIds", pid2 != null ? getDepartmentIdsByPackage(((Number) pid2).longValue()) : List.of());
        return ApiResponse.success(result);
    }
}
