package com.scheduleflow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "timetables")
public class Timetable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String semester;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "generated_by")
    private String generatedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimetableStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Timetable() {}

    public Timetable(String name, String semester, String academicYear,
                     LocalDateTime generatedAt, String generatedBy,
                     TimetableStatus status, LocalDateTime createdAt) {
        this.name = name;
        this.semester = semester;
        this.academicYear = academicYear;
        this.generatedAt = generatedAt;
        this.generatedBy = generatedBy;
        this.status = status;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public TimetableStatus getStatus() { return status; }
    public void setStatus(TimetableStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
