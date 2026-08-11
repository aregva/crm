package com.gym.crm.integration.workload;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TrainerWorkloadClientServiceTest {

    private final TrainerWorkloadClient client = Mockito.mock(TrainerWorkloadClient.class);
    private TrainerWorkloadClientService service;

    @BeforeEach
    void setUp() {
        service = new TrainerWorkloadClientService(client, CircuitBreakerRegistry.ofDefaults());
    }

    @Test
    void notify_ClientSucceeds_DelegatesToClient() {
        TrainerWorkloadRequest request = request();

        service.notify(request);

        verify(client, times(1)).submitWorkload(request);
    }

    @Test
    void notify_ClientThrows_FallbackSwallowsException() {
        TrainerWorkloadRequest request = request();
        doThrow(new RuntimeException("trainer-workload-service unreachable")).when(client).submitWorkload(request);

        assertDoesNotThrow(() -> service.notify(request));
    }

    private TrainerWorkloadRequest request() {
        return new TrainerWorkloadRequest(
                "trainer.one", "Trainer", "One", true, LocalDate.now(), 60, ActionType.ADD);
    }
}
