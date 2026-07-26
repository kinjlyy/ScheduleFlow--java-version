package com.scheduleflow.dto;

import com.scheduleflow.model.LectureType;
import com.scheduleflow.model.PreferredRoomType;

public class SubjectMappingDTO {
    private String subject;
    private String teacher;
    private int lecturesPerWeek;
    private LectureType lectureType = LectureType.THEORY;
    private boolean projectorRequired = false;
    private PreferredRoomType preferredRoomType = PreferredRoomType.ANY;
    private boolean movable = true;

    public SubjectMappingDTO() {}

    public SubjectMappingDTO(String subject, String teacher, int lecturesPerWeek) {
        this.subject = subject;
        this.teacher = teacher;
        this.lecturesPerWeek = lecturesPerWeek;
    }

    public SubjectMappingDTO(String subject, String teacher, int lecturesPerWeek,
                             LectureType lectureType, boolean projectorRequired,
                             PreferredRoomType preferredRoomType, boolean movable) {
        this.subject = subject;
        this.teacher = teacher;
        this.lecturesPerWeek = lecturesPerWeek;
        this.lectureType = lectureType != null ? lectureType : LectureType.THEORY;
        this.projectorRequired = projectorRequired;
        this.preferredRoomType = preferredRoomType != null ? preferredRoomType : PreferredRoomType.ANY;
        this.movable = movable;
    }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getTeacher() { return teacher; }
    public void setTeacher(String teacher) { this.teacher = teacher; }

    public int getLecturesPerWeek() { return lecturesPerWeek; }
    public void setLecturesPerWeek(int lecturesPerWeek) { this.lecturesPerWeek = lecturesPerWeek; }

    public LectureType getLectureType() { return lectureType; }
    public void setLectureType(LectureType lectureType) { this.lectureType = lectureType; }

    public boolean isProjectorRequired() { return projectorRequired; }
    public void setProjectorRequired(boolean projectorRequired) { this.projectorRequired = projectorRequired; }

    public PreferredRoomType getPreferredRoomType() { return preferredRoomType; }
    public void setPreferredRoomType(PreferredRoomType preferredRoomType) { this.preferredRoomType = preferredRoomType; }

    public boolean isMovable() { return movable; }
    public void setMovable(boolean movable) { this.movable = movable; }
}
