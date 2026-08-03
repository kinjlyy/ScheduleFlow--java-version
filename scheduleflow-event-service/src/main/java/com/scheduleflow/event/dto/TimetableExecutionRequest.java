package com.scheduleflow.event.dto;

import java.time.LocalDate;
import java.util.List;

public class TimetableExecutionRequest {

    private Long eventId;
    private String eventTitle;
    private String executionStrategy;
    private LocalDate date;
    private Integer startPeriod;
    private Integer endPeriod;
    private Long locationId;
    private String roomNumber;
    private List<Long> affectedLectureIds;
    private String executedBy;

    public TimetableExecutionRequest() {}

    public TimetableExecutionRequest(Long eventId, String eventTitle, String executionStrategy,
                                     LocalDate date, Integer startPeriod, Integer endPeriod,
                                     List<Long> affectedLectureIds, String executedBy) {
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.executionStrategy = executionStrategy;
        this.date = date;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
        this.affectedLectureIds = affectedLectureIds;
        this.executedBy = executedBy;
    }

    public TimetableExecutionRequest(Long eventId, String eventTitle, String executionStrategy,
                                     LocalDate date, Integer startPeriod, Integer endPeriod,
                                     Long locationId, String roomNumber,
                                     List<Long> affectedLectureIds, String executedBy) {
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.executionStrategy = executionStrategy;
        this.date = date;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
        this.locationId = locationId;
        this.roomNumber = roomNumber;
        this.affectedLectureIds = affectedLectureIds;
        this.executedBy = executedBy;
    }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }

    public String getExecutionStrategy() { return executionStrategy; }
    public void setExecutionStrategy(String executionStrategy) { this.executionStrategy = executionStrategy; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Integer getStartPeriod() { return startPeriod; }
    public void setStartPeriod(Integer startPeriod) { this.startPeriod = startPeriod; }

    public Integer getEndPeriod() { return endPeriod; }
    public void setEndPeriod(Integer endPeriod) { this.endPeriod = endPeriod; }

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public List<Long> getAffectedLectureIds() { return affectedLectureIds; }
    public void setAffectedLectureIds(List<Long> affectedLectureIds) { this.affectedLectureIds = affectedLectureIds; }

    public String getExecutedBy() { return executedBy; }
    public void setExecutedBy(String executedBy) { this.executedBy = executedBy; }
}
