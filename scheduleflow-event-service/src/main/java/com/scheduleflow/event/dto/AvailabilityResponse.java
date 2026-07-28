package com.scheduleflow.event.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * AvailabilityResponse — Result of a room availability query for a given date and period range.
 *
 * <p>Returns all active rooms partitioned into two groups:
 * <ul>
 *   <li>{@code availableRooms} — No reservation conflict in the requested slot.</li>
 *   <li>{@code reservedRooms} — At least one active reservation overlaps the requested slot.</li>
 * </ul>
 *
 * <p>Each {@link RoomAvailabilityInfo} includes {@code occupiedPeriods} for the full day,
 * enabling the frontend to render slot-wise availability without additional requests.
 *
 * <p><strong>Phase 7C Extension Point:</strong>
 * A future {@code timetableOccupied} list can be added to represent rooms blocked by
 * timetable entries from TIMETABLE-SERVICE, without changing the existing API contract
 * or the structure of {@code availableRooms} and {@code reservedRooms}.
 */
public class AvailabilityResponse {

    private LocalDate date;
    private Integer requestedStartPeriod;
    private Integer requestedEndPeriod;
    private List<RoomAvailabilityInfo> availableRooms;
    private List<RoomAvailabilityInfo> reservedRooms;

    // ── Phase 7C Extension Point ──────────────────────────────────────────────
    // Uncomment and populate when timetable integration is implemented:
    // private List<RoomAvailabilityInfo> timetableOccupied;

    public AvailabilityResponse() {}

    public AvailabilityResponse(LocalDate date, Integer requestedStartPeriod, Integer requestedEndPeriod,
                                List<RoomAvailabilityInfo> availableRooms,
                                List<RoomAvailabilityInfo> reservedRooms) {
        this.date = date;
        this.requestedStartPeriod = requestedStartPeriod;
        this.requestedEndPeriod = requestedEndPeriod;
        this.availableRooms = availableRooms;
        this.reservedRooms = reservedRooms;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Integer getRequestedStartPeriod() { return requestedStartPeriod; }
    public void setRequestedStartPeriod(Integer requestedStartPeriod) {
        this.requestedStartPeriod = requestedStartPeriod;
    }

    public Integer getRequestedEndPeriod() { return requestedEndPeriod; }
    public void setRequestedEndPeriod(Integer requestedEndPeriod) {
        this.requestedEndPeriod = requestedEndPeriod;
    }

    public List<RoomAvailabilityInfo> getAvailableRooms() { return availableRooms; }
    public void setAvailableRooms(List<RoomAvailabilityInfo> availableRooms) {
        this.availableRooms = availableRooms;
    }

    public List<RoomAvailabilityInfo> getReservedRooms() { return reservedRooms; }
    public void setReservedRooms(List<RoomAvailabilityInfo> reservedRooms) {
        this.reservedRooms = reservedRooms;
    }
}
