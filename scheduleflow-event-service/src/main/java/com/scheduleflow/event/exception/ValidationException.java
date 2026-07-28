package com.scheduleflow.event.exception;

/**
 * Thrown when an event operation violates domain validation rules.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
