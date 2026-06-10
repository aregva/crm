package com.gym.crm.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TrainerProfileResponse(
        String username,
        String firstName,
        String lastName,
        String specialization,
        @JsonProperty("isActive") boolean isActive,
        List<TraineeSummaryResponse> traineesList
) {
}
