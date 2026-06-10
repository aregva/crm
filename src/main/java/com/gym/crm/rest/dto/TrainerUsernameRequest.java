package com.gym.crm.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record TrainerUsernameRequest(
        @NotBlank String trainerUsername
) {
}
