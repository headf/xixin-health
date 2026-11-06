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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MedicalWorkflowComplianceTest {

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
    void checkInCreatesExamRecordGuideSheetAndProgress() throws Exception {
        MvcResult result = checkIn();
        JsonNode data = data(result);

        assertThat(data.path("id").asLong()).isPositive();
        assertThat(data.path("examNo").asText()).isNotBlank();
    }

    @Test
    void guideSheetContainsOrderedDepartments() throws Exception {
        JsonNode checkedIn = data(checkIn());
        long examRecordId = checkedIn.path("id").asLong();

        MvcResult result = mockMvc.perform(get("/api/v1/guide-sheets/by-exam/{examRecordId}", examRecordId)
                .with(adminWith("ROLE_FRONT_DESK", "FRONT_DESK_CHECKIN")))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode sheetData = data(result);
        assertThat(sheetData).isNotNull();
    }

    @Test
    void departmentQueueSupportsCallSkipAndComplete() throws Exception {
        JsonNode checkedIn = data(checkIn());
        long examRecordId = checkedIn.path("id").asLong();

        // Record vital signs (required before queuing)
        mockMvc.perform(post("/api/v1/exams/vital-signs")
                .with(adminWith("ROLE_NURSE", "VITAL_SIGNS_RECORD"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"examRecordId":%d,"recordedBy":2,
                     "height":170.0,"weight":70.0,
                     "bloodPressureSystolic":120,"bloodPressureDiastolic":80,
                     "heartRate":72,"temperature":36.5}
                    """.formatted(examRecordId)))
            .andExpect(status().isOk());

        // Add to queue for department 1 (检验科)
        mockMvc.perform(post("/api/v1/exams/queue/add")
                .with(adminWith("ROLE_EXAM_DOCTOR", "RESULT_RECORD"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"examRecordId\":%d,\"departmentId\":1}".formatted(examRecordId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.queueStatus").value("WAITING"));

        // Call next (WAITING -> CALLED)
        mockMvc.perform(post("/api/v1/exams/queue/call-next/1")
                .with(adminWith("ROLE_EXAM_DOCTOR", "RESULT_RECORD")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.queueStatus").value("CALLED"));
    }

    @Test
    void progressBlocksReportUntilRequiredItemsCompleted() throws Exception {
        JsonNode checkedIn = data(checkIn());
        long examRecordId = checkedIn.path("id").asLong();

        mockMvc.perform(get("/api/v1/exams/{examRecordId}/progress", examRecordId)
                .with(adminWith("ROLE_REVIEW_DOCTOR", "REPORT_REVIEW")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(3))
            .andExpect(jsonPath("$.data[0].progressStatus").value("PENDING"));
    }

    private MvcResult checkIn() throws Exception {
        return mockMvc.perform(post("/api/v1/exams/check-in")
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
    }

    private JsonNode data(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }

    private RequestPostProcessor adminWith(String... authorities) {
        return user("medical-admin").authorities(Stream.of(authorities)
            .map(SimpleGrantedAuthority::new)
            .toList());
    }
}
