package com.scheduleflow.event.mapper;

import com.scheduleflow.event.dto.CreateEventRequest;
import com.scheduleflow.event.dto.EventResponse;
import com.scheduleflow.event.dto.EventSummaryResponse;
import com.scheduleflow.event.dto.UpdateEventRequest;
import com.scheduleflow.event.entity.Event;
import com.scheduleflow.event.enums.EventStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * EventMapper — Converts between Event entity and DTOs.
 *
 * <p>This is a hand-written Spring {@code @Component} mapper.
 * It does NOT use MapStruct or any code-generation framework,
 * consistent with the existing service architecture.
 */
@Component
public class EventMapper {

    /**
     * Maps a {@link CreateEventRequest} to a new {@link Event} entity.
     * {@code id}, {@code createdAt}, and {@code updatedAt} are managed by JPA lifecycle callbacks.
     */
    public Event toEntity(CreateEventRequest request) {
        if (request == null) return null;

        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEventType(request.getEventType());
        event.setEventCategory(request.getEventCategory());
        event.setDate(request.getDate());
        event.setStartPeriod(request.getStartPeriod());
        event.setEndPeriod(request.getEndPeriod());
        event.setLocationId(request.getLocationId());
        event.setLocationType(request.getLocationType());
        event.setTimetableId(request.getTimetableId());
        event.setOrganizer(request.getOrganizer());
        event.setStatus(request.getStatus() != null ? request.getStatus() : EventStatus.SCHEDULED);
        event.setExecutionStrategy(request.getExecutionStrategy());
        event.setCreatedBy(request.getCreatedBy());
        event.setCreatedAt(LocalDateTime.now());
        return event;
    }

    /**
     * Applies non-null fields from {@link UpdateEventRequest} to an existing {@link Event} entity.
     * Fields not present in the request (null) are left unchanged.
     */
    public void applyUpdate(UpdateEventRequest request, Event event) {
        if (request == null || event == null) return;

        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getEventType() != null) event.setEventType(request.getEventType());
        if (request.getEventCategory() != null) event.setEventCategory(request.getEventCategory());
        if (request.getDate() != null) event.setDate(request.getDate());
        if (request.getStartPeriod() != null) event.setStartPeriod(request.getStartPeriod());
        if (request.getEndPeriod() != null) event.setEndPeriod(request.getEndPeriod());
        if (request.getLocationId() != null) event.setLocationId(request.getLocationId());
        if (request.getLocationType() != null) event.setLocationType(request.getLocationType());
        if (request.getTimetableId() != null) event.setTimetableId(request.getTimetableId());
        if (request.getOrganizer() != null) event.setOrganizer(request.getOrganizer());
        if (request.getStatus() != null) event.setStatus(request.getStatus());
        if (request.getExecutionStrategy() != null) event.setExecutionStrategy(request.getExecutionStrategy());
        if (request.getLastModifiedBy() != null) event.setLastModifiedBy(request.getLastModifiedBy());
    }

    /**
     * Maps an {@link Event} entity to a full {@link EventResponse}.
     */
    public EventResponse toResponse(Event event) {
        if (event == null) return null;

        EventResponse response = new EventResponse();
        response.setId(event.getId());
        response.setTitle(event.getTitle());
        response.setDescription(event.getDescription());
        response.setEventType(event.getEventType());
        response.setEventCategory(event.getEventCategory());
        response.setDate(event.getDate());
        response.setStartPeriod(event.getStartPeriod());
        response.setEndPeriod(event.getEndPeriod());
        response.setLocationId(event.getLocationId());
        response.setLocationType(event.getLocationType());
        response.setTimetableId(event.getTimetableId());
        response.setOrganizer(event.getOrganizer());
        response.setStatus(event.getStatus());
        response.setExecutionStrategy(event.getExecutionStrategy());
        response.setCreatedBy(event.getCreatedBy());
        response.setLastModifiedBy(event.getLastModifiedBy());
        response.setCreatedAt(event.getCreatedAt());
        response.setUpdatedAt(event.getUpdatedAt());
        return response;
    }

    /**
     * Maps an {@link Event} entity to a lightweight {@link EventSummaryResponse}.
     */
    public EventSummaryResponse toSummary(Event event) {
        if (event == null) return null;

        EventSummaryResponse summary = new EventSummaryResponse();
        summary.setId(event.getId());
        summary.setTitle(event.getTitle());
        summary.setEventType(event.getEventType());
        summary.setEventCategory(event.getEventCategory());
        summary.setDate(event.getDate());
        summary.setStartPeriod(event.getStartPeriod());
        summary.setEndPeriod(event.getEndPeriod());
        summary.setLocationId(event.getLocationId());
        summary.setLocationType(event.getLocationType());
        summary.setOrganizer(event.getOrganizer());
        summary.setStatus(event.getStatus());
        return summary;
    }
}
