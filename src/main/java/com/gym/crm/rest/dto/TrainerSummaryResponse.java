package com.gym.crm.rest.dto;

public record TrainerSummaryResponse(
        String trainerUsername,
        String trainerFirstName,
        String trainerLastName,
        String trainerSpecialization
) {
}
