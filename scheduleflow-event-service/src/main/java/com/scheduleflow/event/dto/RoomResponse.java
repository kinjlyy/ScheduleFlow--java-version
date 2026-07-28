package com.scheduleflow.event.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * RoomResponse — Typed DTO representing room data received from RESOURCE-SERVICE via Feign.
 *
 * <p>Mirrors RESOURCE-SERVICE's {@code RoomDTO}. Uses String for {@code roomType} to avoid
 * tight enum coupling — Resource Service owns the RoomType enum definition.
 *
 * <p>@JsonIgnoreProperties(ignoreUnknown = true) ensures forward-compatibility:
 * if Resource Service adds new fields, this DTO will not break.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoomResponse {

    private Long id;
    private String roomNumber;
    private int maximumCapacity;
    private String roomType;
    private boolean hasProjector;
    private boolean hasAc;
    private boolean hasComputers;
    private boolean active;

    public RoomResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public int getMaximumCapacity() { return maximumCapacity; }
    public void setMaximumCapacity(int maximumCapacity) { this.maximumCapacity = maximumCapacity; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public boolean isHasProjector() { return hasProjector; }
    public void setHasProjector(boolean hasProjector) { this.hasProjector = hasProjector; }

    public boolean isHasAc() { return hasAc; }
    public void setHasAc(boolean hasAc) { this.hasAc = hasAc; }

    public boolean isHasComputers() { return hasComputers; }
    public void setHasComputers(boolean hasComputers) { this.hasComputers = hasComputers; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
