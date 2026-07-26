package com.scheduleflow.dto;

import com.scheduleflow.model.TimetableStatus;
import java.time.LocalDateTime;

public class TimetableDTO {

    private Long id;
    private String name;
    private String semester;
    private String academicYear;
    private LocalDateTime generatedAt;
    private String generatedBy;
    private TimetableStatus status;
    private LocalDateTime createdAt;

    public TimetableDTO() {}

    public TimetableDTO(Long id, String name, String semester, String academicYear,
                        LocalDateTime generatedAt, String generatedBy,
                        TimetableStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.semester = semester;
        this.academicYear = academicYear;
        this.generatedAt = generatedAt;
        this.generatedBy = generatedBy;
        this.status = status;
        this.createdAt = createdAt;
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
