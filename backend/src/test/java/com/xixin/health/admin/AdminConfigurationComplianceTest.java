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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminConfigurationComplianceTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void medicalRuleChangeRequiresReviewBeforeEffective() throws Exception {
        long ruleId = postRule("RULE_CRITICAL_GLU_COMPLIANCE", "危急值规则", "MEDICAL_RESULT", "{\"criticalMax\":11.1}");

        mockMvc.perform(put("/api/v1/admin/rules/{id}", ruleId)
                .with(adminWith("ROLE_OPERATIONS_ADMIN", "CONFIG_MANAGE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "ruleCode":"RULE_CRITICAL_GLU_COMPLIANCE",
                      "ruleName":"危急值规则",
                      "ruleType":"MEDICAL_RESULT",
                      "ruleContent":"{\\"criticalMax\\":10.5}",
                      "enabled":true
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.reviewStatus").value("PENDING_REVIEW"))
            .andExpect(jsonPath("$.data.enabled").value(false));

        mockMvc.perform(post("/api/v1/admin/config-reviews/rules/{id}", ruleId)
                .with(adminWith("ROLE_SYSTEM_ADMIN", "REPORT_REVIEW"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"reviewerId":3,"approved":true,"comment":"专业审核通过"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.reviewStatus").value("APPROVED"))
            .andExpect(jsonPath("$.data.enabled").value(true));
    }

    private long postRule(String code, String name, String type, String content) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/admin/rules")
                .with(adminWith("ROLE_OPERATIONS_ADMIN", "CONFIG_MANAGE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "ruleCode":"%s",
                      "ruleName":"%s",
                      "ruleType":"%s",
                      "ruleContent":"%s",
                      "enabled":true
                    }
                    """.formatted(code, name, type, content.replace("\"", "\\\""))))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        return data.path("id").asLong();
    }

    private RequestPostProcessor adminWith(String... authorities) {
        return user("config-admin").authorities(Stream.of(authorities)
            .map(SimpleGrantedAuthority::new)
            .toList());
    }
}
