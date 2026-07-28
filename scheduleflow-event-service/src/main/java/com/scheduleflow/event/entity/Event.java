package com.scheduleflow.event.entity;

import com.scheduleflow.event.enums.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Event Entity representing room reservation events and academic events.
 *
 * <p>Future-Proof Design (Phase 7A Foundation):
 * Prepared for future Room Reservation and Timetable Event Scheduling workflows
 * without requiring schema changes.
 */
@Entity
@Table(
    name = "events",
    indexes = {
        @Index(name = "idx_event_date", columnList = "date"),
        @Index(name = "idx_event_status", columnList = "status"),
        @Index(name = "idx_event_category", columnList = "event_category"),
        @Index(name = "idx_event_timetable", columnList = "timetable_id"),
        @Index(name = "idx_event_location", columnList = "location_id, location_type")
    }
)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_category", nullable = false)
    private EventCategory eventCategory;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "start_period", nullable = false)
    private Integer startPeriod;

    @Column(name = "end_period", nullable = false)
    private Integer endPeriod;

    @Column(name = "location_id")
    private Long locationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type")
    private LocationType locationType;

    @Column(name = "timetable_id")
    private Long timetableId;

    @Column(name = "organizer")
    private String organizer;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EventStatus status = EventStatus.SCHEDULED;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_strategy")
    private ExecutionStrategy executionStrategy;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    @Column(name = "executed_by")
    private String executedBy;

    @Column(name = "execution_started_at")
    private LocalDateTime executionStartedAt;

    @Column(name = "execution_completed_at")
    private LocalDateTime executionCompletedAt;

    @Column(name = "execution_summary", columnDefinition = "TEXT")
    private String executionSummary;

    @Column(name = "execution_result", columnDefinition = "TEXT")
    private String executionResult;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Event() {}

    public Event(String title, String description, EventType eventType, EventCategory eventCategory,
                 LocalDate date, Integer startPeriod, Integer endPeriod, Long locationId,
                 LocationType locationType, Long timetableId, String organizer, EventStatus status,
                 ExecutionStrategy executionStrategy, String createdBy, LocalDateTime createdAt) {
        this.title = title;
        this.description = description;
        this.eventType = eventType;
        this.eventCategory = eventCategory;
        this.date = date;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
        this.locationId = locationId;
        this.locationType = locationType;
        this.timetableId = timetableId;
        this.organizer = organizer;
        this.status = status != null ? status : EventStatus.SCHEDULED;
        this.executionStrategy = executionStrategy;
        this.createdBy = createdBy;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }

    public String getExecutedBy() { return executedBy; }
    public void setExecutedBy(String executedBy) { this.executedBy = executedBy; }

    public LocalDateTime getExecutionStartedAt() { return executionStartedAt; }
    public void setExecutionStartedAt(LocalDateTime executionStartedAt) { this.executionStartedAt = executionStartedAt; }

    public LocalDateTime getExecutionCompletedAt() { return executionCompletedAt; }
    public void setExecutionCompletedAt(LocalDateTime executionCompletedAt) { this.executionCompletedAt = executionCompletedAt; }

    public String getExecutionSummary() { return executionSummary; }
    public void setExecutionSummary(String executionSummary) { this.executionSummary = executionSummary; }

    public String getExecutionResult() { return executionResult; }
    public void setExecutionResult(String executionResult) { this.executionResult = executionResult; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
