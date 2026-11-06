package com.xixin.health.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ResultReviewComplianceTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void recordNumericResultDetectsAbnormalLevel() throws Exception {
        mockMvc.perform(post("/api/v1/results/numeric")
                .with(adminWith("ROLE_EXAM_DOCTOR", "RESULT_RECORD"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(numericResult(8.2, 3.9, 6.1, 11.1)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.abnormalLevel").value("ABNORMAL"))
            .andExpect(jsonPath("$.data.reviewStatus").value("PENDING"));
    }

    @Test
    void reviewEndpointApprovesAndRejectsResult() throws Exception {
        long approveId = resultId(record(5.0));
        mockMvc.perform(put("/api/v1/results/{id}/review", approveId)
                .with(adminWith("ROLE_REVIEW_DOCTOR", "REPORT_REVIEW"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"reviewerId":3,"approved":true,"comment":"正常通过"}
                    """))
            .andExpect(status().isOk());

        long rejectId = resultId(record(4.8));
        mockMvc.perform(put("/api/v1/results/{id}/review", rejectId)
                .with(adminWith("ROLE_REVIEW_DOCTOR", "REPORT_REVIEW"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"reviewerId":3,"approved":false,"comment":"需要重录"}
                    """))
            .andExpect(status().isOk());
    }

    private MvcResult record(double value) throws Exception {
        return mockMvc.perform(post("/api/v1/results/numeric")
                .with(adminWith("ROLE_EXAM_DOCTOR", "RESULT_RECORD"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(numericResult(value, 3.9, 6.1, 11.1)))
            .andExpect(status().isOk())
            .andReturn();
    }

    private long resultId(MvcResult result) throws Exception {
        return data(result).path("id").asLong();
    }

    private JsonNode data(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }

    private String numericResult(double value, double referenceMin, double referenceMax, double criticalMax) {
        return """
            {
              "examRecordId":1,
              "itemId":1,
              "itemName":"Blood Glucose",
              "unit":"mmol/L",
              "numericValue":%s,
              "referenceMin":%s,
              "referenceMax":%s,
              "criticalMax":%s,
              "criticalMin":-1,
              "recorderId":2
            }
            """.formatted(value, referenceMin, referenceMax, criticalMax);
    }

    private RequestPostProcessor adminWith(String... authorities) {
        return user("result-admin").authorities(Stream.of(authorities)
            .map(SimpleGrantedAuthority::new)
            .toList());
    }
}
