package com.scheduleflow.provider;

/**
 * Abstraction for publishing domain events.
 *
 * <p><strong>Current implementation:</strong> {@link NoOpEventPublisher} — does nothing.
 *
 * <p><strong>Future implementation:</strong> In Phase 5 (Event Service), replace with a
 * Kafka or RabbitMQ publisher. Services that call this interface need no code changes.
 *
 * <p>Domain events in the Timetable Service include:
 * <ul>
 *   <li>TIMETABLE_GENERATED — a new timetable was successfully generated</li>
 *   <li>TIMETABLE_ARCHIVED  — a previous active timetable was archived</li>
 *   <li>LECTURE_UPDATED     — a lecture slot was modified</li>
 * </ul>
 */
public interface EventPublisher {

    /**
     * Publishes a domain event.
     *
     * @param eventType  a string identifying the event (e.g., "TIMETABLE_GENERATED")
     * @param payload    the event data as a string (JSON or plain text)
     */
    void publish(String eventType, String payload);
}
