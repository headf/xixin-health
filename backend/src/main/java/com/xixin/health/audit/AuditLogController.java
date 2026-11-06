package com.xixin.health.audit;

import com.xixin.health.common.api.ApiResponse;
import com.xixin.health.common.api.PageResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/v1/admin/audit-logs", "/api/v1/system/audit-logs"})
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'OPERATIONS_ADMIN')")
@Deprecated /* /api/v1/system/audit-logs 已废弃，请统一使用 /api/v1/admin/audit-logs */
public class AuditLogController {

    private final OperationLogRepository operationLogRepository;
    private final JdbcTemplate jdbc;

    public AuditLogController(OperationLogRepository operationLogRepository, JdbcTemplate jdbc) {
        this.operationLogRepository = operationLogRepository;
        this.jdbc = jdbc;
    }

    @GetMapping
    public ApiResponse<PageResult<AuditLogVO>> auditLogs(
        @RequestParam(required = false) String operatorId,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String startTime,
        @RequestParam(required = false) String endTime,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        LocalDateTime start = parseStart(startTime);
        LocalDateTime end = parseEnd(endTime);
        List<OperationLog> logs = operationLogRepository.findAll().stream()
            .filter(log -> operatorId == null || operatorId.isBlank() || operatorId.equals(log.getOperatorId()))
            .filter(log -> type == null || type.isBlank() || type.equals(log.getOperationType()))
            .filter(log -> start == null || !log.getOperationTime().isBefore(start))
            .filter(log -> end == null || !log.getOperationTime().isAfter(end))
            .sorted(Comparator.comparing(OperationLog::getOperationTime).reversed())
            .toList();

        // 批量查询用户显示名
        List<String> userIds = logs.stream().map(OperationLog::getOperatorId).distinct().toList();
        Map<String, String> nameMap = userIds.stream().collect(Collectors.toMap(
            id -> id,
            id -> {
                try {
                    return jdbc.queryForObject(
                        "SELECT COALESCE(display_name, phone) FROM sys_user WHERE id = ? OR phone = ? LIMIT 1",
                        String.class, id, id);
                } catch (Exception e) {
                    return "anonymousUser".equals(id) ? "匿名用户" : id;
                }
            }
        ));

        List<AuditLogVO> vos = logs.stream().map(log -> new AuditLogVO(
            log.getId(), log.getOperationTime(), log.getOperatorId(),
            nameMap.getOrDefault(log.getOperatorId(), log.getOperatorId()),
            log.getOperationType(), log.getTargetType(), log.getTargetId(),
            log.getResult(), log.getDetail(), log.getIpAddress(), log.getTraceId()
        )).toList();

        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, vos.size());
        int toIndex = Math.min(fromIndex + normalizedSize, vos.size());
        return ApiResponse.success(PageResult.of(
            vos.subList(fromIndex, toIndex),
            normalizedPage,
            normalizedSize,
            vos.size()
        ));
    }

    public record AuditLogVO(
        Long id, LocalDateTime operationTime, String operatorId, String operatorName,
        String operationType, String targetType, String targetId,
        String result, String detail, String ipAddress, String traceId
    ) {}

    private LocalDateTime parseStart(String value) {
        return parseDateTime(value, false);
    }

    private LocalDateTime parseEnd(String value) {
        return parseDateTime(value, true);
    }

    private LocalDateTime parseDateTime(String value, boolean endOfDay) {
        if (value == null || value.isBlank()) return null;
        if (value.contains("T")) return LocalDateTime.parse(value);
        LocalDate date = LocalDate.parse(value);
        return endOfDay ? date.atTime(23, 59, 59) : date.atStartOfDay();
    }
}
