package com.gym.crm.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeLoginRequest(
        @NotBlank String username,
        @NotBlank String oldPassword,
        @NotBlank String newPassword
) {
}
