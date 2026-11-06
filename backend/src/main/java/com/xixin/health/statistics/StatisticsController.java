package com.xixin.health.statistics;

import com.xixin.health.audit.AdminAuditRecorder;
import com.xixin.health.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@Deprecated /* 同时暴露 /api/v1/statistics/* 和 /api/v1/admin/statistics/* 两套路径，后者已废弃，请统一使用 /api/v1/admin/statistics/* */
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final AdminAuditRecorder auditRecorder;

    public StatisticsController(StatisticsService statisticsService, AdminAuditRecorder auditRecorder) {
        this.statisticsService = statisticsService;
        this.auditRecorder = auditRecorder;
    }

    @GetMapping({"/api/v1/statistics/business", "/api/v1/admin/statistics/business"})

    public ApiResponse<StatisticsService.BusinessStatistics> business(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) Long branchId,
        @RequestParam(required = false) String dimension,
        HttpServletRequest request
    ) {
        StatisticsService.BusinessStatistics statistics = statisticsService.business(startDate, endDate, branchId, dimension);
        auditRecorder.record("STAT_QUERY", "business_statistics", branchId, request);
        return ApiResponse.success(statistics);
    }

    @GetMapping({"/api/v1/statistics/health", "/api/v1/admin/statistics/health"})

    public ApiResponse<StatisticsService.HealthStatistics> health(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) Long branchId,
        HttpServletRequest request
    ) {
        StatisticsService.HealthStatistics statistics = statisticsService.health(startDate, endDate, branchId);
        auditRecorder.record("STAT_QUERY", "health_statistics", branchId, request);
        return ApiResponse.success(statistics);
    }

    @GetMapping({"/api/v1/statistics/export", "/api/v1/admin/statistics/export"})

    public ResponseEntity<byte[]> export(
        @RequestParam(defaultValue = "business") String type,
        @RequestParam(defaultValue = "CSV") String format,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) Long branchId,
        HttpServletRequest request
    ) {
        StatisticsService.ExportFile file = statisticsService.export(type, format, startDate, endDate, branchId);
        auditRecorder.record("STAT_EXPORT", "statistics_export", type + ":" + format, request);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(file.contentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.filename() + "\"")
            .body(file.content());
    }

    // ========== 医护按分院统计 ==========

    @GetMapping({"/api/v1/statistics/staff", "/api/v1/admin/statistics/staff"})

    public ApiResponse<List<StatisticsService.BranchStaff>> staffByBranch(HttpServletRequest request) {
        auditRecorder.record("STAT_QUERY", "staff_statistics", null, request);
        return ApiResponse.success(statisticsService.getStaffByBranch());
    }
}
