package com.scheduleflow.dto;

import com.scheduleflow.model.LectureType;

public class LectureDTO {

    private Long id;
    private Long timetableId;
    private String sectionId;
    private String subjectId;
    private String teacherId;
    private Long roomId;
    private String roomNumber;
    private String day;
    private int lectureSlot;
    private LectureType lectureType;

    public LectureDTO() {}

    public LectureDTO(Long id, Long timetableId, String sectionId, String subjectId,
                      String teacherId, Long roomId, String roomNumber, String day,
                      int lectureSlot, LectureType lectureType) {
        this.id = id;
        this.timetableId = timetableId;
        this.sectionId = sectionId;
        this.subjectId = subjectId;
        this.teacherId = teacherId;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.day = day;
        this.lectureSlot = lectureSlot;
        this.lectureType = lectureType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTimetableId() { return timetableId; }
    public void setTimetableId(Long timetableId) { this.timetableId = timetableId; }

    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }

    public int getLectureSlot() { return lectureSlot; }
    public void setLectureSlot(int lectureSlot) { this.lectureSlot = lectureSlot; }

    public LectureType getLectureType() { return lectureType; }
    public void setLectureType(LectureType lectureType) { this.lectureType = lectureType; }
}
