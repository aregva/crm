package com.gym.crm.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateTraineeTrainersRequest(
        @NotBlank String traineeUsername,
        @NotEmpty @Valid List<TrainerUsernameRequest> trainersList
) {
}
