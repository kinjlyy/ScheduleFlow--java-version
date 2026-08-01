package com.scheduleflow.event.dto;

import java.util.List;

/**
 * RoomAvailabilityInfo — Combines room metadata with its occupancy state for a given query slot.
 *
 * <p>Used by {@link AvailabilityResponse} for both available and reserved room lists.
 * {@code occupiedPeriods} contains ALL non-cancelled reservations for the room on the queried date —
 * not just the conflicting one — so the frontend can render the full day schedule.
 *
 * <p><strong>Phase 7C Extension Point:</strong>
 * When timetable integration is added, timetable-occupied slots can be merged into
 * {@code occupiedPeriods} using a separate source flag without changing this DTO's contract.
 */
public class RoomAvailabilityInfo {

    private RoomResponse room;

    /**
     * True if this room has no conflict with the requested date/startPeriod/endPeriod slot.
     * False if one or more existing reservations overlap with the requested slot.
     */
    private boolean availableInRequestedSlot;

    /**
     * All non-cancelled reservations for this room on the queried date.
     * Empty if the room has no bookings that day.
     */
    private List<OccupiedPeriod> occupiedPeriods;
    private boolean recommended;
    private String recommendationReason;

    public RoomAvailabilityInfo() {}

    public RoomAvailabilityInfo(RoomResponse room, boolean availableInRequestedSlot,
                                List<OccupiedPeriod> occupiedPeriods) {
        this(room, availableInRequestedSlot, occupiedPeriods, false, null);
    }

    public RoomAvailabilityInfo(RoomResponse room, boolean availableInRequestedSlot,
                                List<OccupiedPeriod> occupiedPeriods, boolean recommended,
                                String recommendationReason) {
        this.room = room;
        this.availableInRequestedSlot = availableInRequestedSlot;
        this.occupiedPeriods = occupiedPeriods;
        this.recommended = recommended;
        this.recommendationReason = recommendationReason;
    }

    public RoomResponse getRoom() { return room; }
    public void setRoom(RoomResponse room) { this.room = room; }

    public boolean isAvailableInRequestedSlot() { return availableInRequestedSlot; }
    public void setAvailableInRequestedSlot(boolean availableInRequestedSlot) {
        this.availableInRequestedSlot = availableInRequestedSlot;
    }

    public List<OccupiedPeriod> getOccupiedPeriods() { return occupiedPeriods; }
    public void setOccupiedPeriods(List<OccupiedPeriod> occupiedPeriods) {
        this.occupiedPeriods = occupiedPeriods;
    }

    public boolean isRecommended() { return recommended; }
    public void setRecommended(boolean recommended) { this.recommended = recommended; }

    public String getRecommendationReason() { return recommendationReason; }
    public void setRecommendationReason(String recommendationReason) { this.recommendationReason = recommendationReason; }
}
