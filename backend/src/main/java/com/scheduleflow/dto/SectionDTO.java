package com.scheduleflow.dto;

import java.util.List;

public class SectionDTO {
    private String id;
    private String name;
    private int capacity;
    private Long fixedRoomId;
    private List<String> subjects;
    private List<String> teachers;
    private List<SubjectMappingDTO> mappings;

    public SectionDTO() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public Long getFixedRoomId() { return fixedRoomId; }
    public void setFixedRoomId(Long fixedRoomId) { this.fixedRoomId = fixedRoomId; }

    public List<String> getSubjects() { return subjects; }
    public void setSubjects(List<String> subjects) { this.subjects = subjects; }

    public List<String> getTeachers() { return teachers; }
    public void setTeachers(List<String> teachers) { this.teachers = teachers; }

    public List<SubjectMappingDTO> getMappings() { return mappings; }
    public void setMappings(List<SubjectMappingDTO> mappings) { this.mappings = mappings; }
}
