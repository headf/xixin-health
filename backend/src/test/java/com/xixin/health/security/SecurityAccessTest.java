package com.xixin.health.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xixin.health.iam.Role;
import com.xixin.health.iam.RoleRepository;
import com.xixin.health.iam.UserAccount;
import com.xixin.health.iam.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAccessTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserAccountRepository userAccountRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void adminEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/admin/audit-logs"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void systemAdminCanReadRoles() throws Exception {
        mockMvc.perform(get("/api/v1/admin/roles").with(adminWithIamManage()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].code").exists());
    }

    @Test
    void bearerTokenCanAuthorizeSystemAdmin() throws Exception {
        Role systemAdmin = roleRepository.findByCode("SYSTEM_ADMIN").orElseThrow();
        UserAccount account = new UserAccount(
            "13900000001",
            "admin-token@example.com",
            "Token Admin",
            passwordEncoder.encode("Passw0rd!")
        );
        account.addRole(systemAdmin);
        userAccountRepository.save(account);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"loginId":"13900000001","password":"Passw0rd!"}
                    """))
            .andExpect(status().isOk())
            .andReturn();
        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
            .path("data")
            .path("accessToken")
            .asText();

        mockMvc.perform(get("/api/v1/admin/roles")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].code").exists());
    }

    private RequestPostProcessor adminWithIamManage() {
        return user("admin").authorities(Stream.of("ROLE_SYSTEM_ADMIN", "IAM_MANAGE")
            .map(SimpleGrantedAuthority::new)
            .toList());
    }
}
