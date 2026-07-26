package com.scheduleflow.dto;

import com.scheduleflow.model.RoomAllocationStrategy;

import java.util.List;
import java.util.Map;

public class TimetableRequestDTO {
    private List<SectionDTO> sections;
    private int daysPerWeek;
    private int periodsPerDay;
    private Map<String, Integer> teacherMaxLectures; // teacher -> max lectures/week
    private RoomAllocationStrategy roomAllocationStrategy = RoomAllocationStrategy.DYNAMIC_ALLOCATION;

    public TimetableRequestDTO() {}

    public List<SectionDTO> getSections() { return sections; }
    public void setSections(List<SectionDTO> sections) { this.sections = sections; }

    public int getDaysPerWeek() { return daysPerWeek; }
    public void setDaysPerWeek(int daysPerWeek) { this.daysPerWeek = daysPerWeek; }

    public int getPeriodsPerDay() { return periodsPerDay; }
    public void setPeriodsPerDay(int periodsPerDay) { this.periodsPerDay = periodsPerDay; }

    public Map<String, Integer> getTeacherMaxLectures() { return teacherMaxLectures; }
    public void setTeacherMaxLectures(Map<String, Integer> teacherMaxLectures) { this.teacherMaxLectures = teacherMaxLectures; }

    public RoomAllocationStrategy getRoomAllocationStrategy() { return roomAllocationStrategy; }
    public void setRoomAllocationStrategy(RoomAllocationStrategy roomAllocationStrategy) { this.roomAllocationStrategy = roomAllocationStrategy; }
}
