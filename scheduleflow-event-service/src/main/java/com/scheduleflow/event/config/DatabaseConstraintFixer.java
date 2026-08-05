package com.scheduleflow.event.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * DatabaseConstraintFixer — Programmatically updates PostgreSQL check constraints
 * on Event Service application startup.
 */
@Component
public class DatabaseConstraintFixer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConstraintFixer.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseConstraintFixer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void fixCheckConstraints() {
        log.info("▶ Starting programmatic database constraint verification for EVENT-SERVICE...");
        try {
            // 1. events_event_type_check
            jdbcTemplate.execute("ALTER TABLE events DROP CONSTRAINT IF EXISTS events_event_type_check");
            jdbcTemplate.execute(
                "ALTER TABLE events ADD CONSTRAINT events_event_type_check CHECK (event_type IN (" +
                "'LECTURE', 'MEETING', 'SEMINAR', 'WORKSHOP', 'CEREMONY', 'EXAM', 'SPORTS', 'CULTURAL', 'OTHER'))"
            );

            // 2. events_event_category_check
            jdbcTemplate.execute("ALTER TABLE events DROP CONSTRAINT IF EXISTS events_event_category_check");
            jdbcTemplate.execute(
                "ALTER TABLE events ADD CONSTRAINT events_event_category_check CHECK (event_category IN (" +
                "'GENERAL', 'ROOM_RESERVATION', 'TIMETABLE_EVENT'))"
            );

            // 3. events_status_check
            jdbcTemplate.execute("ALTER TABLE events DROP CONSTRAINT IF EXISTS events_status_check");
            jdbcTemplate.execute(
                "ALTER TABLE events ADD CONSTRAINT events_status_check CHECK (status IN (" +
                "'DRAFT', 'SCHEDULED', 'IMPACT_ANALYZED', 'READY_FOR_EXECUTION', 'EXECUTING', 'COMPLETED', 'CANCELLED', 'FAILED'))"
            );

            log.info("✔ Successfully verified all PostgreSQL check constraints for EVENT-SERVICE");
        } catch (Exception ex) {
            log.error("⚠️ Failed to update event service database check constraints on startup: {}", ex.getMessage(), ex);
        }
    }
}
