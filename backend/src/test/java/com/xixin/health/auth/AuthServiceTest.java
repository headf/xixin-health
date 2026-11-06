package com.xixin.health.auth;

import com.xixin.health.auth.dto.AuthTokenResponse;
import com.xixin.health.auth.dto.LoginRequest;
import com.xixin.health.auth.dto.RegisterRequest;
import com.xixin.health.iam.UserAccount;
import com.xixin.health.iam.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class AuthServiceTest {

    @Autowired
    AuthService authService;

    @Autowired
    UserAccountRepository userAccountRepository;

    @Test
    void registersDefaultNormalUserAndLogsIn() {
        authService.register(new RegisterRequest("13800000001", "alice@example.com", "Alice", "Passw0rd!", ""));
        UserAccount account = userAccountRepository.findByPhone("13800000001").orElseThrow();

        assertThat(account.getPasswordHash()).startsWith("$2");
        assertThat(account.getRoles()).extracting("code").contains("NORMAL_USER");

        AuthTokenResponse token = authService.login(new LoginRequest("13800000001", "Passw0rd!"), "127.0.0.1", "JUnit");

        assertThat(token.accessToken()).isNotBlank();
        assertThat(token.refreshToken()).isNotBlank();
        assertThat(token.tokenType()).isEqualTo("Bearer");
        assertThat(token.expiresInSeconds()).isEqualTo(7200);
    }
}
