package com.scheduleflow.util;

/**
 * Shared constants used across the Timetable Service.
 *
 * <p>Centralises values that were previously duplicated across
 * {@code SchedulerService} and {@code TimetableService}.
 */
public final class TimetableConstants {

    private TimetableConstants() {
        // Utility class — do not instantiate
    }

    /**
     * Canonical day names used in timetable grids.
     * Index 0 = Monday, index 6 = Sunday.
     */
    public static final String[] DAYS = {
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };
}
