package com.scheduleflow.provider;

/**
 * Abstraction for audit trail publishing.
 *
 * <p><strong>Current implementation:</strong> {@link LocalAuditPublisher} — logs to the local
 * application log (SLF4J). Audit entries are visible in logs but not persisted to a dedicated store.
 *
 * <p><strong>Future implementation:</strong> Replace with a database-backed or external
 * audit service client when compliance requirements demand it.
 *
 * <p>All user-initiated or system actions that modify timetable state should be published here.
 */
public interface AuditPublisher {

    /**
     * Records an auditable action.
     *
     * @param actor  who performed the action (e.g., user email, "system")
     * @param action what happened (e.g., "TIMETABLE_GENERATED", "ROOM_CREATED")
     * @param detail additional context
     */
    void audit(String actor, String action, String detail);
}
