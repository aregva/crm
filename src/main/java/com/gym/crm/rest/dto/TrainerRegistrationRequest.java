package com.gym.crm.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record TrainerRegistrationRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String specialization
) {
}
