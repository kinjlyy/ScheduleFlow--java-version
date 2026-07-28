package com.scheduleflow.event.dto;

import com.scheduleflow.event.enums.EventType;
import com.scheduleflow.event.enums.LocationType;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * CreateReservationRequest — Request body for creating a room reservation.
 *
 * <p>{@code eventCategory} is intentionally excluded — it is always forced to
 * {@code ROOM_RESERVATION} by the service layer. Clients cannot override this.
 * {@code executionStrategy} and {@code timetableId} are also excluded: they are
 * Phase 7C concerns and irrelevant for room-only reservations.
 */
public class CreateReservationRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Event type is required")
    private EventType eventType;

    @NotNull(message = "Date is required")
    @FutureOrPresent(message = "Reservation date must be today or in the future")
    private LocalDate date;

    @NotNull(message = "Start period is required")
    @Min(value = 1, message = "Start period must be at least 1")
    private Integer startPeriod;

    @NotNull(message = "End period is required")
    @Min(value = 1, message = "End period must be at least 1")
    private Integer endPeriod;

    @NotNull(message = "locationId is required for room reservations")
    private Long locationId;

    private LocationType locationType;

    @NotBlank(message = "Organizer is required")
    @Size(max = 200, message = "Organizer name must not exceed 200 characters")
    private String organizer;

    @Size(max = 200, message = "createdBy must not exceed 200 characters")
    private String createdBy;

    public CreateReservationRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

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

    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
