package com.scheduleflow.model;

/**
 * Pure in-memory domain model representing a room resource inside {@code TIMETABLE-SERVICE}.
 *
 * <p><strong>Architectural Boundary Notice (Phase 6):</strong>
 * This class is <strong>NOT</strong> a JPA database entity and has <strong>zero persistence responsibility</strong>.
 * Database persistence, entity mapping, and CRUD management for room resources are strictly and exclusively
 * owned by {@code RESOURCE-SERVICE}.
 *
 * <p>This model is used purely in-memory by:
 * <ul>
 *   <li>{@link com.scheduleflow.service.SchedulerService} — for graph-coloring room allocation constraints</li>
 *   <li>{@link com.scheduleflow.scheduler.RoomProvider} — domain abstraction interface</li>
 *   <li>{@link com.scheduleflow.scheduler.FeignRoomProvider} — OpenFeign integration layer</li>
 *   <li>{@link com.scheduleflow.mapper.RoomMapper} — DTO-to-domain transformation</li>
 * </ul>
 */
public class Room {

    private Long id;
    private String roomNumber;
    private int maximumCapacity;
    private RoomType roomType;
    private boolean hasProjector;
    private boolean hasAc;
    private boolean hasComputers;
    private boolean active = true;

    public Room() {}

    public Room(String roomNumber, int maximumCapacity, RoomType roomType,
                boolean hasProjector, boolean hasAc, boolean hasComputers, boolean active) {
        this.roomNumber = roomNumber;
        this.maximumCapacity = maximumCapacity;
        this.roomType = roomType;
        this.hasProjector = hasProjector;
        this.hasAc = hasAc;
        this.hasComputers = hasComputers;
        this.active = active;
    }

    public Room(Long id, String roomNumber, int maximumCapacity, RoomType roomType,
                boolean hasProjector, boolean hasAc, boolean hasComputers, boolean active) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.maximumCapacity = maximumCapacity;
        this.roomType = roomType;
        this.hasProjector = hasProjector;
        this.hasAc = hasAc;
        this.hasComputers = hasComputers;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public int getMaximumCapacity() { return maximumCapacity; }
    public void setMaximumCapacity(int maximumCapacity) { this.maximumCapacity = maximumCapacity; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public boolean isHasProjector() { return hasProjector; }
    public void setHasProjector(boolean hasProjector) { this.hasProjector = hasProjector; }

    public boolean isHasAc() { return hasAc; }
    public void setHasAc(boolean hasAc) { this.hasAc = hasAc; }

    public boolean isHasComputers() { return hasComputers; }
    public void setHasComputers(boolean hasComputers) { this.hasComputers = hasComputers; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
