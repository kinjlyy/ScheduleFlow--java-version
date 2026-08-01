package com.scheduleflow.event.service;

import com.scheduleflow.event.dto.*;
import com.scheduleflow.event.enums.EventCategory;
import com.scheduleflow.event.enums.EventStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * EventService — Defines the contract for Event business operations.
 *
 * <p>Phase 7A: Event CRUD.
 * <p>Phase 7B: Room Reservation.
 * <p>Phase 7C: Academic Event Scheduling &amp; Impact Analysis Orchestration.
 */
public interface EventService {

    // ── Phase 7A Event Operations ──────────────────────────────────────────────

    EventResponse createEvent(CreateEventRequest request);
    EventResponse getEventById(Long id);
    List<EventSummaryResponse> getAllEvents();
    EventResponse updateEvent(Long id, UpdateEventRequest request);
    void deleteEvent(Long id);
    List<EventSummaryResponse> getEventsByDate(LocalDate date);
    List<EventSummaryResponse> getEventsByStatus(EventStatus status);
    List<EventSummaryResponse> getEventsByCategory(EventCategory category);

    // ── Phase 7B Room Reservation Operations ───────────────────────────────────

    ReservationResponse reserveRoom(CreateReservationRequest request);
    void cancelReservation(Long id);
    /**
     * Checks room availability for a given date and period range.
     *
     * @param date        the calendar date (used to derive day-of-week)
     * @param startPeriod 1-indexed start period
     * @param endPeriod   1-indexed end period
     * @param timetableId optional — if provided, occupied rooms are derived from this specific timetable's lectures;
     *                    if null, falls back to the active timetable
     */
    AvailabilityResponse checkAvailability(LocalDate date, Integer startPeriod, Integer endPeriod, Long timetableId);
    List<ReservationResponse> getReservations(LocalDate date, Long locationId, EventStatus status);

    // ── Phase 7C Academic Event Scheduling & Impact Operations ────────────────

    /**
     * Read-only Impact Analysis calculation.
     * Determines which lectures, teachers, sections, and rooms will be affected.
     * Advances Event status to {@code IMPACT_ANALYZED}.
     */
    ImpactAnalysisResponse generateImpactAnalysis(ImpactAnalysisRequest request);

    /**
     * Read-only Execution Plan generation.
     * Calculates lectures to reschedule vs cancel for a chosen strategy without modifying timetable state.
     * Advances Event status to {@code READY_FOR_EXECUTION}.
     */
    ExecutionPlanResponse generateExecutionPlan(ExecutionPlanRequest request);

    /**
     * Executes the chosen strategy via TIMETABLE-SERVICE orchestration.
     * Advances Event status to {@code EXECUTING} → {@code COMPLETED} (or {@code FAILED} on error).
     */
    ExecutionResponse executeStrategy(Long id, ExecutionRequest request);

    /**
     * Retrieves lightweight execution history stored on the Event entity.
     */
    ExecutionHistoryResponse getExecutionHistory(Long id);
}
