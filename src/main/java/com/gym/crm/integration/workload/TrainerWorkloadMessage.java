package com.gym.crm.integration.workload;

import java.time.LocalDate;

/**
 * Mirrors the message contract {@code trainer-workload-service}'s JMS listener
 * consumes from the {@code trainer.workload.queue} queue. Duplicated by design:
 * each microservice owns its own contract copy rather than sharing a library.
 */
public record TrainerWorkloadMessage(
        String trainerUsername,
        String trainerFirstName,
        String trainerLastName,
        boolean isActive,
        LocalDate trainingDate,
        Integer trainingDuration,
        ActionType actionType
) {}
