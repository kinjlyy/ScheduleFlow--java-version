package com.scheduleflow.event.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TimetableImpactResponse {

    private Long timetableId;
    private List<ImpactedLectureResponse> affectedLectures;
    private List<String> affectedTeachers;
    private List<String> affectedSections;
    private List<String> affectedRooms;
    private List<String> affectedSubjects;
    private int totalAffectedLectures;
    private int reschedulableLectures;
    private int nonReschedulableLectures;
    private List<String> conflicts;
    private String summary;

    public TimetableImpactResponse() {}

    public Long getTimetableId() { return timetableId; }
    public void setTimetableId(Long timetableId) { this.timetableId = timetableId; }

    public List<ImpactedLectureResponse> getAffectedLectures() { return affectedLectures; }
    public void setAffectedLectures(List<ImpactedLectureResponse> affectedLectures) { this.affectedLectures = affectedLectures; }

    public List<String> getAffectedTeachers() { return affectedTeachers; }
    public void setAffectedTeachers(List<String> affectedTeachers) { this.affectedTeachers = affectedTeachers; }

    public List<String> getAffectedSections() { return affectedSections; }
    public void setAffectedSections(List<String> affectedSections) { this.affectedSections = affectedSections; }

    public List<String> getAffectedRooms() { return affectedRooms; }
    public void setAffectedRooms(List<String> affectedRooms) { this.affectedRooms = affectedRooms; }

    public List<String> getAffectedSubjects() { return affectedSubjects; }
    public void setAffectedSubjects(List<String> affectedSubjects) { this.affectedSubjects = affectedSubjects; }

    public int getTotalAffectedLectures() { return totalAffectedLectures; }
    public void setTotalAffectedLectures(int totalAffectedLectures) { this.totalAffectedLectures = totalAffectedLectures; }

    public int getReschedulableLectures() { return reschedulableLectures; }
    public void setReschedulableLectures(int reschedulableLectures) { this.reschedulableLectures = reschedulableLectures; }

    public int getNonReschedulableLectures() { return nonReschedulableLectures; }
    public void setNonReschedulableLectures(int nonReschedulableLectures) { this.nonReschedulableLectures = nonReschedulableLectures; }

    public List<String> getConflicts() { return conflicts; }
    public void setConflicts(List<String> conflicts) { this.conflicts = conflicts; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}
