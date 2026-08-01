package com.scheduleflow.event.controller;

import com.scheduleflow.event.dto.*;
import com.scheduleflow.event.enums.EventCategory;
import com.scheduleflow.event.enums.EventStatus;
import com.scheduleflow.event.service.EventService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * EventController — REST controller for Event, Room Reservation &amp; Academic Event operations.
 *
 * <p>Base path: {@code /api/events}
 * Routed from API Gateway: {@code /api/events/**} → {@code lb://EVENT-SERVICE}
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private static final Logger log = LoggerFactory.getLogger(EventController.class);

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // ── Phase 7A General Event Endpoints ───────────────────────────────────────

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {
        log.info("POST /api/events — Creating event: {}", request.getTitle());
        EventResponse response = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<EventSummaryResponse>> getAllEvents() {
        log.debug("GET /api/events — Fetching all events");
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        log.debug("GET /api/events/{} — Fetching event details", id);
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventRequest request) {
        log.info("PUT /api/events/{} — Updating event", id);
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        log.info("DELETE /api/events/{} — Deleting event", id);
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-date")
    public ResponseEntity<List<EventSummaryResponse>> getEventsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.debug("GET /api/events/by-date?date={}", date);
        return ResponseEntity.ok(eventService.getEventsByDate(date));
    }

    @GetMapping("/by-status")
    public ResponseEntity<List<EventSummaryResponse>> getEventsByStatus(
            @RequestParam EventStatus status) {
        log.debug("GET /api/events/by-status?status={}", status);
        return ResponseEntity.ok(eventService.getEventsByStatus(status));
    }

    @GetMapping("/by-category")
    public ResponseEntity<List<EventSummaryResponse>> getEventsByCategory(
            @RequestParam EventCategory category) {
        log.debug("GET /api/events/by-category?category={}", category);
        return ResponseEntity.ok(eventService.getEventsByCategory(category));
    }

    // ── Phase 7B Room Reservation Endpoints ────────────────────────────────────

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> reserveRoom(
            @Valid @RequestBody CreateReservationRequest request) {
        log.info("POST /api/events/reservations — Reserving room locationId={} for date={}",
                request.getLocationId(), request.getDate());
        ReservationResponse response = eventService.reserveRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        log.info("DELETE /api/events/reservations/{} — Cancelling reservation", id);
        eventService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/availability")
    public ResponseEntity<AvailabilityResponse> checkAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Integer startPeriod,
            @RequestParam Integer endPeriod,
            @RequestParam(required = false) Long timetableId) {
        log.debug("GET /api/events/availability — date={}, periods={}-{}, timetableId={}", date, startPeriod, endPeriod, timetableId);
        AvailabilityResponse response = eventService.checkAvailability(date, startPeriod, endPeriod, timetableId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> getReservations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) EventStatus status) {
        log.debug("GET /api/events/reservations — date={}, locationId={}, status={}", date, locationId, status);
        List<ReservationResponse> responses = eventService.getReservations(date, locationId, status);
        return ResponseEntity.ok(responses);
    }

    // ── Phase 7C Academic Event & Impact Endpoints ──────────────────────────────

    /**
     * POST /api/events/impact-analysis — Read-only impact analysis.
     */
    @PostMapping("/impact-analysis")
    public ResponseEntity<ImpactAnalysisResponse> generateImpactAnalysis(
            @Valid @RequestBody ImpactAnalysisRequest request) {
        log.info("POST /api/events/impact-analysis — eventId={}, timetableId={}",
                request.getEventId(), request.getTimetableId());
        ImpactAnalysisResponse response = eventService.generateImpactAnalysis(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/events/execution-plan — Read-only execution plan generation.
     */
    @PostMapping("/execution-plan")
    public ResponseEntity<ExecutionPlanResponse> generateExecutionPlan(
            @Valid @RequestBody ExecutionPlanRequest request) {
        log.info("POST /api/events/execution-plan — eventId={}, strategy={}",
                request.getEventId(), request.getExecutionStrategy());
        ExecutionPlanResponse response = eventService.generateExecutionPlan(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/events/{id}/execute — Execute chosen strategy via TIMETABLE-SERVICE orchestration.
     */
    @PostMapping("/{id}/execute")
    public ResponseEntity<ExecutionResponse> executeStrategy(
            @PathVariable Long id,
            @Valid @RequestBody ExecutionRequest request) {
        log.info("POST /api/events/{}/execute — strategy={}", id, request.getExecutionStrategy());
        ExecutionResponse response = eventService.executeStrategy(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/events/{id}/execution — Retrieve execution history metadata.
     */
    @GetMapping("/{id}/execution")
    public ResponseEntity<ExecutionHistoryResponse> getExecutionHistory(@PathVariable Long id) {
        log.debug("GET /api/events/{}/execution — Fetching execution history", id);
        ExecutionHistoryResponse response = eventService.getExecutionHistory(id);
        return ResponseEntity.ok(response);
    }
}
