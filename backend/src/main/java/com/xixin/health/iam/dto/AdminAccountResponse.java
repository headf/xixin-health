package com.xixin.health.iam.dto;

import com.xixin.health.iam.UserAccount;

import java.util.List;

public record AdminAccountResponse(
    Long id,
    String phone,
    String email,
    String displayName,
    boolean enabled,
    List<String> roleCodes,
    Long branchId
) {
    public static AdminAccountResponse from(UserAccount account) {
        return new AdminAccountResponse(
            account.getId(),
            account.getPhone(),
            account.getEmail(),
            account.getDisplayName(),
            account.isEnabled(),
            account.getRoles().stream()
                .filter(role -> !role.isDeleted())
                .map(role -> role.getCode())
                .sorted()
                .toList(),
            account.getBranchId()
        );
    }
}
