package com.scheduleflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when request input fails business-level validation.
 * Maps to HTTP 400 Bad Request.
 *
 * Examples:
 *   - Duplicate room number on create
 *   - Request with no sections
 *   - daysPerWeek or periodsPerDay is zero
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
