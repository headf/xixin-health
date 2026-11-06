package com.xixin.health.spec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RequiredInterfaceCompletionTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void userSecurityProfileAppointmentAndOrderInterfacesCoverRequiredLifecycle() throws Exception {
        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"loginId":"13900000000","password":"ChangeMe123!"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
            .andReturn()
            .getResponse()
            .getContentAsString();
        String refreshToken = objectMapper.readTree(loginResponse).path("data").path("refreshToken").asText();

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"refreshToken":"%s"}
                    """.formatted(refreshToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

        mockMvc.perform(post("/api/v1/profiles/me")
                .with(user("normal").roles("NORMAL_USER"))
                .param("ownerUserId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name":"Zhang San",
                      "idCardNo":"130000199001011234",
                      "phone":"13900000000",
                      "allergyHistory":"None",
                      "medicalHistory":"None",
                      "emergencyContactName":"Li Si",
                      "emergencyContactPhone":"13900000001"
                    }
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/profiles/me")
                .with(user("normal").roles("NORMAL_USER"))
                .param("ownerUserId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("Zhang San"));

        long familyId = idFrom(mockMvc.perform(post("/api/v1/profiles/family")
                .with(user("normal").roles("NORMAL_USER"))
                .param("ownerUserId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name":"Zhang Jr",
                      "relationship":"CHILD",
                      "idCardNo":"130000201501011234",
                      "phone":"13900000002"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.relationship").value("CHILD"))
            .andReturn()
            .getResponse()
            .getContentAsString());

        mockMvc.perform(put("/api/v1/profiles/family/{familyId}", familyId)
                .with(user("normal").roles("NORMAL_USER"))
                .param("ownerUserId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name":"Zhang Jr Updated",
                      "relationship":"CHILD",
                      "idCardNo":"130000201501011234",
                      "phone":"13900000003"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("Zhang Jr Updated"));

        mockMvc.perform(get("/api/v1/profiles/family")
                .with(user("normal").roles("NORMAL_USER"))
                .param("ownerUserId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/api/v1/branches/1/time-slots")
                .with(user("normal").roles("NORMAL_USER"))
                .param("date", LocalDate.now().plusDays(3).toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].timeSlot").exists());

        long appointmentId = idFrom(mockMvc.perform(post("/api/v1/appointments")
                .with(user("normal").roles("NORMAL_USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "userId":1,
                      "profileId":1,
                      "packageId":1,
                      "branchId":1,
                      "appointmentDate":"%s",
                      "timeSlot":"AM"
                    }
                    """.formatted(LocalDate.now().plusDays(3))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString());

        mockMvc.perform(get("/api/v1/appointments")
                .with(user("normal").roles("NORMAL_USER"))
                .param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));

        String orderResponse = mockMvc.perform(post("/api/v1/orders")
                .with(user("normal").roles("NORMAL_USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"appointmentId":%d,"amountCent":29900}
                    """.formatted(appointmentId)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        String orderNo = objectMapper.readTree(orderResponse).path("data").path("orderNo").asText();

        String paymentResponse = mockMvc.perform(post("/api/v1/orders/{orderNo}/pay", orderNo)
                .with(user("normal").roles("NORMAL_USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"channel":"WECHAT"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.paymentNo").isNotEmpty())
            .andReturn()
            .getResponse()
            .getContentAsString();
        String paymentNo = objectMapper.readTree(paymentResponse).path("data").path("paymentNo").asText();

        mockMvc.perform(post("/api/v1/orders/payment-callback")
                .with(user("normal").roles("NORMAL_USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"orderNo":"%s","paymentNo":"%s","success":true}
                    """.formatted(orderNo, paymentNo)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/orders/{orderNo}/refund", orderNo)
                .with(user("normal").roles("NORMAL_USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"amountCent":29900,"reason":"Change plan"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("REQUESTED"));

        mockMvc.perform(get("/api/v1/orders")
                .with(user("normal").roles("NORMAL_USER"))
                .param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].orderNo").exists());
    }

    @Test
    void examResultReportAndAnalysisInterfacesCoverRequiredMedicalWorkflow() throws Exception {
        long appointmentId = idFrom(mockMvc.perform(post("/api/v1/appointments")
                .with(user("normal").roles("NORMAL_USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "userId":1,
                      "profileId":1,
                      "packageId":1,
                      "branchId":1,
                      "appointmentDate":"%s",
                      "timeSlot":"AM"
                    }
                    """.formatted(LocalDate.now().plusDays(5))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString());

        long examRecordId = idFrom(mockMvc.perform(post("/api/v1/exams/check-in")
                .with(user("front").roles("FRONT_DESK"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"appointmentId":%d}
                    """.formatted(appointmentId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.examNo").isNotEmpty())
            .andReturn()
            .getResponse()
            .getContentAsString());

        mockMvc.perform(get("/api/v1/exams/{examRecordId}/guide", examRecordId)
                .with(user("front").roles("FRONT_DESK")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.routeText", containsString("Laboratory")));

        mockMvc.perform(get("/api/v1/exams/{examRecordId}/progress", examRecordId)
                .with(user("front").roles("FRONT_DESK")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/api/v1/exams/departments/1/queue")
                .with(user("doctor").roles("EXAM_DOCTOR")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));

        long resultId = idFrom(mockMvc.perform(post("/api/v1/results/numeric")
                .with(user("doctor").roles("EXAM_DOCTOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "examRecordId":%d,
                      "itemId":1,
                      "itemName":"Blood Glucose",
                      "unit":"mmol/L",
                      "numericValue":8.2,
                      "referenceMin":3.9,
                      "referenceMax":6.1,
                      "criticalMax":11.1,
                      "recorderId":2
                    }
                    """.formatted(examRecordId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.abnormalLevel").value("ABNORMAL"))
            .andReturn()
            .getResponse()
            .getContentAsString());

        mockMvc.perform(post("/api/v1/results/{resultId}/review", resultId)
                .with(user("reviewer").roles("REVIEW_DOCTOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"reviewerId":3,"approved":true,"comment":"Confirmed"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.reviewStatus").value("APPROVED"));

        long reportId = idFrom(mockMvc.perform(post("/api/v1/reports/generate")
                .with(user("reviewer").roles("REVIEW_DOCTOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"examRecordId":%d,"reviewerId":3}
                    """.formatted(examRecordId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.userId").value(1))
            .andReturn()
            .getResponse()
            .getContentAsString());

        mockMvc.perform(get("/api/v1/reports")
                .with(user("normal").roles("NORMAL_USER"))
                .param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/api/v1/reports/{reportId}", reportId)
                .with(user("normal").roles("NORMAL_USER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(reportId));

        mockMvc.perform(get("/api/v1/reports/{reportId}/download", reportId)
                .with(user("normal").roles("NORMAL_USER")))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("report-")))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF));

        mockMvc.perform(post("/api/v1/health-analysis/reports/{reportId}/generate", reportId)
                .with(user("reviewer").roles("REVIEW_DOCTOR")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.riskLevel").value("MEDIUM"));

        mockMvc.perform(get("/api/v1/health-analysis")
                .with(user("normal").roles("NORMAL_USER"))
                .param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/api/v1/health-analysis/trends")
                .with(user("normal").roles("NORMAL_USER"))
                .param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.trendData").exists());
    }

    private long idFrom(String response) throws Exception {
        JsonNode data = objectMapper.readTree(response).path("data");
        return data.path("id").asLong();
    }
}
