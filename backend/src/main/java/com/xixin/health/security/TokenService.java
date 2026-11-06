package com.xixin.health.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xixin.health.iam.Role;
import com.xixin.health.iam.UserAccount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TokenService {

    private final byte[] secret;
    private final long accessTokenSeconds;
    private final long refreshTokenSeconds;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    public TokenService(
        @Value("${xixin.security.token-secret}") String secret,
        @Value("${xixin.security.access-token-seconds}") long accessTokenSeconds,
        @Value("${xixin.security.refresh-token-seconds}") long refreshTokenSeconds,
        ObjectMapper objectMapper
    ) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.accessTokenSeconds = accessTokenSeconds;
        this.refreshTokenSeconds = refreshTokenSeconds;
        this.objectMapper = objectMapper;
    }

    public TokenPair issueTokens(UserAccount account) {
        return new TokenPair(
            createToken(account, "access", accessTokenSeconds),
            createToken(account, "refresh", refreshTokenSeconds),
            accessTokenSeconds
        );
    }

    public Optional<AuthenticatedToken> authenticateAccessToken(String token) {
        return parseAndValidate(token, "access");
    }

    public Optional<AuthenticatedToken> authenticateRefreshToken(String token) {
        return parseAndValidate(token, "refresh");
    }

    private String createToken(UserAccount account, String tokenUse, long ttlSeconds) {
        Instant now = Instant.now();
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String roles = account.getRoles().stream()
            .map(Role::getCode)
            .map(role -> "\"" + role + "\"")
            .collect(Collectors.joining(","));
        String displayName = account.getDisplayName();
        Long branchId = account.getBranchId();
        String branchIdJson = branchId != null ? ",\"branchId\":" + branchId : "";
        String payload = "{"
            + "\"sub\":\"" + account.getId() + "\","
            + "\"phone\":\"" + account.getPhone() + "\","
            + "\"displayName\":\"" + (displayName != null ? displayName.replace("\"", "\\\"") : "") + "\","
            + "\"use\":\"" + tokenUse + "\","
            + "\"roles\":[" + roles + "]"
            + branchIdJson + ","
            + "\"iat\":" + now.getEpochSecond() + ","
            + "\"exp\":" + now.plusSeconds(ttlSeconds).getEpochSecond() + ","
            + "\"jti\":\"" + randomId() + "\""
            + "}";
        String encoded = base64Url(header.getBytes(StandardCharsets.UTF_8))
            + "."
            + base64Url(payload.getBytes(StandardCharsets.UTF_8));
        return encoded + "." + sign(encoded);
    }

    private String randomId() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return base64Url(bytes);
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return base64Url(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign token", exception);
        }
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private Optional<AuthenticatedToken> parseAndValidate(String token, String expectedUse) {
        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3 || parts[0].isBlank() || parts[1].isBlank() || parts[2].isBlank()) {
                return Optional.empty();
            }

            String signedContent = parts[0] + "." + parts[1];
            if (!MessageDigest.isEqual(
                sign(signedContent).getBytes(StandardCharsets.US_ASCII),
                parts[2].getBytes(StandardCharsets.US_ASCII)
            )) {
                return Optional.empty();
            }

            JsonNode payload = objectMapper.readTree(Base64.getUrlDecoder().decode(parts[1]));
            if (!expectedUse.equals(payload.path("use").asText())) {
                return Optional.empty();
            }
            if (payload.path("exp").asLong(0) <= Instant.now().getEpochSecond()) {
                return Optional.empty();
            }

            String subject = payload.path("sub").asText();
            if (subject.isBlank()) {
                return Optional.empty();
            }
            List<String> roles = new ArrayList<>();
            payload.path("roles").forEach(role -> {
                if (role.isTextual() && !role.asText().isBlank()) {
                    roles.add(role.asText());
                }
            });
            Long branchId = payload.has("branchId") && !payload.path("branchId").isNull() ? payload.path("branchId").asLong() : null;
            return Optional.of(new AuthenticatedToken(Long.parseLong(subject), payload.path("phone").asText(), payload.path("displayName").asText(), roles, branchId));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    public record TokenPair(String accessToken, String refreshToken, long expiresInSeconds) {
    }

    public record AuthenticatedToken(Long userId, String phone, String displayName, List<String> roles, Long branchId) {
    }
}
