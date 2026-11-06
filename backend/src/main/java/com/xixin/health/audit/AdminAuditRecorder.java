package com.xixin.health.audit;

import com.xixin.health.security.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AdminAuditRecorder {

    private final AuditService auditService;

    public AdminAuditRecorder(AuditService auditService) {
        this.auditService = auditService;
    }

    public void record(String operationType, String targetType, Object targetId, HttpServletRequest request) {
        auditService.record(
            operatorId(),
            operationType,
            targetType,
            targetId == null ? null : String.valueOf(targetId),
            "SUCCESS",
            clientIp(request),
            request == null ? null : request.getHeader("User-Agent"),
            request == null ? null : request.getHeader("X-Trace-Id")
        );
    }

    private String operatorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof TokenService.AuthenticatedToken token) {
            return String.valueOf(token.userId());
        }
        return authentication.getName();
    }

    private String clientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",", 2)[0].trim();
        }
        return request.getRemoteAddr();
    }
}
