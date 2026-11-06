package com.xixin.health.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "phone must be a mainland China mobile number")
    String phone,

    @Email
    String email,

    @NotBlank
    @Size(max = 64)
    String displayName,

    @NotBlank
    @Size(min = 8, max = 64)
    String password,

    String idCard
) {
}
