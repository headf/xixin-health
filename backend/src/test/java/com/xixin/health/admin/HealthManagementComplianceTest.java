package com.xixin.health.admin;

import com.xixin.health.management.HealthManagement;
import com.xixin.health.management.HealthManagementRepository;
import com.xixin.health.report.ExamReport;
import com.xixin.health.report.ReportService;
import com.xixin.health.result.CheckResult;
import com.xixin.health.result.ResultService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HealthManagementComplianceTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ResultService resultService;

    @Autowired
    ReportService reportService;

    @Autowired
    HealthManagementRepository healthManagementRepository;

    @Test
    void criticalReportCreatesHighRiskManagementRecord() {
        ExamReport report = createCriticalReport(101L);

        assertThat(healthManagementRepository.findByReportId(report.getId()))
            .isPresent()
            .get()
            .extracting(HealthManagement::getRiskLevel)
            .isEqualTo("HIGH");
    }

    @Test
    void followUpPlanCreatesNotification() throws Exception {
        HealthManagement management = healthManagementRepository.findByReportId(createCriticalReport(102L).getId()).orElseThrow();

        mockMvc.perform(post("/api/v1/admin/health-management/{id}/follow-up", management.getId())
                .with(adminWith("ROLE_REVIEW_DOCTOR", "REPORT_REVIEW"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "followUpPlan":"2026-07-23 复查空腹血糖",
                      "interventionPlan":"低糖饮食，每周运动三次"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.managementStatus").value("FOLLOW_UP_PLANNED"));
    }

    private ExamReport createCriticalReport(Long examRecordId) {
        CheckResult result = resultService.recordNumericResult(
            examRecordId,
            1L,
            "Blood Glucose",
            "mmol/L",
            12.2,
            3.9,
            6.1,
            11.1,
            Double.NaN,
            2L
        );
        resultService.reviewResult(result.getId(), 3L, true, "critical confirmed");
        return reportService.generateReport(examRecordId, 3L);
    }

    private RequestPostProcessor adminWith(String... authorities) {
        return user("health-admin").authorities(Stream.of(authorities)
            .map(SimpleGrantedAuthority::new)
            .toList());
    }
}
