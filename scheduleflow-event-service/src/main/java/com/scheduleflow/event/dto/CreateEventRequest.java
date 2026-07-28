package com.scheduleflow.event.dto;

import com.scheduleflow.event.enums.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO for creating a new Event.
 */
public class CreateEventRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Event type is required")
    private EventType eventType;

    @NotNull(message = "Event category is required")
    private EventCategory eventCategory;

    @NotNull(message = "Date is required")
    @FutureOrPresent(message = "Event date must be today or in the future")
    private LocalDate date;

    @NotNull(message = "Start period is required")
    @Min(value = 1, message = "Start period must be at least 1")
    private Integer startPeriod;

    @NotNull(message = "End period is required")
    @Min(value = 1, message = "End period must be at least 1")
    private Integer endPeriod;

    private Long locationId;

    private LocationType locationType;

    private Long timetableId;

    @NotBlank(message = "Organizer is required")
    @Size(max = 200, message = "Organizer name must not exceed 200 characters")
    private String organizer;

    private EventStatus status;

    private ExecutionStrategy executionStrategy;

    @Size(max = 200, message = "CreatedBy must not exceed 200 characters")
    private String createdBy;

    public CreateEventRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public EventCategory getEventCategory() { return eventCategory; }
    public void setEventCategory(EventCategory eventCategory) { this.eventCategory = eventCategory; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Integer getStartPeriod() { return startPeriod; }
    public void setStartPeriod(Integer startPeriod) { this.startPeriod = startPeriod; }

    public Integer getEndPeriod() { return endPeriod; }
    public void setEndPeriod(Integer endPeriod) { this.endPeriod = endPeriod; }

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }

    public LocationType getLocationType() { return locationType; }
    public void setLocationType(LocationType locationType) { this.locationType = locationType; }

    public Long getTimetableId() { return timetableId; }
    public void setTimetableId(Long timetableId) { this.timetableId = timetableId; }

    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }

    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }

    public ExecutionStrategy getExecutionStrategy() { return executionStrategy; }
    public void setExecutionStrategy(ExecutionStrategy executionStrategy) { this.executionStrategy = executionStrategy; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
