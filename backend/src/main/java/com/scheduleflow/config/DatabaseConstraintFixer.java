package com.scheduleflow.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * DatabaseConstraintFixer — Programmatically updates PostgreSQL check constraints
 * on application startup.
 *
 * <p>Ensures that lectures_lecture_type_check permits THEORY, LAB, and EVENT
 * on the exact production database connected to TIMETABLE-SERVICE, regardless of
 * Flyway schema history locks or pre-existing Hibernate check constraints.
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
        log.info("▶ Starting programmatic database constraint verification for TIMETABLE-SERVICE...");
        try {
            // Drop any legacy check constraints on lectures.lecture_type
            jdbcTemplate.execute("ALTER TABLE lectures DROP CONSTRAINT IF EXISTS lectures_lecture_type_check");
            jdbcTemplate.execute("ALTER TABLE lectures DROP CONSTRAINT IF EXISTS lectures_lecture_type_check1");
            jdbcTemplate.execute("ALTER TABLE lectures DROP CONSTRAINT IF EXISTS lectures_lecture_type_check2");

            // Add clean constraint matching LectureType enum (THEORY, LAB, EVENT)
            jdbcTemplate.execute(
                "ALTER TABLE lectures ADD CONSTRAINT lectures_lecture_type_check CHECK (lecture_type IN ('THEORY', 'LAB', 'EVENT'))"
            );

            log.info("✔ Successfully updated PostgreSQL constraint 'lectures_lecture_type_check' to ('THEORY', 'LAB', 'EVENT')");
        } catch (Exception ex) {
            log.error("⚠️ Failed to update database check constraint on startup: {}", ex.getMessage(), ex);
        }
    }
}
