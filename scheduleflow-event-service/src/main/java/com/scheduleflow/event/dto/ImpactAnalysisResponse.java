package com.scheduleflow.event.dto;

import com.scheduleflow.event.enums.EventStatus;

public class ImpactAnalysisResponse {

    private Long eventId;
    private Long timetableId;
    private TimetableImpactResponse impact;
    private EventStatus status;

    public ImpactAnalysisResponse() {}

    public ImpactAnalysisResponse(Long eventId, Long timetableId, TimetableImpactResponse impact, EventStatus status) {
        this.eventId = eventId;
        this.timetableId = timetableId;
        this.impact = impact;
        this.status = status;
    }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public Long getTimetableId() { return timetableId; }
    public void setTimetableId(Long timetableId) { this.timetableId = timetableId; }

    public TimetableImpactResponse getImpact() { return impact; }
    public void setImpact(TimetableImpactResponse impact) { this.impact = impact; }

    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }
}
