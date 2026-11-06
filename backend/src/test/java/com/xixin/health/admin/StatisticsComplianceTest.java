package com.xixin.health.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StatisticsComplianceTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void businessStatisticsAreAggregatedAndAudited() throws Exception {
        mockMvc.perform(get("/api/v1/admin/statistics/business")
                .param("startDate", "2026-01-01")
                .param("endDate", "2026-12-31")
                .header("X-Trace-Id", "stats-business-audit")
                .with(user("ops").roles("OPERATIONS_ADMIN").authorities(() -> "STATISTICS_QUERY")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("totalAppointments")))
            .andExpect(content().string(containsString("totalRevenueCent")));

        mockMvc.perform(get("/api/v1/admin/audit-logs")
                .param("operatorId", "ops")
                .param("type", "STAT_QUERY")
                .with(user("auditor").roles("SYSTEM_ADMIN").authorities(() -> "AUDIT_QUERY")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("business_statistics")))
            .andExpect(content().string(containsString("stats-business-audit")));
    }

    @Test
    void healthStatisticsAreDesensitized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/statistics/health")
                .param("startDate", "2026-01-01")
                .param("endDate", "2026-12-31")
                .with(user("ops").roles("OPERATIONS_ADMIN").authorities(() -> "STATISTICS_QUERY")))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("phone"))))
            .andExpect(content().string(not(containsString("idCard"))));
    }

    @Test
    void exportSupportsBusinessAndHealthExcelPdf() throws Exception {
        mockMvc.perform(get("/api/v1/admin/statistics/export")
                .param("type", "business")
                .param("format", "EXCEL")
                .with(user("ops").roles("OPERATIONS_ADMIN").authorities(() -> "STATISTICS_EXPORT")))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")));

        MvcResult result = mockMvc.perform(get("/api/v1/admin/statistics/export")
                .param("type", "health")
                .param("format", "PDF")
                .with(user("ops").roles("OPERATIONS_ADMIN").authorities(() -> "STATISTICS_EXPORT")))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
            .andReturn();

        String pdf = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.US_ASCII);
        assertThat(pdf)
            .contains("totalReports")
            .contains("criticalReports")
            .doesNotContain("totalAppointments")
            .doesNotContain("totalRevenueCent");
    }

    @Test
    void statisticsRangeCannotExceedOneYear() throws Exception {
        mockMvc.perform(get("/api/v1/admin/statistics/business")
                .param("startDate", "2025-01-01")
                .param("endDate", "2026-12-31")
                .with(user("ops").roles("OPERATIONS_ADMIN").authorities(() -> "STATISTICS_QUERY")))
            .andExpect(status().isBadRequest());
    }
}
