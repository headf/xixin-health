package com.xixin.health.auth.dto;

public record AuthTokenResponse(
    String tokenType,
    String accessToken,
    String refreshToken,
    long expiresInSeconds,
    Long userId,
    String phone,
    String displayName
) {
}
