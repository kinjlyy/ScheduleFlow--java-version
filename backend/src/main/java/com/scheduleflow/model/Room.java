package com.scheduleflow.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_number", nullable = false, unique = true)
    private String roomNumber;

    @Column(name = "maximum_capacity", nullable = false)
    private int maximumCapacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private RoomType roomType;

    @Column(name = "has_projector", nullable = false)
    private boolean hasProjector;

    @Column(name = "has_ac", nullable = false)
    private boolean hasAc;

    @Column(name = "has_computers", nullable = false)
    private boolean hasComputers;

    @Column(name = "active", nullable = false)
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
