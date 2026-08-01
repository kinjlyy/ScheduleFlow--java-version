package com.scheduleflow.event.repository;

import com.scheduleflow.event.entity.Event;
import com.scheduleflow.event.enums.EventCategory;
import com.scheduleflow.event.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * EventRepository — Spring Data JPA repository for the {@link Event} entity.
 *
 * <p>Phase 7B Extensions:
 * Includes parameterized conflict detection query binding enums explicitly as method parameters
 * rather than hardcoded JPQL string literals.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Find all events on a specific date.
     */
    List<Event> findByDate(LocalDate date);

    /**
     * Find all events with a given status.
     */
    List<Event> findByStatus(EventStatus status);

    /**
     * Find all events in a given category (ROOM_RESERVATION or TIMETABLE_EVENT).
     */
    List<Event> findByEventCategory(EventCategory eventCategory);

    /**
     * Find all events associated with a specific timetable.
     */
    List<Event> findByTimetableId(Long timetableId);

    /**
     * Find all events within a date range.
     */
    List<Event> findByDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find all events at a specific location.
     */
    List<Event> findByLocationId(Long locationId);

    /**
     * Check if an event exists with the given status.
     */
    boolean existsByIdAndStatus(Long id, EventStatus status);

    /**
     * Find all events on a date for a specific location.
     */
    List<Event> findByDateAndLocationId(LocalDate date, Long locationId);

    // ── Phase 7B Room Reservation Queries ──────────────────────────────────────

    /**
     * Check for overlapping active reservations using explicit enum parameter binding.
     * Overlap formula: e.startPeriod <= request.endPeriod AND e.endPeriod >= request.startPeriod.
     *
     * @param locationId      target room location ID
     * @param date            target reservation date
     * @param startPeriod     requested start period
     * @param endPeriod       requested end period
     * @param category        bound enum parameter (EventCategory.ROOM_RESERVATION)
     * @param cancelledStatus bound enum parameter (EventStatus.CANCELLED)
     * @param excludeId       optional event ID to exclude when checking updates (null for creation)
     * @return true if an overlapping non-cancelled reservation exists
     */
    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.locationId = :locationId AND e.date = :date " +
           "AND e.eventCategory = :category AND e.status <> :cancelledStatus " +
           "AND e.startPeriod <= :endPeriod AND e.endPeriod >= :startPeriod " +
           "AND (:excludeId IS NULL OR e.id <> :excludeId)")
    boolean existsConflictingReservation(
            @Param("locationId") Long locationId,
            @Param("date") LocalDate date,
            @Param("startPeriod") Integer startPeriod,
            @Param("endPeriod") Integer endPeriod,
            @Param("category") EventCategory category,
            @Param("cancelledStatus") EventStatus cancelledStatus,
            @Param("excludeId") Long excludeId
    );

    /**
     * Find all non-cancelled reservations for a location on a specific date.
     * Used for full-day slot occupancy mapping in availability calculation.
     */
    List<Event> findByDateAndLocationIdAndEventCategoryAndStatusNot(
            LocalDate date, Long locationId, EventCategory category, EventStatus cancelledStatus);

    /**
     * Find all non-cancelled reservations on a specific date across all rooms.
     */
    List<Event> findByDateAndEventCategoryAndStatusNot(
            LocalDate date, EventCategory category, EventStatus cancelledStatus);

    /**
     * Find reservations matching category with optional status filter.
     */
    List<Event> findByEventCategoryAndStatusNot(EventCategory category, EventStatus status);

    /**
     * Find all events on a specific date with status not equal to specified status.
     */
    List<Event> findByDateAndStatusNot(LocalDate date, EventStatus status);
}
