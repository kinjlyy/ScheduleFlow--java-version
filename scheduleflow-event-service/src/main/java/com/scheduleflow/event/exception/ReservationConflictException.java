package com.scheduleflow.event.exception;

/**
 * Thrown when a room reservation conflicts with an existing active reservation
 * for the same location, date, and overlapping period range.
 *
 * Maps to HTTP 409 Conflict.
 */
public class ReservationConflictException extends RuntimeException {

    public ReservationConflictException(String message) {
        super(message);
    }
}
