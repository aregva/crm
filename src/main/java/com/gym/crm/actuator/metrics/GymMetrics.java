package com.gym.crm.actuator.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class GymMetrics {

    private final Counter traineeCreatedCounter;
    private final Counter trainerCreatedCounter;
    private final Counter trainingCreatedCounter;
    private final Counter loginSuccessCounter;
    private final Counter loginFailedCounter;

    public GymMetrics(MeterRegistry registry) {

        traineeCreatedCounter =
                Counter.builder("gym.trainee.created")
                        .description("Created trainees")
                        .register(registry);
        trainerCreatedCounter =
                Counter.builder("gym.trainer.created")
                        .description("Number of trainers created")
                        .register(registry);

        trainingCreatedCounter =
                Counter.builder("gym.training.created")
                        .description("Number of trainings created")
                        .register(registry);

        loginSuccessCounter =
                Counter.builder("gym.login.success")
                        .description("Successful logins")
                        .register(registry);

        loginFailedCounter =
                Counter.builder("gym.login.failed")
                        .description("Failed logins")
                        .register(registry);
    }

    public void incrementTraineeCreated() {
        traineeCreatedCounter.increment();
    }

    public void incrementTrainerCreated() {
        trainerCreatedCounter.increment();
    }

    public void incrementTrainingCreated() {
        trainingCreatedCounter.increment();
    }

    public void incrementLoginSuccess() {
        loginSuccessCounter.increment();
    }

    public void incrementLoginFailed() {
        loginFailedCounter.increment();
    }
}