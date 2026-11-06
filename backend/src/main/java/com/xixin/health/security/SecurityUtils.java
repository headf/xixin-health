package com.xixin.health.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Utility to get current user's security context info, especially branchId.
 * SYSTEM_ADMIN sees all, OPERATIONS_ADMIN with branchId sees only their branch.
 */
@Component
public class SecurityUtils {

    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public boolean isAuthenticated() {
        Authentication auth = getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
    }

    public TokenService.AuthenticatedToken getCurrentUser() {
        if (!isAuthenticated()) return null;
        Object principal = getAuthentication().getPrincipal();
        if (principal instanceof TokenService.AuthenticatedToken token) {
            return token;
        }
        return null;
    }

    public Long getCurrentUserId() {
        TokenService.AuthenticatedToken user = getCurrentUser();
        return user != null ? user.userId() : null;
    }

    /**
     * Returns the branchId the current user is restricted to, or null if they see all.
     * SYSTEM_ADMIN = null (see all)
     * OPERATIONS_ADMIN with branchId = that branch only
     */
    public Long getCurrentBranchId() {
        TokenService.AuthenticatedToken user = getCurrentUser();
        if (user == null) return null;
        List<String> roles = user.roles();
        // 系统管理员没有分院限制
        if (roles.contains("SYSTEM_ADMIN")) return null;
        // 分院管理员返回分院ID
        if (roles.contains("OPERATIONS_ADMIN")) {
            return user.branchId();
        }
        return null;
    }

    /**
     * Whether the current user has the given role.
     */
    public boolean hasRole(String roleCode) {
        TokenService.AuthenticatedToken user = getCurrentUser();
        return user != null && user.roles().contains(roleCode);
    }
}
