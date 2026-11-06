package com.xixin.health.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xixin.health.iam.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminBackendApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RoleRepository roleRepository;

    @Test
    void defaultSystemAdminCanLogin() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"loginId":"13900000000","password":"ChangeMe123!"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    void managesConfigurationCatalogs() throws Exception {
        long examItemId = postForId("/api/v1/admin/exam-items", """
            {
              "itemCode":"ITEM_ADMIN_GLU",
              "itemName":"管理员测试血糖",
              "departmentId":1,
              "unit":"mmol/L",
              "referenceMin":3.9,
              "referenceMax":6.1,
              "criticalMax":11.1,
              "enabled":true
            }
            """);

        mockMvc.perform(put("/api/v1/admin/exam-items/{id}", examItemId)
                .with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "itemCode":"ITEM_ADMIN_GLU",
                      "itemName":"管理员测试血糖-更新",
                      "departmentId":1,
                      "unit":"mmol/L",
                      "referenceMin":3.8,
                      "referenceMax":6.2,
                      "criticalMax":11.5,
                      "enabled":true
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.itemName").value("管理员测试血糖-更新"));

        mockMvc.perform(get("/api/v1/admin/exam-items")
                .with(admin()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));

        long packageId = postForId("/api/v1/admin/packages", """
            {
              "packageCode":"PKG_ADMIN",
              "packageName":"管理员测试套餐",
              "category":"BASIC",
              "genderLimit":"ALL",
              "ageRange":"18-80",
              "priceCent":19900,
              "enabled":true,
              "itemIds":[%d]
            }
            """.formatted(examItemId));

        mockMvc.perform(put("/api/v1/admin/packages/{id}", packageId)
                .with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "packageCode":"PKG_ADMIN",
                      "packageName":"管理员测试套餐-更新",
                      "category":"BASIC",
                      "genderLimit":"ALL",
                      "ageRange":"18-80",
                      "priceCent":20900,
                      "enabled":true,
                      "itemIds":[%d]
                    }
                    """.formatted(examItemId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.packageName").value("管理员测试套餐-更新"));

        long branchId = postForId("/api/v1/admin/branches", """
            {
              "branchCode":"BR_ADMIN",
              "branchName":"管理员测试分院",
              "address":"测试路 1 号",
              "servicePhone":"400-800-0000",
              "enabled":true
            }
            """);

        mockMvc.perform(patch("/api/v1/admin/branches/{id}/status", branchId)
                .with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"enabled":false}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.enabled").value(false));

        long ruleId = postForId("/api/v1/admin/rules", """
            {
              "ruleCode":"RULE_ADMIN_CANCEL",
              "ruleName":"管理员测试取消规则",
              "ruleType":"APPOINTMENT",
              "ruleContent":"{\\"minHoursBeforeExam\\":24}",
              "enabled":true
            }
            """);

        mockMvc.perform(put("/api/v1/admin/rules/{id}", ruleId)
                .with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "ruleCode":"RULE_ADMIN_CANCEL",
                      "ruleName":"管理员测试取消规则-更新",
                      "ruleType":"APPOINTMENT",
                      "ruleContent":"{\\"minHoursBeforeExam\\":12}",
                      "enabled":true
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.ruleName").value("管理员测试取消规则-更新"));
    }

    @Test
    void managesRolesAndAccounts() throws Exception {
        long roleId = postForId("/api/v1/admin/roles", """
            {
              "code":"ADMIN_TEST_ROLE",
              "name":"管理员测试角色",
              "roleType":"ADMIN",
              "permissionCodes":["AUTH_LOGIN","CONFIG_MANAGE"]
            }
            """);

        mockMvc.perform(put("/api/v1/admin/roles/{id}", roleId)
                .with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code":"ADMIN_TEST_ROLE",
                      "name":"管理员测试角色-更新",
                      "roleType":"ADMIN",
                      "permissionCodes":["AUTH_LOGIN","CONFIG_MANAGE","AUDIT_QUERY"]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("管理员测试角色-更新"));

        mockMvc.perform(get("/api/v1/system/roles")
                .with(admin()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));

        long accountId = postForId("/api/v1/admin/accounts", """
            {
              "phone":"13900000222",
              "email":"admin-worker@example.com",
              "displayName":"管理员测试员工",
              "password":"Worker@123456",
              "roleCodes":["ADMIN_TEST_ROLE"]
            }
            """);

        mockMvc.perform(patch("/api/v1/admin/accounts/{id}/status", accountId)
                .with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"enabled":false}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.enabled").value(false));

        mockMvc.perform(patch("/api/v1/admin/accounts/{id}/password", accountId)
                .with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"password":"Worker@654321"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(accountId));

        mockMvc.perform(get("/api/v1/system/accounts")
                .with(admin()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));

        mockMvc.perform(delete("/api/v1/admin/roles/{id}", roleId)
                .with(admin()))
            .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/v1/admin/accounts/{id}", accountId)
                .with(admin()))
            .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/admin/roles/{id}", roleId)
                .with(admin()))
            .andExpect(status().isOk());
    }

    @Test
    void filtersAuditLogsAndProvidesStatistics() throws Exception {
        mockMvc.perform(get("/api/v1/admin/audit-logs")
                .param("page", "0")
                .param("size", "10")
                .with(admin()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isArray());

        mockMvc.perform(get("/api/v1/statistics/business")
                .param("startDate", "2026-01-01")
                .param("endDate", "2026-12-31")
                .param("dimension", "MONTH")
                .with(admin()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalAppointments").exists())
            .andExpect(jsonPath("$.data.totalRevenueCent").exists());

        mockMvc.perform(get("/api/v1/admin/statistics/health")
                .param("startDate", "2026-01-01")
                .param("endDate", "2026-12-31")
                .with(admin()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.abnormalDistribution").isArray());

        mockMvc.perform(get("/api/v1/admin/statistics/export")
                .param("type", "business")
                .param("format", "EXCEL")
                .with(admin()))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("statistics.xlsx")))
            .andExpect(content().contentTypeCompatibleWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        mockMvc.perform(get("/api/v1/admin/statistics/export")
                .param("type", "business")
                .param("format", "PDF")
                .with(admin()))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("statistics.pdf")))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF));

        mockMvc.perform(get("/api/v1/admin/statistics/business")
                .param("startDate", "2025-01-01")
                .param("endDate", "2026-02-01")
                .with(admin()))
            .andExpect(status().isBadRequest());
    }

    @Test
    void managesDepartmentsReportTemplatesAndFlowNodes() throws Exception {
        long departmentId = postForId("/api/v1/admin/departments", """
            {
              "branchId":1,
              "departmentCode":"DEP_ADMIN_LAB",
              "departmentName":"Admin Lab",
              "location":"2F-A",
              "capacity":18,
              "fastingRequired":true,
              "enabled":true
            }
            """);

        mockMvc.perform(put("/api/v1/admin/departments/{id}", departmentId)
                .with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "branchId":1,
                      "departmentCode":"DEP_ADMIN_LAB",
                      "departmentName":"Admin Lab Updated",
                      "location":"2F-B",
                      "capacity":24,
                      "fastingRequired":false,
                      "enabled":true
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.departmentName").value("Admin Lab Updated"));

        mockMvc.perform(patch("/api/v1/admin/departments/{id}/status", departmentId)
                .with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"enabled":false}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.enabled").value(false));

        long templateId = postForId("/api/v1/admin/report-templates", """
            {
              "templateCode":"TPL_ADMIN_BASIC",
              "templateName":"Admin Basic Template",
              "packageId":1,
              "templateContent":"summary:{summary}; advice:{advice}",
              "enabled":true
            }
            """);

        mockMvc.perform(put("/api/v1/admin/report-templates/{id}", templateId)
                .with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "templateCode":"TPL_ADMIN_BASIC",
                      "templateName":"Admin Basic Template V2",
                      "packageId":1,
                      "templateContent":"summary:{summary}; advice:{advice}; reviewer:{reviewer}",
                      "enabled":true
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.versionNo").value(2));

        mockMvc.perform(get("/api/v1/admin/report-templates/{id}/preview", templateId)
                .param("summary", "Normal")
                .param("advice", "Keep exercise")
                .param("reviewer", "Doctor")
                .with(admin()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.previewText", containsString("Normal")))
            .andExpect(jsonPath("$.data.previewText", containsString("Keep exercise")));

        long flowNodeId = postForId("/api/v1/admin/flow-nodes", """
            {
              "nodeCode":"FLOW_ADMIN_REVIEW",
              "nodeName":"Report Review",
              "nodeType":"APPROVAL",
              "sortNo":1,
              "requiredPermission":"REPORT_REVIEW",
              "approvalRoleCode":"REVIEW_DOCTOR",
              "enabled":true
            }
            """);

        mockMvc.perform(put("/api/v1/admin/flow-nodes/{id}", flowNodeId)
                .with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "nodeCode":"FLOW_ADMIN_REVIEW",
                      "nodeName":"Report Review Updated",
                      "nodeType":"APPROVAL",
                      "sortNo":2,
                      "requiredPermission":"REPORT_REVIEW",
                      "approvalRoleCode":"REVIEW_DOCTOR",
                      "enabled":true
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.sortNo").value(2));

        mockMvc.perform(get("/api/v1/admin/flow-nodes")
                .with(admin()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
    }

    @Test
    void deleteConfigurationDisablesInsteadOfPhysicallyRemoving() throws Exception {
        long itemId = postForId("/api/v1/admin/exam-items", """
            {
              "itemCode":"ITEM_DELETE_DISABLE",
              "itemName":"Delete Disable Item",
              "departmentId":1,
              "unit":"U/L",
              "referenceMin":1,
              "referenceMax":10,
              "criticalMax":100,
              "enabled":true
            }
            """);

        long packageId = postForId("/api/v1/admin/packages", """
            {
              "packageCode":"PKG_DELETE_DISABLE",
              "packageName":"Delete Disable Package",
              "category":"BASIC",
              "genderLimit":"ALL",
              "ageRange":"18-80",
              "priceCent":9900,
              "enabled":true,
              "itemIds":[%d]
            }
            """.formatted(itemId));

        mockMvc.perform(delete("/api/v1/admin/exam-items/{id}", itemId)
                .with(admin()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.enabled").value(false));

        mockMvc.perform(get("/api/v1/admin/exam-items")
                .with(admin()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("ITEM_DELETE_DISABLE")));

        mockMvc.perform(delete("/api/v1/admin/packages/{id}", packageId)
                .with(admin()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.enabled").value(false))
            .andExpect(jsonPath("$.data.itemIds", contains((int) itemId)));
    }

    @Test
    void protectsBuiltInRolesAndAuditsAdminOperations() throws Exception {
        long systemRoleId = roleRepository.findByCode("SYSTEM_ADMIN").orElseThrow().getId();

        mockMvc.perform(put("/api/v1/admin/roles/{id}", systemRoleId)
                .with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code":"SYSTEM_ADMIN",
                      "name":"Tampered System Admin",
                      "roleType":"ADMIN",
                      "permissionCodes":["AUTH_LOGIN"]
                    }
                    """))
            .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/admin/roles/{id}", systemRoleId)
                .with(admin()))
            .andExpect(status().isForbidden());

        long ruleId = postForId("/api/v1/admin/rules", """
            {
              "ruleCode":"RULE_AUDIT_ADMIN",
              "ruleName":"Audit Admin Rule",
              "ruleType":"APPOINTMENT",
              "ruleContent":"{\\"minHoursBeforeExam\\":24}",
              "enabled":true
            }
            """);

        mockMvc.perform(patch("/api/v1/admin/rules/{id}/status", ruleId)
                .with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"enabled":false}
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/audit-logs")
                .param("type", "CONFIG_CREATE")
                .param("size", "20")
                .with(admin()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content[?(@.operationType == 'CONFIG_CREATE')].targetType").isNotEmpty());

        mockMvc.perform(get("/api/v1/admin/audit-logs")
                .param("type", "CONFIG_STATUS")
                .param("size", "20")
                .with(admin()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content[?(@.operationType == 'CONFIG_STATUS')].targetType").isNotEmpty());
    }

    private long postForId(String path, String body) throws Exception {
        String response = mockMvc.perform(post(path)
                .with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();
        JsonNode data = objectMapper.readTree(response).path("data");
        return data.path("id").asLong();
    }

    private RequestPostProcessor admin() {
        return user("admin").authorities(Stream.of(
                "ROLE_SYSTEM_ADMIN",
                "AUDIT_QUERY",
                "CONFIG_MANAGE",
                "DEPARTMENT_MANAGE",
                "FLOW_MANAGE",
                "IAM_MANAGE",
                "STATISTICS_EXPORT",
                "STATISTICS_QUERY",
                "TEMPLATE_MANAGE"
            )
            .map(SimpleGrantedAuthority::new)
            .toList());
    }
}
