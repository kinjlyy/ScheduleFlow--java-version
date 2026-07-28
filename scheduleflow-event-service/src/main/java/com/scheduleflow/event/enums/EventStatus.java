package com.scheduleflow.event.enums;

/**
 * EventStatus — Represents the lifecycle status of an Event.
 *
 * <p>Phase 7C Lifecycle for Academic Events:
 * DRAFT → IMPACT_ANALYZED → READY_FOR_EXECUTION → EXECUTING → COMPLETED / FAILED
 *
 * <p>Room Reservation Lifecycle:
 * SCHEDULED → CANCELLED
 */
public enum EventStatus {
    /** Initial state; event created but not yet analyzed. */
    DRAFT,
    /** Room reservation confirmed or generic event scheduled. */
    SCHEDULED,
    /** Timetable impact has been calculated (read-only analysis complete). */
    IMPACT_ANALYZED,
    /** Execution plan has been generated; awaiting administrator approval. */
    READY_FOR_EXECUTION,
    /** Execution in progress — do not issue concurrent executions. */
    EXECUTING,
    /** Execution completed successfully. */
    COMPLETED,
    /** Event or reservation was cancelled (soft-delete). */
    CANCELLED,
    /** Execution failed; check executionSummary for error details. */
    FAILED
}
