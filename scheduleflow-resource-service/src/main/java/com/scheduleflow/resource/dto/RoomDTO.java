package com.scheduleflow.resource.dto;

import com.scheduleflow.resource.model.RoomType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RoomDTO {

    private Long id;

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @Min(value = 1, message = "Maximum capacity must be at least 1")
    private int maximumCapacity;

    @NotNull(message = "Room type is required")
    private RoomType roomType;

    private boolean hasProjector;
    private boolean hasAc;
    private boolean hasComputers;
    private boolean active = true;

    public RoomDTO() {}

    public RoomDTO(Long id, String roomNumber, int maximumCapacity, RoomType roomType,
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
