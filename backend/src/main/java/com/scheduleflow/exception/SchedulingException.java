package com.scheduleflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when the graph-coloring scheduling engine encounters an unrecoverable error.
 * Maps to HTTP 422 Unprocessable Entity.
 *
 * Distinct from warnings (soft failures reported inside TimetableResponseDTO).
 * This exception represents hard failures that prevent timetable generation entirely.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class SchedulingException extends RuntimeException {

    public SchedulingException(String message) {
        super(message);
    }

    public SchedulingException(String message, Throwable cause) {
        super(message, cause);
    }
}
