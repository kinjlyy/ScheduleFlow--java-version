-- =============================================================================
--  V3__update_status_check_constraint.sql
--  Safely updates the events_status_check constraint on the events table to
--  match all Java EventStatus enum values.
--
--  EventStatus enum values:
--  DRAFT, SCHEDULED, IMPACT_ANALYZED, READY_FOR_EXECUTION, EXECUTING,
--  COMPLETED, CANCELLED, FAILED
-- =============================================================================

-- Drop the old constraint (created by Hibernate on initial schema creation)
ALTER TABLE events
    DROP CONSTRAINT IF EXISTS events_status_check;

-- Re-add with all current EventStatus enum values
ALTER TABLE events
    ADD CONSTRAINT events_status_check
    CHECK (status IN (
        'DRAFT',
        'SCHEDULED',
        'IMPACT_ANALYZED',
        'READY_FOR_EXECUTION',
        'EXECUTING',
        'COMPLETED',
        'CANCELLED',
        'FAILED'
    ));

-- Verification test inside migration
DO $$
DECLARE
    test_id BIGINT;
BEGIN
    INSERT INTO events (
        title, event_type, event_category, date,
        start_period, end_period, status, created_at
    ) VALUES (
        '_flyway_status_check', 'LECTURE', 'GENERAL',
        CURRENT_DATE, 1, 1, 'FAILED', NOW()
    )
    RETURNING id INTO test_id;

    DELETE FROM events WHERE id = test_id;
END $$;
