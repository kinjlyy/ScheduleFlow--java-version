package com.scheduleflow.event.dto;

import com.scheduleflow.event.enums.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO for updating an existing Event.
 * All fields are optional — only non-null fields will be applied.
 */
public class UpdateEventRequest {

    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private EventType eventType;

    private EventCategory eventCategory;

    @FutureOrPresent(message = "Event date must be today or in the future")
    private LocalDate date;

    @Min(value = 1, message = "Start period must be at least 1")
    private Integer startPeriod;

    @Min(value = 1, message = "End period must be at least 1")
    private Integer endPeriod;

    private Long locationId;

    private LocationType locationType;

    private Long timetableId;

    @Size(max = 200, message = "Organizer name must not exceed 200 characters")
    private String organizer;

    private EventStatus status;

    private ExecutionStrategy executionStrategy;

    @Size(max = 200, message = "LastModifiedBy must not exceed 200 characters")
    private String lastModifiedBy;

    public UpdateEventRequest() {}

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

    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }
}
