package com.scheduleflow.event.dto;

import com.scheduleflow.event.enums.EventStatus;

/**
 * OccupiedPeriod — Represents a single time slot occupied by a room reservation.
 *
 * <p>Used inside {@link RoomAvailabilityInfo} to show a room's existing bookings
 * on a given date, helping the frontend render slot-wise availability without
 * making additional requests.
 */
public class OccupiedPeriod {

    private Long eventId;
    private String eventTitle;
    private Integer startPeriod;
    private Integer endPeriod;
    private EventStatus status;

    public OccupiedPeriod() {}

    public OccupiedPeriod(Long eventId, String eventTitle,
                          Integer startPeriod, Integer endPeriod, EventStatus status) {
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
        this.status = status;
    }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }

    public Integer getStartPeriod() { return startPeriod; }
    public void setStartPeriod(Integer startPeriod) { this.startPeriod = startPeriod; }

    public Integer getEndPeriod() { return endPeriod; }
    public void setEndPeriod(Integer endPeriod) { this.endPeriod = endPeriod; }

    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }
}
