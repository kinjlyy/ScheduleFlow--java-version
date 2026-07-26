package com.scheduleflow.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * No-operation implementation of {@link EventPublisher}.
 *
 * <p>Active in all environments until an Event Service or message broker is introduced.
 * Logs the event at INFO level so developers can observe the event lifecycle.
 *
 * <p><strong>Replacement strategy:</strong>
 * Implement {@link EventPublisher} with a Kafka producer or AMQP publisher and annotate
 * with {@code @Primary}. The service layer requires zero changes.
 */
@Component
@Primary
public class NoOpEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NoOpEventPublisher.class);

    @Override
    public void publish(String eventType, String payload) {
        log.info("[NoOp] Domain event suppressed — type={}, payload-preview={}",
                eventType, truncate(payload, 80));
        // No-operation: plug in Kafka/RabbitMQ publisher in Phase 5.
    }

    private String truncate(String s, int max) {
        if (s == null) return "null";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
