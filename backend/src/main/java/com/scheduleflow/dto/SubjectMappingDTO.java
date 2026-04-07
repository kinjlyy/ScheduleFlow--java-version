package com.scheduleflow.dto;

public class SubjectMappingDTO {
    private String subject;
    private String teacher;
    private int lecturesPerWeek;

    public SubjectMappingDTO() {}

    public SubjectMappingDTO(String subject, String teacher, int lecturesPerWeek) {
        this.subject = subject;
        this.teacher = teacher;
        this.lecturesPerWeek = lecturesPerWeek;
    }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getTeacher() { return teacher; }
    public void setTeacher(String teacher) { this.teacher = teacher; }

    public int getLecturesPerWeek() { return lecturesPerWeek; }
    public void setLecturesPerWeek(int lecturesPerWeek) { this.lecturesPerWeek = lecturesPerWeek; }
}
