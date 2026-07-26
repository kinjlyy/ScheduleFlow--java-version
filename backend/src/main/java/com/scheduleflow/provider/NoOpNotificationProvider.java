package com.scheduleflow.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * No-operation implementation of {@link NotificationProvider}.
 *
 * <p>Active in all environments until a real notification mechanism is wired.
 * Logs the notification intent at DEBUG level so developers can confirm the call chain works.
 *
 * <p><strong>Replacement strategy:</strong>
 * Implement {@link NotificationProvider} in a new class (e.g., {@code SmtpNotificationProvider}
 * or {@code KafkaNotificationProvider}) and annotate it with {@code @Primary} to override this bean.
 */
@Component
@Primary
public class NoOpNotificationProvider implements NotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(NoOpNotificationProvider.class);

    @Override
    public void notify(String recipient, String subject, String body) {
        log.debug("[NoOp] Notification suppressed — recipient={}, subject={}", recipient, subject);
        // No-operation: replace this implementation in a future phase.
    }
}
