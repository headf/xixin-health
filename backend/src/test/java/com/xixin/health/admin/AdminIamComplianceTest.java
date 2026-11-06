package com.xixin.health.admin;

import com.xixin.health.iam.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminIamComplianceTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    RoleRepository roleRepository;

    @Test
    void accountResponseDoesNotExposePasswordHash() throws Exception {
        mockMvc.perform(get("/api/v1/admin/accounts").with(adminWith("ROLE_SYSTEM_ADMIN", "IAM_MANAGE")))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("passwordHash"))));
    }

    @Test
    void operationsAdminCannotManageIamWithoutIamPermission() throws Exception {
        mockMvc.perform(get("/api/v1/admin/accounts").with(adminWith("ROLE_OPERATIONS_ADMIN", "CONFIG_MANAGE")))
            .andExpect(status().isForbidden());
    }

    @Test
    void systemAdminWithIamPermissionCanCreateRole() throws Exception {
        mockMvc.perform(post("/api/v1/admin/roles")
                .with(adminWith("ROLE_SYSTEM_ADMIN", "IAM_MANAGE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code":"NURSE_GUIDE_COMPLIANCE",
                      "name":"导诊护士",
                      "roleType":"MEDICAL",
                      "permissionCodes":["FRONT_DESK_CHECKIN"]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.code").value("NURSE_GUIDE_COMPLIANCE"));
    }

    @Test
    void builtInRoleCannotBeModifiedOrDeleted() throws Exception {
        long systemRoleId = roleRepository.findByCode("SYSTEM_ADMIN").orElseThrow().getId();

        mockMvc.perform(put("/api/v1/admin/roles/{id}", systemRoleId)
                .with(adminWith("ROLE_SYSTEM_ADMIN", "IAM_MANAGE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code":"SYSTEM_ADMIN",
                      "name":"Changed",
                      "roleType":"ADMIN",
                      "permissionCodes":["AUTH_LOGIN"]
                    }
                    """))
            .andExpect(status().isForbidden());
    }

    private RequestPostProcessor adminWith(String... authorities) {
        return user("admin").authorities(Stream.of(authorities)
            .map(SimpleGrantedAuthority::new)
            .toList());
    }
}
