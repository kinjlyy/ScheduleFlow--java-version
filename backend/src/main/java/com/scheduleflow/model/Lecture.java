package com.scheduleflow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "lectures",
    indexes = {
        @Index(name = "idx_lecture_timetable", columnList = "timetable_id"),
        @Index(name = "idx_lecture_room", columnList = "room_id"),
        @Index(name = "idx_lecture_teacher", columnList = "teacher_id"),
        @Index(name = "idx_lecture_section", columnList = "section_id"),
        @Index(name = "idx_lecture_day", columnList = "day")
    }
)
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_id", nullable = false)
    private Timetable timetable;

    @Column(name = "section_id", nullable = false)
    private String sectionId;

    @Column(name = "subject_id", nullable = false)
    private String subjectId;

    @Column(name = "teacher_id", nullable = false)
    private String teacherId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "day", nullable = false)
    private String day;

    @Column(name = "lecture_slot", nullable = false)
    private int lectureSlot;

    @Enumerated(EnumType.STRING)
    @Column(name = "lecture_type", nullable = false)
    private LectureType lectureType = LectureType.THEORY;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Lecture() {}

    public Lecture(Timetable timetable, String sectionId, String subjectId,
                   String teacherId, Room room, String day, int lectureSlot,
                   LectureType lectureType, LocalDateTime createdAt) {
        this.timetable = timetable;
        this.sectionId = sectionId;
        this.subjectId = subjectId;
        this.teacherId = teacherId;
        this.room = room;
        this.day = day;
        this.lectureSlot = lectureSlot;
        this.lectureType = lectureType != null ? lectureType : LectureType.THEORY;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Timetable getTimetable() { return timetable; }
    public void setTimetable(Timetable timetable) { this.timetable = timetable; }

    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }

    public int getLectureSlot() { return lectureSlot; }
    public void setLectureSlot(int lectureSlot) { this.lectureSlot = lectureSlot; }

    public LectureType getLectureType() { return lectureType; }
    public void setLectureType(LectureType lectureType) { this.lectureType = lectureType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
