package com.xixin.health.analysis;

import com.xixin.health.common.error.BusinessException;
import com.xixin.health.common.error.ErrorCode;
import com.xixin.health.report.ExamReport;
import com.xixin.health.report.ExamReportRepository;
import com.xixin.health.result.AbnormalLevel;
import com.xixin.health.result.CheckResult;
import com.xixin.health.result.CheckResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HealthAnalysisService {

    private final HealthAnalysisRepository healthAnalysisRepository;
    private final ExamReportRepository examReportRepository;
    private final CheckResultRepository checkResultRepository;

    public HealthAnalysisService(
        HealthAnalysisRepository healthAnalysisRepository,
        ExamReportRepository examReportRepository,
        CheckResultRepository checkResultRepository
    ) {
        this.healthAnalysisRepository = healthAnalysisRepository;
        this.examReportRepository = examReportRepository;
        this.checkResultRepository = checkResultRepository;
    }

    @Transactional
    public HealthAnalysis generateForReport(Long reportId) {
        return healthAnalysisRepository.findFirstByReportIdOrderByCreatedAtDesc(reportId)
            .orElseGet(() -> createAnalysis(reportId));
    }

    @Transactional(readOnly = true)
    public List<HealthAnalysis> listForUser(Long userId) {
        return healthAnalysisRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public TrendResponse trends(Long userId) {
        List<HealthAnalysis> analyses = listForUser(userId);
        String trendData = analyses.isEmpty()
            ? "[]"
            : analyses.get(0).getTrendData();
        return new TrendResponse(userId, trendData);
    }

    private HealthAnalysis createAnalysis(Long reportId) {
        ExamReport report = examReportRepository.findById(reportId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Report not found"));
        List<CheckResult> results = checkResultRepository.findByExamRecordId(report.getExamRecordId());
        int abnormalCount = (int) results.stream()
            .filter(result -> result.getAbnormalLevel() == AbnormalLevel.ABNORMAL)
            .count();
        int criticalCount = (int) results.stream()
            .filter(result -> result.getAbnormalLevel() == AbnormalLevel.CRITICAL)
            .count();
        String riskLevel = criticalCount > 0 ? "HIGH" : abnormalCount > 0 ? "MEDIUM" : "LOW";
        String trendData = results.stream()
            .map(result -> "{\"item\":\"" + result.getItemName() + "\",\"level\":\"" + result.getAbnormalLevel() + "\"}")
            .reduce((left, right) -> left + "," + right)
            .map(values -> "[" + values + "]")
            .orElse("[]");
        return healthAnalysisRepository.save(new HealthAnalysis(reportId, report.getUserId(), abnormalCount, criticalCount, trendData, riskLevel));
    }

    public record TrendResponse(Long userId, String trendData) {
    }
}
