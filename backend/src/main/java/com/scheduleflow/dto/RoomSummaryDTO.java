package com.scheduleflow.dto;

public class RoomSummaryDTO {

    private long totalRooms;
    private long activeRooms;
    private long inactiveRooms;
    private long classrooms;
    private long laboratories;
    private long seminarHalls;
    private long auditoriums;
    private long projectorEnabledRooms;
    private int largestCapacity;

    public RoomSummaryDTO() {}

    public RoomSummaryDTO(long totalRooms, long activeRooms, long inactiveRooms,
                          long classrooms, long laboratories, long seminarHalls, long auditoriums,
                          long projectorEnabledRooms, int largestCapacity) {
        this.totalRooms = totalRooms;
        this.activeRooms = activeRooms;
        this.inactiveRooms = inactiveRooms;
        this.classrooms = classrooms;
        this.laboratories = laboratories;
        this.seminarHalls = seminarHalls;
        this.auditoriums = auditoriums;
        this.projectorEnabledRooms = projectorEnabledRooms;
        this.largestCapacity = largestCapacity;
    }

    public long getTotalRooms() { return totalRooms; }
    public void setTotalRooms(long totalRooms) { this.totalRooms = totalRooms; }

    public long getActiveRooms() { return activeRooms; }
    public void setActiveRooms(long activeRooms) { this.activeRooms = activeRooms; }

    public long getInactiveRooms() { return inactiveRooms; }
    public void setInactiveRooms(long inactiveRooms) { this.inactiveRooms = inactiveRooms; }

    public long getClassrooms() { return classrooms; }
    public void setClassrooms(long classrooms) { this.classrooms = classrooms; }

    public long getLaboratories() { return laboratories; }
    public void setLaboratories(long laboratories) { this.laboratories = laboratories; }

    public long getSeminarHalls() { return seminarHalls; }
    public void setSeminarHalls(long seminarHalls) { this.seminarHalls = seminarHalls; }

    public long getAuditoriums() { return auditoriums; }
    public void setAuditoriums(long auditoriums) { this.auditoriums = auditoriums; }

    public long getProjectorEnabledRooms() { return projectorEnabledRooms; }
    public void setProjectorEnabledRooms(long projectorEnabledRooms) { this.projectorEnabledRooms = projectorEnabledRooms; }

    public int getLargestCapacity() { return largestCapacity; }
    public void setLargestCapacity(int largestCapacity) { this.largestCapacity = largestCapacity; }
}
