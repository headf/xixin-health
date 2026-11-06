package com.xixin.health.common.error;

public enum ErrorCode {
    AUTH_INVALID_CREDENTIALS("AUTH_INVALID_CREDENTIALS", "Invalid credentials"),
    AUTH_UNAUTHORIZED("AUTH_UNAUTHORIZED", "Authentication is required"),
    AUTH_FORBIDDEN("AUTH_FORBIDDEN", "Permission denied"),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Resource not found"),
    BUSINESS_CONFLICT("BUSINESS_CONFLICT", "Business state conflict"),
    VALIDATION_ERROR("VALIDATION_ERROR", "Request validation failed"),
    INVALID_PARAMETER("INVALID_PARAMETER", "Invalid parameter"),
    SYSTEM_ERROR("SYSTEM_ERROR", "System error");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String code() {
        return code;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
