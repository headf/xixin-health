package com.xixin.health.appointment;

import com.xixin.health.common.api.ApiResponse;
import com.xixin.health.iam.RoleRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {

    private final RoleRepository roleRepository;
    private final JdbcTemplate jdbc;

    public PatientController(RoleRepository roleRepository, JdbcTemplate jdbc) {
        this.roleRepository = roleRepository;
        this.jdbc = jdbc;
    }

    @GetMapping
    public ApiResponse<List<PatientInfo>> listPatients(
            @RequestParam(required = false) Long branchId) {
        var normalRole = roleRepository.findByCode("NORMAL_USER").orElse(null);
        if (normalRole == null) {
            return ApiResponse.success(List.of());
        }

        String sql = "SELECT a.user_id, COALESCE(hp.name, u.display_name) AS patient_name, u.phone, " +
            "1 AS has_paid " +
            "FROM t_appointment a " +
            "JOIN sys_user u ON a.user_id = u.id " +
            "LEFT JOIN t_health_profile hp ON a.profile_id = hp.id " +
            "WHERE a.appointment_date = ? AND a.deleted = FALSE AND u.enabled = TRUE AND u.deleted = FALSE " +
            "AND a.status = 'PAID' " +
            // 只返回纯患者（排除医护人员和管理员）
            "AND u.id NOT IN ( " +
            "  SELECT ur.user_id FROM sys_user_role ur " +
            "  JOIN sys_role r ON ur.role_id = r.id " +
            "  WHERE r.role_type IN ('MEDICAL', 'ADMIN', 'BUSINESS') " +
            ") ";

        // 如果指定了分院ID，按分院过滤
        if (branchId != null) {
            sql += "AND a.branch_id = ? ";
        }
        sql += "GROUP BY a.user_id, a.profile_id, hp.name, u.display_name, u.phone ORDER BY a.user_id";

        var rows = (branchId != null)
            ? jdbc.queryForList(sql, LocalDate.now(), branchId)
            : jdbc.queryForList(sql, LocalDate.now());

        List<PatientInfo> patients = rows.stream()
            .map(r -> new PatientInfo(
                ((Number) r.get("user_id")).longValue(),
                (String) r.get("patient_name"),
                (String) r.get("phone"),
                ((Number) r.get("has_paid")).intValue() == 1
            ))
            .sorted(Comparator.comparing(PatientInfo::id))
            .collect(Collectors.toList());

        return ApiResponse.success(patients);
    }

    public record PatientInfo(Long id, String name, String phone, boolean hasPaid) {}
}
