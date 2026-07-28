package com.scheduleflow.event.exception;

import feign.FeignException;
import feign.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler — Centralized exception handling for the Event Service.
 *
 * <p>Intercepts all uncaught exceptions including Feign communication failures
 * and converts them to structured JSON error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── ResourceNotFoundException ──────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage());
    }

    // ── ReservationConflictException ──────────────────────────────────────────

    @ExceptionHandler(ReservationConflictException.class)
    public ResponseEntity<Map<String, Object>> handleReservationConflict(ReservationConflictException ex) {
        log.warn("Reservation conflict: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "Reservation Conflict", ex.getMessage());
    }

    // ── ValidationException ────────────────────────────────────────────────────

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(ValidationException ex) {
        log.warn("Validation failed: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation Error", ex.getMessage());
    }

    // ── Feign Inter-Service Communication Exception Handlers ─────────────────

    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<Map<String, Object>> handleFeignNotFound(FeignException.NotFound ex) {
        log.warn("Downstream Feign call returned 404: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Downstream Resource Not Found",
                "The requested resource was not found in the downstream service.");
    }

    @ExceptionHandler({FeignException.ServiceUnavailable.class, FeignException.BadGateway.class, RetryableException.class})
    public ResponseEntity<Map<String, Object>> handleFeignServiceUnavailable(Exception ex) {
        log.error("Downstream service unavailable: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "Downstream Service Unavailable",
                "Unable to connect to downstream service. Please try again later.");
    }

    @ExceptionHandler(FeignException.BadRequest.class)
    public ResponseEntity<Map<String, Object>> handleFeignBadRequest(FeignException.BadRequest ex) {
        log.warn("Downstream Feign call returned 400: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Downstream Validation Error",
                "Downstream service rejected the request parameters.");
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, Object>> handleGenericFeignException(FeignException ex) {
        log.error("Feign inter-service communication error: status={}, message={}", ex.status(), ex.getMessage());
        HttpStatus status = ex.status() > 0 ? HttpStatus.resolve(ex.status()) : HttpStatus.BAD_GATEWAY;
        if (status == null) status = HttpStatus.BAD_GATEWAY;
        return buildErrorResponse(status, "Inter-Service Communication Error",
                "An error occurred while communicating with downstream microservices.");
    }

    // ── Bean Validation (@Valid) ───────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.warn("Bean validation failed: {}", fieldErrors);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("message", "One or more fields failed validation");
        body.put("fieldErrors", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ── Illegal Argument ───────────────────────────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Request", ex.getMessage());
    }

    // ── Generic Fallback ───────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected internal error in Event Service", ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred. Please try again later."
        );
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
