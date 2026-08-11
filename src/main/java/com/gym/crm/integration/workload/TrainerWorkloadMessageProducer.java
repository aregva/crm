package com.gym.crm.integration.workload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.crm.rest.logging.TransactionLoggingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

/**
 * Publishes trainer workload events to {@code trainer-workload-service} over
 * ActiveMQ instead of calling it synchronously. A broker outage or send failure
 * is caught and logged here rather than propagated, so it never fails the
 * caller's local training add/cancel — the same guarantee the old circuit
 * breaker fallback gave the synchronous REST call.
 */
@Service
public class TrainerWorkloadMessageProducer {
    private static final Logger log = LoggerFactory.getLogger(TrainerWorkloadMessageProducer.class);

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;
    private final String queueName;

    public TrainerWorkloadMessageProducer(JmsTemplate jmsTemplate,
                                          ObjectMapper objectMapper,
                                          @Value("${messaging.trainer-workload.queue}") String queueName) {
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = objectMapper;
        this.queueName = queueName;
    }

    public void send(TrainerWorkloadMessage message) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            String transactionId = MDC.get(TransactionLoggingFilter.TRANSACTION_ID_MDC_KEY);

            jmsTemplate.send(queueName, session -> {
                var textMessage = session.createTextMessage(payload);
                if (transactionId != null && !transactionId.isBlank()) {
                    textMessage.setStringProperty(TransactionLoggingFilter.TRANSACTION_ID_MDC_KEY, transactionId);
                }
                return textMessage;
            });

            log.info("Published {} workload message trainer={} date={}",
                    message.actionType(), message.trainerUsername(), message.trainingDate());
        } catch (Exception ex) {
            log.error("Workload message not sent, trainer-workload-service messaging unavailable: trainer={}, action={}, error={}",
                    message.trainerUsername(), message.actionType(), ex.getMessage());
        }
    }
}
