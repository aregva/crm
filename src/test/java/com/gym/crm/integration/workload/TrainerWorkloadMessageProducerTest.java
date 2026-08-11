package com.gym.crm.integration.workload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym.crm.rest.logging.TransactionLoggingFilter;
import jakarta.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainerWorkloadMessageProducerTest {

    private static final String QUEUE = "test.trainer.workload.queue";

    @Test
    void send_ValidMessage_PublishesJsonWithTransactionIdProperty() throws Exception {
        // A raw (uncached) ActiveMQConnectionFactory closes its connection after each
        // send()/receive() call; since the vm:// transport tears the embedded broker down
        // once its last connection closes, the broker (and its in-memory queue) would be
        // recreated from scratch between send() and receive(). Caching the connection, like
        // Spring Boot's own ActiveMQAutoConfiguration does in production, keeps one broker
        // instance alive for both calls.
        ActiveMQConnectionFactory targetConnectionFactory =
                new ActiveMQConnectionFactory("vm://producer-test-broker?broker.persistent=false&broker.useJmx=false");
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(targetConnectionFactory);
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setReceiveTimeout(5000);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        TrainerWorkloadMessageProducer producer = new TrainerWorkloadMessageProducer(jmsTemplate, objectMapper, QUEUE);

        MDC.put(TransactionLoggingFilter.TRANSACTION_ID_MDC_KEY, "tx-producer-test");
        try {
            producer.send(new TrainerWorkloadMessage(
                    "trainer.one", "Trainer", "One", true, LocalDate.of(2026, 7, 1), 60, ActionType.ADD));
        } finally {
            MDC.remove(TransactionLoggingFilter.TRANSACTION_ID_MDC_KEY);
        }

        TextMessage received = (TextMessage) jmsTemplate.receive(QUEUE);
        assertNotNull(received, "expected a message on " + QUEUE);
        assertTrue(received.getText().contains("\"trainerUsername\":\"trainer.one\""));
        assertTrue(received.getText().contains("\"actionType\":\"ADD\""));
        assertEquals("tx-producer-test", received.getStringProperty(TransactionLoggingFilter.TRANSACTION_ID_MDC_KEY));
    }

    @Test
    void send_BrokerUnavailable_DoesNotThrow() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:1");
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        TrainerWorkloadMessageProducer producer = new TrainerWorkloadMessageProducer(jmsTemplate, objectMapper, QUEUE);

        assertDoesNotThrow(() -> producer.send(
                new TrainerWorkloadMessage("trainer.two", "Trainer", "Two", true, LocalDate.now(), 30, ActionType.DELETE)));
    }
}
