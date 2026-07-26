package com.scheduleflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested resource (Timetable, Lecture, Room) does not exist.
 * Maps to HTTP 404 Not Found.
 *
 * Replaces raw {@code RuntimeException("X not found")} patterns throughout the service.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final Object resourceId;

    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(String.format("%s not found with identifier: %s", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceType = "Resource";
        this.resourceId = null;
    }

    public String getResourceType() { return resourceType; }
    public Object getResourceId() { return resourceId; }
}
