package com.scheduleflow.provider;

/**
 * Abstraction for outbound notifications.
 *
 * <p><strong>Current implementation:</strong> {@link NoOpNotificationProvider} — does nothing.
 *
 * <p><strong>Future implementation:</strong> In a later phase, replace with an SMTP-based
 * implementation or a Notification Service client (e.g., via RabbitMQ/Kafka).
 *
 * <p>Callers (services) must depend only on this interface — never on the implementation.
 */
public interface NotificationProvider {

    /**
     * Sends a notification about a timetable lifecycle event.
     *
     * @param recipient the target email / user identifier
     * @param subject   the notification subject
     * @param body      the notification body
     */
    void notify(String recipient, String subject, String body);
}
