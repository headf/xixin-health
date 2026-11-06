package com.xixin.health.analysis;

import com.xixin.health.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/health-analysis")
public class HealthAnalysisController {

    private final HealthAnalysisService healthAnalysisService;

    public HealthAnalysisController(HealthAnalysisService healthAnalysisService) {
        this.healthAnalysisService = healthAnalysisService;
    }

    @PostMapping("/reports/{reportId}/generate")
    public ApiResponse<HealthAnalysis> generate(@PathVariable Long reportId) {
        return ApiResponse.success(healthAnalysisService.generateForReport(reportId));
    }

    @GetMapping
    public ApiResponse<List<HealthAnalysis>> list(@RequestParam Long userId) {
        return ApiResponse.success(healthAnalysisService.listForUser(userId));
    }

    @GetMapping("/trends")
    public ApiResponse<HealthAnalysisService.TrendResponse> trends(@RequestParam Long userId) {
        return ApiResponse.success(healthAnalysisService.trends(userId));
    }
}
