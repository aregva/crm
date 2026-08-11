package com.gym.crm.integration.workload;

import com.gym.crm.rest.logging.TransactionLoggingFilter;
import com.gym.crm.security.JwtTokenService;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.time.Duration;

/**
 * Authenticates gym-crm's outbound calls to trainer-workload-service with a
 * service-level JWT bearer token, and propagates the current transactionId so
 * both services' logs can be correlated for the same operation.
 */
@Configuration
public class WorkloadFeignConfig {

    @Bean
    public RequestInterceptor workloadServiceRequestInterceptor(
            JwtTokenService jwtTokenService,
            @Value("${security.jwt.service.audience:trainer-workload-service}") String audience,
            @Value("${security.jwt.service.ttl-seconds:60}") long ttlSeconds) {
        return new WorkloadServiceRequestInterceptor(jwtTokenService, audience, ttlSeconds);
    }

    private record WorkloadServiceRequestInterceptor(
            JwtTokenService jwtTokenService,
            String audience,
            long ttlSeconds) implements RequestInterceptor {

        @Override
        public void apply(RequestTemplate template) {
            String token = jwtTokenService.generateServiceToken("gym-crm", audience, Duration.ofSeconds(ttlSeconds));
            template.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);

            String transactionId = MDC.get("transactionId");
            if (transactionId != null && !transactionId.isBlank()) {
                template.header(TransactionLoggingFilter.TRANSACTION_ID_HEADER, transactionId);
            }
        }
    }
}
