package com.scheduleflow.event.service.impl;

import com.scheduleflow.event.client.ResourceServiceClient;
import com.scheduleflow.event.client.TimetableServiceClient;
import com.scheduleflow.event.dto.*;
import com.scheduleflow.event.entity.Event;
import com.scheduleflow.event.enums.EventCategory;
import com.scheduleflow.event.enums.EventStatus;
import com.scheduleflow.event.enums.ExecutionStrategy;
import com.scheduleflow.event.enums.LocationType;
import com.scheduleflow.event.exception.ReservationConflictException;
import com.scheduleflow.event.exception.ResourceNotFoundException;
import com.scheduleflow.event.exception.ValidationException;
import com.scheduleflow.event.mapper.EventMapper;
import com.scheduleflow.event.repository.EventRepository;
import com.scheduleflow.event.service.EventService;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * EventServiceImpl — Complete implementation of {@link EventService} across Phases 7A, 7B, and 7C.
 *
 * <p>Phase 7C Orchestration:
 * Coordinates academic event scheduling workflows with TIMETABLE-SERVICE via {@link TimetableServiceClient}.
 * Follows the strict lifecycle: DRAFT → IMPACT_ANALYZED → READY_FOR_EXECUTION → EXECUTING → COMPLETED / FAILED.
 */
@Service
@Transactional
public class EventServiceImpl implements EventService {

    private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final ResourceServiceClient resourceServiceClient;
    private final TimetableServiceClient timetableServiceClient;

    public EventServiceImpl(EventRepository eventRepository,
                            EventMapper eventMapper,
                            ResourceServiceClient resourceServiceClient,
                            TimetableServiceClient timetableServiceClient) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
        this.resourceServiceClient = resourceServiceClient;
        this.timetableServiceClient = timetableServiceClient;
    }

    // ── Phase 7A Event Operations ──────────────────────────────────────────────

    @Override
    public EventResponse createEvent(CreateEventRequest request) {
        log.info("Creating event: title={}, category={}, syncWithTimetable={}",
                request.getTitle(), request.getEventCategory(), request.getSyncWithTimetable());

        validatePeriods(request.getStartPeriod(), request.getEndPeriod());

        Event event = eventMapper.toEntity(request);

        if (event.getTimetableId() == null) {
            try {
                Map<String, Object> activeTt = timetableServiceClient.getActiveTimetable();
                if (activeTt != null && activeTt.get("id") != null) {
                    event.setTimetableId(Long.valueOf(activeTt.get("id").toString()));
                }
            } catch (Exception ex) {
                log.warn("Could not auto-resolve active timetable ID for event creation: {}", ex.getMessage());
            }
        }

        Event saved = eventRepository.save(event);

        if (Boolean.TRUE.equals(request.getSyncWithTimetable())) {
            log.info("syncWithTimetable is TRUE for event id={}. Triggering timetable synchronization pipeline...", saved.getId());
            try {
                ExecutionStrategy strategy = request.getExecutionStrategy() != null
                        ? request.getExecutionStrategy()
                        : ExecutionStrategy.RESCHEDULE_AND_CANCEL;
                String createdBy = request.getCreatedBy() != null ? request.getCreatedBy() : "Admin";
                executeStrategy(saved.getId(), new ExecutionRequest(strategy, createdBy));
                saved = eventRepository.findById(saved.getId()).orElse(saved);
            } catch (Exception ex) {
                log.error("Failed to execute timetable synchronization for event id={}: {}", saved.getId(), ex.getMessage(), ex);
            }
        }

        log.info("Event created successfully: id={}", saved.getId());
        return eventMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id) {
        log.debug("Fetching event by id={}", id);
        Event event = findEventOrThrow(id);
        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventSummaryResponse> getAllEvents() {
        log.debug("Fetching all events");
        return eventRepository.findAll()
                .stream()
                .map(eventMapper::toSummary)
                .toList();
    }

    @Override
    public EventResponse updateEvent(Long id, UpdateEventRequest request) {
        log.info("Updating event id={}", id);

        Event event = findEventOrThrow(id);

        if (request.getStartPeriod() != null || request.getEndPeriod() != null) {
            int startPeriod = request.getStartPeriod() != null ? request.getStartPeriod() : event.getStartPeriod();
            int endPeriod = request.getEndPeriod() != null ? request.getEndPeriod() : event.getEndPeriod();
            validatePeriods(startPeriod, endPeriod);
        }

        eventMapper.applyUpdate(request, event);
        Event saved = eventRepository.save(event);

        log.info("Event updated successfully: id={}", saved.getId());
        return eventMapper.toResponse(saved);
    }

    @Override
    public void deleteEvent(Long id) {
        log.info("Deleting event id={}", id);
        findEventOrThrow(id);
        eventRepository.deleteById(id);
        log.info("Event deleted: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventSummaryResponse> getEventsByDate(LocalDate date) {
        log.debug("Fetching events for date={}", date);
        return eventRepository.findByDate(date)
                .stream()
                .map(eventMapper::toSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventSummaryResponse> getEventsByStatus(EventStatus status) {
        log.debug("Fetching events with status={}", status);
        return eventRepository.findByStatus(status)
                .stream()
                .map(eventMapper::toSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventSummaryResponse> getEventsByCategory(EventCategory category) {
        log.debug("Fetching events with category={}", category);
        return eventRepository.findByEventCategory(category)
                .stream()
                .map(eventMapper::toSummary)
                .toList();
    }

    // ── Phase 7B Room Reservation Operations ───────────────────────────────────

    @Override
    @Transactional
    public ReservationResponse reserveRoom(CreateReservationRequest request) {
        log.info("Initiating room reservation: title={}, locationId={}, date={}, periods={}-{}",
                request.getTitle(), request.getLocationId(), request.getDate(),
                request.getStartPeriod(), request.getEndPeriod());

        if (request.getLocationId() == null) {
            throw new ValidationException("locationId is required for room reservations");
        }
        if (request.getDate() == null) {
            throw new ValidationException("Date is required for room reservations");
        }

        RoomResponse room = fetchRoomOrThrow(request.getLocationId());

        if (!room.isActive()) {
            throw new ValidationException("Room '" + room.getRoomNumber() + "' (ID: "
                    + room.getId() + ") is inactive and cannot be reserved");
        }

        validatePeriods(request.getStartPeriod(), request.getEndPeriod());

        boolean conflict = eventRepository.existsConflictingReservation(
                request.getLocationId(),
                request.getDate(),
                request.getStartPeriod(),
                request.getEndPeriod(),
                EventCategory.ROOM_RESERVATION,
                EventStatus.CANCELLED,
                null
        );

        if (conflict) {
            log.warn("Reservation Conflict: locationId={}, room={}, date={}, periods={}-{}",
                    request.getLocationId(), room.getRoomNumber(), request.getDate(),
                    request.getStartPeriod(), request.getEndPeriod());
            throw new ReservationConflictException("Room '" + room.getRoomNumber()
                    + "' is already reserved during periods " + request.getStartPeriod()
                    + "-" + request.getEndPeriod() + " on " + request.getDate());
        }

        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEventType(request.getEventType());
        event.setEventCategory(EventCategory.ROOM_RESERVATION);
        event.setExecutionStrategy(null);
        event.setDate(request.getDate());
        event.setStartPeriod(request.getStartPeriod());
        event.setEndPeriod(request.getEndPeriod());
        event.setLocationId(request.getLocationId());
        event.setLocationType(request.getLocationType() != null ? request.getLocationType() : LocationType.CLASSROOM);
        event.setStatus(EventStatus.SCHEDULED);
        event.setOrganizer(request.getOrganizer());
        event.setCreatedBy(request.getCreatedBy());

        Event saved = eventRepository.save(event);

        log.info("Reservation Created: id={}, room={}, date={}, periods={}-{}, organizer={}",
                saved.getId(), room.getRoomNumber(), saved.getDate(),
                saved.getStartPeriod(), saved.getEndPeriod(), saved.getOrganizer());

        EventResponse eventResponse = eventMapper.toResponse(saved);
        String message = "Room " + room.getRoomNumber() + " successfully reserved for '" + saved.getTitle() + "'";
        return new ReservationResponse(eventResponse, room, message);
    }

    @Override
    @Transactional
    public void cancelReservation(Long id) {
        log.info("Initiating reservation cancellation for event id={}", id);

        Event event = findEventOrThrow(id);

        if (event.getEventCategory() != EventCategory.ROOM_RESERVATION) {
            throw new ValidationException("Event ID " + id + " is not a room reservation");
        }

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new ValidationException("Reservation ID " + id + " is already cancelled");
        }

        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);

        log.info("Reservation Cancelled: id={}, title={}, locationId={}",
                event.getId(), event.getTitle(), event.getLocationId());
    }

    @Override
    @Transactional(readOnly = true)
    public AvailabilityResponse checkAvailability(LocalDate date, Integer startPeriod, Integer endPeriod, Long timetableId) {
        log.info("▶ checkAvailability INCOMING REQUEST: date={}, startPeriod={}, endPeriod={}, timetableId={}",
                date, startPeriod, endPeriod, timetableId);

        if (date == null) {
            throw new ValidationException("Date parameter is required for availability check");
        }
        validatePeriods(startPeriod, endPeriod);

        String dayOfWeek = date.getDayOfWeek().name(); // e.g. MONDAY
        int startSlot = startPeriod - 1; // Period 1 → Slot 0
        int endSlot = endPeriod - 1;     // Period 2 → Slot 1

        log.info("▶ Derived values: dayOfWeek={}, startSlot={}, endSlot={} (Period range {}-{})",
                dayOfWeek, startSlot, endSlot, startPeriod, endPeriod);

        List<RoomResponse> activeRooms = null;
        try {
            activeRooms = resourceServiceClient.getActiveRooms();
        } catch (Exception ex) {
            log.error("Failed to fetch active rooms from Resource Service: {}", ex.getMessage(), ex);
            activeRooms = List.of();
        }
        if (activeRooms == null) activeRooms = List.of();

        List<Long> allRoomIds = activeRooms.stream().map(RoomResponse::getId).toList();
        log.info("▶ Active rooms count={}, allRoomIds={}", activeRooms.size(), allRoomIds);

        // Source 1: Event Service Scheduled Events & Room Reservations on this Date
        List<Event> dailyEvents = eventRepository.findByDateAndStatusNot(date, EventStatus.CANCELLED);
        Map<Long, List<Event>> eventsByRoomId = dailyEvents.stream()
                .filter(e -> e.getLocationId() != null)
                .collect(Collectors.groupingBy(Event::getLocationId));

        // Source 2: Timetable Service Occupied Rooms derived directly from Lectures
        java.util.Set<Long> timetableOccupiedRoomIds = new java.util.HashSet<>();
        Long resolvedTimetableId = timetableId;

        try {
            if (resolvedTimetableId == null) {
                Map<String, Object> activeTt = timetableServiceClient.getActiveTimetable();
                log.info("▶ Active timetable response from Timetable Service: {}", activeTt);
                if (activeTt != null && activeTt.get("id") != null) {
                    resolvedTimetableId = Long.valueOf(activeTt.get("id").toString());
                }
            }

            if (resolvedTimetableId != null) {
                log.info("▶ Querying Timetable Service getOccupiedRoomIds: timetableId={}, day={}, startPeriod={}, endPeriod={}",
                        resolvedTimetableId, dayOfWeek, startPeriod, endPeriod);
                List<Long> occupiedIds = timetableServiceClient.getOccupiedRoomIds(resolvedTimetableId, dayOfWeek, startPeriod, endPeriod);
                if (occupiedIds != null) {
                    timetableOccupiedRoomIds.addAll(occupiedIds);
                }
            } else {
                log.warn("⚠️ No active timetable found to query occupied room IDs!");
            }
        } catch (Exception ex) {
            log.error("❌ Exception querying occupied room IDs from Timetable Service for timetableId={}: {}",
                    resolvedTimetableId, ex.getMessage(), ex);
        }

        log.info("▶ Occupied room IDs from Timetable Service: {}", timetableOccupiedRoomIds);

        List<RoomAvailabilityInfo> availableRooms = new ArrayList<>();
        List<RoomAvailabilityInfo> reservedRooms = new ArrayList<>();

        for (RoomResponse room : activeRooms) {
            List<Event> roomBookings = eventsByRoomId.getOrDefault(room.getId(), List.of());

            List<OccupiedPeriod> occupiedPeriods = new ArrayList<>(roomBookings.stream()
                    .map(b -> new OccupiedPeriod(b.getId(), b.getTitle(), b.getStartPeriod(), b.getEndPeriod(), b.getStatus()))
                    .toList());

            boolean hasEventConflict = roomBookings.stream().anyMatch(b ->
                    b.getStartPeriod() <= endPeriod && b.getEndPeriod() >= startPeriod
            );

            boolean hasTimetableConflict = timetableOccupiedRoomIds.contains(room.getId());

            boolean isReserved = hasEventConflict || hasTimetableConflict;

            if (hasTimetableConflict) {
                occupiedPeriods.add(new OccupiedPeriod(
                        0L,
                        "Timetable Lecture Scheduled",
                        startPeriod,
                        endPeriod,
                        EventStatus.SCHEDULED
                ));
            }

            RoomAvailabilityInfo info = new RoomAvailabilityInfo(room, !isReserved, occupiedPeriods);

            if (isReserved) {
                reservedRooms.add(info);
            } else {
                availableRooms.add(info);
            }
        }

        // Intelligent Room Ranking for Available Rooms
        availableRooms.sort((a, b) -> {
            int scoreA = calculateRoomSuitabilityScore(a.getRoom());
            int scoreB = calculateRoomSuitabilityScore(b.getRoom());
            return Integer.compare(scoreB, scoreA); // descending score
        });

        if (!availableRooms.isEmpty()) {
            RoomAvailabilityInfo top = availableRooms.get(0);
            top.setRecommended(true);
            top.setRecommendationReason("Best Capacity & Type Match (" + top.getRoom().getRoomType() + ", Cap: " + top.getRoom().getMaximumCapacity() + ")");
        }

        List<Long> availableRoomIds = availableRooms.stream().map(r -> r.getRoom().getId()).toList();
        List<Long> reservedRoomIds = reservedRooms.stream().map(r -> r.getRoom().getId()).toList();

        log.info("✔ checkAvailability COMPLETED: totalAvailable={}, totalReserved={}, availableRoomIds={}, reservedRoomIds={}",
                availableRooms.size(), reservedRooms.size(), availableRoomIds, reservedRoomIds);

        return new AvailabilityResponse(date, startPeriod, endPeriod, availableRooms, reservedRooms);
    }

    private int calculateRoomSuitabilityScore(RoomResponse room) {
        int score = 0;
        if (room == null) return score;
        if ("CLASSROOM".equalsIgnoreCase(room.getRoomType())) score += 50;
        else if ("SEMINAR_HALL".equalsIgnoreCase(room.getRoomType())) score += 40;
        else if ("AUDITORIUM".equalsIgnoreCase(room.getRoomType())) score += 30;

        int cap = room.getMaximumCapacity();
        if (cap >= 40 && cap <= 100) score += 30;
        else if (cap > 100) score += 15;

        return score;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservations(LocalDate date, Long locationId, EventStatus status) {
        log.debug("Fetching reservations: date={}, locationId={}, status={}", date, locationId, status);

        List<Event> reservations = eventRepository.findByEventCategory(EventCategory.ROOM_RESERVATION);

        List<Event> filtered = reservations.stream()
                .filter(e -> date == null || date.equals(e.getDate()))
                .filter(e -> locationId == null || locationId.equals(e.getLocationId()))
                .filter(e -> status == null || status == e.getStatus())
                .toList();

        if (filtered.isEmpty()) {
            return List.of();
        }

        // Batch active room lookup to avoid N+1 Feign calls
        Map<Long, RoomResponse> roomMap = new HashMap<>();
        try {
            List<RoomResponse> activeRooms = resourceServiceClient.getActiveRooms();
            if (activeRooms != null) {
                activeRooms.forEach(r -> roomMap.put(r.getId(), r));
            }
        } catch (Exception ex) {
            log.warn("Failed to batch fetch active rooms: {}", ex.getMessage());
        }

        return filtered.stream()
                .map(e -> {
                    RoomResponse room = e.getLocationId() != null ? roomMap.get(e.getLocationId()) : null;
                    if (room == null && e.getLocationId() != null) {
                        try {
                            room = resourceServiceClient.getRoomById(e.getLocationId());
                        } catch (Exception ex) {
                            log.warn("Failed to fetch room metadata for locationId={}", e.getLocationId());
                        }
                    }
                    return new ReservationResponse(
                            eventMapper.toResponse(e),
                            room,
                            "Reservation details retrieved"
                    );
                })
                .toList();
    }

    // ── Phase 7C Academic Event Scheduling & Impact Operations ────────────────

    @Override
    @Transactional
    public ImpactAnalysisResponse generateImpactAnalysis(ImpactAnalysisRequest request) {
        log.info("Impact Analysis Started: eventId={}, timetableId={}, date={}, periods={}-{}",
                request.getEventId(), request.getTimetableId(), request.getDate(),
                request.getStartPeriod(), request.getEndPeriod());

        Event event = null;
        Long timetableId = request.getTimetableId();
        LocalDate date = request.getDate();
        Integer startPeriod = request.getStartPeriod();
        Integer endPeriod = request.getEndPeriod();
        Long locationId = request.getLocationId();

        if (request.getEventId() != null) {
            event = findEventOrThrow(request.getEventId());
            if (timetableId == null) timetableId = event.getTimetableId();
            if (date == null) date = event.getDate();
            if (startPeriod == null) startPeriod = event.getStartPeriod();
            if (endPeriod == null) endPeriod = event.getEndPeriod();
            if (locationId == null) locationId = event.getLocationId();
        }

        if (timetableId == null) {
            Map<String, Object> activeTt = timetableServiceClient.getActiveTimetable();
            if (activeTt != null && activeTt.containsKey("id")) {
                timetableId = Long.valueOf(activeTt.get("id").toString());
            } else {
                throw new ValidationException("timetableId is required for impact analysis");
            }
        }

        validatePeriods(startPeriod, endPeriod);

        // Feign call to TIMETABLE-SERVICE for read-only impact calculation
        TimetableImpactResponse impact = timetableServiceClient.getImpactedLectures(
                timetableId, date, startPeriod, endPeriod, locationId);

        EventStatus newStatus = EventStatus.IMPACT_ANALYZED;
        if (event != null) {
            event.setTimetableId(timetableId);
            event.setStatus(newStatus);
            eventRepository.save(event);
        }

        log.info("Impact Analysis Completed: eventId={}, timetableId={}, totalAffected={}",
                request.getEventId(), timetableId, impact != null ? impact.getTotalAffectedLectures() : 0);

        return new ImpactAnalysisResponse(
                event != null ? event.getId() : null,
                timetableId,
                impact,
                newStatus
        );
    }

    @Override
    @Transactional
    public ExecutionPlanResponse generateExecutionPlan(ExecutionPlanRequest request) {
        log.info("Execution Plan Generated: eventId={}, strategy={}",
                request.getEventId(), request.getExecutionStrategy());

        Event event = findEventOrThrow(request.getEventId());

        ImpactAnalysisRequest impactReq = new ImpactAnalysisRequest();
        impactReq.setEventId(event.getId());
        ImpactAnalysisResponse impactAnalysis = generateImpactAnalysis(impactReq);

        TimetableImpactResponse impact = impactAnalysis.getImpact();
        List<ImpactedLectureResponse> allAffected = impact != null && impact.getAffectedLectures() != null
                ? impact.getAffectedLectures() : List.of();

        List<ImpactedLectureResponse> lecturesToReschedule = new ArrayList<>();
        List<ImpactedLectureResponse> lecturesToCancel = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        ExecutionStrategy strategy = request.getExecutionStrategy();

        if (strategy == ExecutionStrategy.CANCEL_ALL) {
            lecturesToCancel.addAll(allAffected);
            warnings.add("All " + allAffected.size() + " affected lectures will be cancelled without rescheduling.");
        } else if (strategy == ExecutionStrategy.RESCHEDULE_AND_CANCEL) {
            for (ImpactedLectureResponse l : allAffected) {
                if (l.isReschedulable()) {
                    lecturesToReschedule.add(l);
                } else {
                    lecturesToCancel.add(l);
                    warnings.add("Lecture " + l.getSubject() + " (Section " + l.getSection() + ") is non-reschedulable (e.g. Lab) and will be cancelled.");
                }
            }
        }

        String summary = String.format("Execution Plan for strategy %s: %d to reschedule, %d to cancel.",
                strategy, lecturesToReschedule.size(), lecturesToCancel.size());

        event.setExecutionStrategy(strategy);
        event.setStatus(EventStatus.READY_FOR_EXECUTION);
        eventRepository.save(event);

        return new ExecutionPlanResponse(
                event.getId(),
                strategy,
                summary,
                impactAnalysis,
                lecturesToReschedule,
                lecturesToCancel,
                warnings,
                EventStatus.READY_FOR_EXECUTION
        );
    }

    @Override
    @Transactional
    public ExecutionResponse executeStrategy(Long id, ExecutionRequest request) {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("Execution Started: eventId={}, strategy={}, executedBy={}", id, request.getExecutionStrategy(), request.getExecutedBy());

        Event event = findEventOrThrow(id);

        ExecutionStrategy strategy = request.getExecutionStrategy() != null ? request.getExecutionStrategy() : event.getExecutionStrategy();
        if (strategy == null) {
            strategy = ExecutionStrategy.CANCEL_ALL;
        }

        event.setExecutionStrategy(strategy);
        event.setExecutedBy(request.getExecutedBy());
        event.setExecutionStartedAt(startTime);
        event.setStatus(EventStatus.EXECUTING);
        eventRepository.save(event);

        // Fetch affected lectures to send to Timetable Service
        ImpactAnalysisRequest impactReq = new ImpactAnalysisRequest();
        impactReq.setEventId(event.getId());
        ImpactAnalysisResponse impactAnalysis = generateImpactAnalysis(impactReq);
        TimetableImpactResponse impact = impactAnalysis.getImpact();

        if (event.getTimetableId() == null && impactAnalysis != null && impactAnalysis.getTimetableId() != null) {
            event.setTimetableId(impactAnalysis.getTimetableId());
            eventRepository.save(event);
        }

        List<Long> affectedIds = impact != null && impact.getAffectedLectures() != null
                ? impact.getAffectedLectures().stream().map(ImpactedLectureResponse::getId).toList()
                : List.of();

        String roomNumber = null;
        if (event.getLocationId() != null) {
            try {
                RoomResponse room = resourceServiceClient.getRoomById(event.getLocationId());
                if (room != null) roomNumber = room.getRoomNumber();
            } catch (Exception ex) {
                log.warn("Could not fetch room metadata for locationId={}", event.getLocationId());
            }
        }

        TimetableExecutionRequest ttRequest = new TimetableExecutionRequest(
                event.getId(),
                event.getTitle(),
                strategy.name(),
                event.getDate(),
                event.getStartPeriod(),
                event.getEndPeriod(),
                event.getLocationId(),
                roomNumber,
                affectedIds,
                request.getExecutedBy()
        );

        TimetableExecutionResultResponse ttResult = null;
        LocalDateTime endTime;
        long durationMs;

        try {
            // Single orchestration call to TIMETABLE-SERVICE
            ttResult = timetableServiceClient.executeEventImpact(event.getTimetableId(), ttRequest);

            endTime = LocalDateTime.now();
            durationMs = Duration.between(startTime, endTime).toMillis();

            event.setStatus(EventStatus.COMPLETED);
            event.setExecutionCompletedAt(endTime);
            event.setExecutionSummary(ttResult != null ? ttResult.getSummary() : "Execution completed successfully");
            event.setExecutionResult("Rescheduled: " + (ttResult != null ? ttResult.getRescheduledCount() : 0)
                    + ", Cancelled: " + (ttResult != null ? ttResult.getCancelledCount() : 0));
            eventRepository.save(event);

            log.info("Execution Completed: eventId={}, strategy={}, timetableId={}, durationMs={}",
                    event.getId(), strategy, event.getTimetableId(), durationMs);

            return new ExecutionResponse(
                    event.getId(),
                    strategy,
                    request.getExecutedBy(),
                    endTime,
                    durationMs,
                    EventStatus.COMPLETED,
                    event.getExecutionSummary(),
                    ttResult != null ? ttResult.getRescheduledCount() : 0,
                    ttResult != null ? ttResult.getCancelledCount() : 0,
                    ttResult != null ? ttResult.getWarnings() : List.of()
            );

        } catch (Exception ex) {
            endTime = LocalDateTime.now();
            durationMs = Duration.between(startTime, endTime).toMillis();

            event.setStatus(EventStatus.FAILED);
            event.setExecutionCompletedAt(endTime);
            event.setExecutionSummary("Execution failed: " + ex.getMessage());
            event.setExecutionResult("FAILED");
            eventRepository.save(event);

            log.error("Execution Failed: eventId={}, strategy={}, timetableId={}, durationMs={}, error={}",
                    event.getId(), strategy, event.getTimetableId(), durationMs, ex.getMessage(), ex);

            return new ExecutionResponse(
                    event.getId(),
                    strategy,
                    request.getExecutedBy(),
                    endTime,
                    durationMs,
                    EventStatus.FAILED,
                    "Execution failed: " + ex.getMessage(),
                    0,
                    0,
                    List.of("Timetable execution failed: " + ex.getMessage())
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ExecutionHistoryResponse getExecutionHistory(Long id) {
        log.debug("Fetching execution history for eventId={}", id);
        Event event = findEventOrThrow(id);

        return new ExecutionHistoryResponse(
                event.getId(),
                event.getStatus(),
                event.getExecutionStrategy(),
                event.getExecutedBy(),
                event.getExecutionStartedAt(),
                event.getExecutionCompletedAt(),
                event.getExecutionSummary(),
                event.getExecutionResult()
        );
    }

    // ── Private Helpers ────────────────────────────────────────────────────────

    private Event findEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
    }

    private RoomResponse fetchRoomOrThrow(Long roomId) {
        try {
            RoomResponse room = resourceServiceClient.getRoomById(roomId);
            if (room == null) {
                throw new ResourceNotFoundException("Room", roomId);
            }
            return room;
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Room", roomId);
        }
    }

    private void validatePeriods(Integer startPeriod, Integer endPeriod) {
        if (startPeriod == null || startPeriod < 1) {
            throw new ValidationException("Start period must be at least 1");
        }
        if (endPeriod == null || endPeriod < 1) {
            throw new ValidationException("End period must be at least 1");
        }
        if (endPeriod < startPeriod) {
            throw new ValidationException(
                    "End period (" + endPeriod + ") must be greater than or equal to start period (" + startPeriod + ")"
            );
        }
    }
}
