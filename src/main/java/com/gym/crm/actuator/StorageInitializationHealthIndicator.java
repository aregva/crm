package com.gym.crm.actuator;

import com.gym.crm.storage.StorageInitializer;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class StorageInitializationHealthIndicator
        implements HealthIndicator {

    private final StorageInitializer storageInitializer;

    public StorageInitializationHealthIndicator(
            StorageInitializer storageInitializer) {
        this.storageInitializer = storageInitializer;
    }

    @Override
    public Health health() {

        return Health.up()
                .withDetail("storage", "initialized")
                .build();
    }
}