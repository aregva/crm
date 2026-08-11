package com.gym.crm.integration.workload;

import java.time.LocalDate;

/**
 * Mirrors the request contract of {@code trainer-workload-service}'s
 * {@code POST /api/trainer-workloads} endpoint. Duplicated by design: each
 * microservice owns its own contract copy rather than sharing a library.
 */
public record TrainerWorkloadRequest(
        String trainerUsername,
        String trainerFirstName,
        String trainerLastName,
        boolean isActive,
        LocalDate trainingDate,
        Integer trainingDuration,
        ActionType actionType
) {}
