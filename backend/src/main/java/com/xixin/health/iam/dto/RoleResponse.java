package com.xixin.health.iam.dto;

import com.xixin.health.iam.Role;

import java.util.List;

public record RoleResponse(
    Long id,
    String code,
    String name,
    String roleType,
    boolean systemBuiltIn,
    List<String> permissionCodes
) {
    public static RoleResponse from(Role role) {
        return new RoleResponse(
            role.getId(),
            role.getCode(),
            role.getName(),
            role.getRoleType(),
            role.isSystemBuiltIn(),
            role.getPermissions().stream()
                .map(permission -> permission.getCode())
                .sorted()
                .toList()
        );
    }
}
