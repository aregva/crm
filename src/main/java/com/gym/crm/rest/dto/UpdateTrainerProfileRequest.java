package com.gym.crm.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateTrainerProfileRequest(
        @NotBlank String username,
        @NotBlank String firstName,
        @NotBlank String lastName,
        String specialization,
        @JsonProperty("isActive") @NotNull Boolean isActive
) {
}
