-- =============================================================================
--  V2__update_enum_check_constraints.sql
--  Safely updates the CHECK constraints on the events table to match the
--  current Java enums.  No rows are dropped or modified.
--
--  EventType  now includes: LECTURE (new)
--  EventCategory now includes: GENERAL (new)
-- =============================================================================

-- ── events_event_type_check ───────────────────────────────────────────────────
-- Drop the old constraint (created by Hibernate on initial schema creation)
ALTER TABLE events
    DROP CONSTRAINT IF EXISTS events_event_type_check;

-- Re-add with all current EventType enum values
ALTER TABLE events
    ADD CONSTRAINT events_event_type_check
    CHECK (event_type IN (
        'LECTURE',
        'MEETING',
        'SEMINAR',
        'WORKSHOP',
        'CEREMONY',
        'EXAM',
        'SPORTS',
        'CULTURAL',
        'OTHER'
    ));

-- ── events_event_category_check ───────────────────────────────────────────────
-- Drop the old constraint
ALTER TABLE events
    DROP CONSTRAINT IF EXISTS events_event_category_check;

-- Re-add with all current EventCategory enum values
ALTER TABLE events
    ADD CONSTRAINT events_event_category_check
    CHECK (event_category IN (
        'GENERAL',
        'ROOM_RESERVATION',
        'TIMETABLE_EVENT'
    ));

-- ── Verification INSERT (rolled back immediately) ─────────────────────────────
-- This block runs inside Flyway's own transaction and confirms both new values
-- are accepted before the migration commits.
DO $$
DECLARE
    test_id BIGINT;
BEGIN
    INSERT INTO events (
        title, event_type, event_category, date,
        start_period, end_period, status, created_at
    ) VALUES (
        '_flyway_constraint_check', 'LECTURE', 'GENERAL',
        CURRENT_DATE, 1, 1, 'DRAFT', NOW()
    )
    RETURNING id INTO test_id;

    DELETE FROM events WHERE id = test_id;
END $$;
