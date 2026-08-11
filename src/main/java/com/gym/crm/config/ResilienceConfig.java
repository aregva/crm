package com.gym.crm.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Registers a plain (non-autoconfigured) Resilience4j {@link CircuitBreakerRegistry}
 * bean so the circuit breaker around calls to trainer-workload-service works
 * identically in the full Spring Boot application and in lightweight
 * {@code AnnotationConfigApplicationContext}-based unit tests that don't trigger
 * Spring Boot autoconfiguration.
 */
@Configuration
public class ResilienceConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(
            @Value("${resilience4j.trainer-workload.sliding-window-size:10}") int slidingWindowSize,
            @Value("${resilience4j.trainer-workload.minimum-number-of-calls:5}") int minimumNumberOfCalls,
            @Value("${resilience4j.trainer-workload.failure-rate-threshold:50}") float failureRateThreshold,
            @Value("${resilience4j.trainer-workload.wait-duration-in-open-state-seconds:10}") long waitDurationInOpenStateSeconds,
            @Value("${resilience4j.trainer-workload.permitted-calls-in-half-open-state:3}") int permittedCallsInHalfOpenState) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(slidingWindowSize)
                .minimumNumberOfCalls(minimumNumberOfCalls)
                .failureRateThreshold(failureRateThreshold)
                .waitDurationInOpenState(Duration.ofSeconds(waitDurationInOpenStateSeconds))
                .permittedNumberOfCallsInHalfOpenState(permittedCallsInHalfOpenState)
                .build();
        return CircuitBreakerRegistry.of(config);
    }
}
