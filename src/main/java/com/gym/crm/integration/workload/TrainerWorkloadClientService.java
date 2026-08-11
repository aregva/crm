package com.gym.crm.integration.workload;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Wraps the Feign call to trainer-workload-service with a circuit breaker: if the
 * remote service is failing, unreachable, or the breaker is open, the failure is
 * caught and logged so the caller's local training add/cancel is never blocked or
 * rolled back by a downstream outage.
 */
@Service
public class TrainerWorkloadClientService {
    private static final String CIRCUIT_BREAKER_NAME = "trainerWorkload";

    private static final Logger log = LoggerFactory.getLogger(TrainerWorkloadClientService.class);

    private final TrainerWorkloadClient client;
    private final CircuitBreaker circuitBreaker;

    public TrainerWorkloadClientService(TrainerWorkloadClient client, CircuitBreakerRegistry circuitBreakerRegistry) {
        this.client = client;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NAME);
    }

    public void notify(TrainerWorkloadRequest request) {
        try {
            circuitBreaker.executeRunnable(() -> client.submitWorkload(request));
        } catch (Exception ex) {
            log.error("Workload sync skipped, trainer-workload-service unavailable: trainer={}, action={}, error={}",
                    request.trainerUsername(), request.actionType(), ex.getMessage());
        }
    }
}
