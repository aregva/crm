package com.gym.crm.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record AddTrainingRequest(
        String trainingName,
        LocalDate trainingDate,
        Integer trainingDuration
) {}
