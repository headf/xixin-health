package com.xixin.health.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReportComplianceTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void resetAppointment() {
        jdbc.update("UPDATE t_appointment SET status = 'PAID' WHERE id = 1");
    }

    @Test
    void templatePreviewUsesConfiguredContent() throws Exception {
        mockMvc.perform(get("/api/v1/admin/report-templates/1/preview")
                .param("summary", "Normal")
                .param("advice", "Keep exercise")
                .with(adminWith("ROLE_SYSTEM_ADMIN", "TEMPLATE_MANAGE")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.previewText", containsString("Normal")))
            .andExpect(jsonPath("$.data.previewText", containsString("Keep exercise")));
    }

    @Test
    void criticalReportIsMarkedAndRequiresDoctorPublish() throws Exception {
        long examRecordId = checkIn();
        long resultId = recordCriticalResult(examRecordId);
        approveResult(resultId);

        // Complete all 3 progress items so report can be generated
        MvcResult progressResult = mockMvc.perform(get("/api/v1/exams/{examRecordId}/progress", examRecordId)
                .with(adminWith("ROLE_REVIEW_DOCTOR", "REPORT_REVIEW")))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode progressItems = data(progressResult);
        for (JsonNode item : progressItems) {
            long progressId = item.path("id").asLong();
            mockMvc.perform(put("/api/v1/exams/progress/{progressId}/COMPLETE", progressId)
                    .with(adminWith("ROLE_EXAM_DOCTOR", "RESULT_RECORD")))
                .andExpect(status().isOk());
        }

        MvcResult generated = mockMvc.perform(post("/api/v1/reports/generate")
                .with(adminWith("ROLE_REVIEW_DOCTOR", "REPORT_REVIEW"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"examRecordId":%d,"reviewerId":3}
                    """.formatted(examRecordId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.abnormalStatus").value("CRITICAL"))
            .andReturn();

        assertThat(data(generated).path("templateId").asLong()).isNotNull();
        long reportId = data(generated).path("id").asLong();

        // Publish report
        mockMvc.perform(post("/api/v1/reports/{reportId}/publish", reportId)
                .with(adminWith("ROLE_REVIEW_DOCTOR", "REPORT_REVIEW"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"summary":"正常","doctorAdvice":"保持健康"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.reportStatus").value("PUBLISHED"));
    }

    private long checkIn() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/exams/check-in")
                .with(adminWith("ROLE_FRONT_DESK", "FRONT_DESK_CHECKIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "appointmentId":1,
                      "userId":1,
                      "profileId":1,
                      "branchId":1
                    }
                    """))
            .andExpect(status().isOk())
            .andReturn();
        return data(result).path("id").asLong();
    }

    private long recordCriticalResult(long examRecordId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/results/numeric")
                .with(adminWith("ROLE_EXAM_DOCTOR", "RESULT_RECORD"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "examRecordId":%d,
                      "itemId":1,
                      "itemName":"Blood Glucose",
                      "unit":"mmol/L",
                      "numericValue":12.2,
                      "referenceMin":3.9,
                      "referenceMax":6.1,
                      "criticalMax":11.1,
                      "recorderId":2
                    }
                    """.formatted(examRecordId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.abnormalLevel").value("CRITICAL"))
            .andReturn();
        return data(result).path("id").asLong();
    }

    private void approveResult(long resultId) throws Exception {
        mockMvc.perform(put("/api/v1/results/{id}/review", resultId)
                .with(adminWith("ROLE_REVIEW_DOCTOR", "REPORT_REVIEW"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"reviewerId":3,"approved":true,"comment":"危急值确认"}
                    """))
            .andExpect(status().isOk());
    }

    private JsonNode data(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }

    private RequestPostProcessor adminWith(String... authorities) {
        return user("report-admin").authorities(Stream.of(authorities)
            .map(SimpleGrantedAuthority::new)
            .toList());
    }
}
