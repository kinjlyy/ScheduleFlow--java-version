package com.scheduleflow.exception;

/**
 * Domain / Application exception thrown when communication with {@code RESOURCE-SERVICE} fails or times out.
 *
 * <p><strong>Transport Agnostic:</strong>
 * This exception belongs to the Application / Domain Layer and contains zero HTTP or framework-specific logic.
 * It can be thrown safely by scheduled tasks, asynchronous background jobs, or internal services.
 * When it reaches the Web REST Layer, {@link GlobalExceptionHandler} translates it to an HTTP 503 Service Unavailable response.
 */
public class RoomServiceUnavailableException extends RuntimeException {

    public RoomServiceUnavailableException(String message) {
        super(message);
    }

    public RoomServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
