package com.scheduleflow.event.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ImpactedLectureResponse {

    private Long id;
    private String subject;
    private String teacher;
    private String section;
    private String day;
    private int period;
    private String roomNumber;
    private boolean reschedulable;

    public ImpactedLectureResponse() {}

    public ImpactedLectureResponse(Long id, String subject, String teacher, String section,
                                   String day, int period, String roomNumber, boolean reschedulable) {
        this.id = id;
        this.subject = subject;
        this.teacher = teacher;
        this.section = section;
        this.day = day;
        this.period = period;
        this.roomNumber = roomNumber;
        this.reschedulable = reschedulable;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getTeacher() { return teacher; }
    public void setTeacher(String teacher) { this.teacher = teacher; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }

    public int getPeriod() { return period; }
    public void setPeriod(int period) { this.period = period; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public boolean isReschedulable() { return reschedulable; }
    public void setReschedulable(boolean reschedulable) { this.reschedulable = reschedulable; }
}
