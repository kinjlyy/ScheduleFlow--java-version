package com.scheduleflow.event.dto;

/**
 * ReservationResponse — API response returned after a successful room reservation.
 *
 * <p>Composes {@link EventResponse} (full event record) with {@link RoomResponse}
 * (room metadata from RESOURCE-SERVICE) and a human-readable confirmation message.
 *
 * <p>This avoids duplicating EventResponse fields while providing everything the
 * frontend needs in a single response:
 * <ul>
 *   <li>Full event/reservation details via {@code event}</li>
 *   <li>Room metadata (number, capacity, type, facilities) via {@code roomDetails}</li>
 *   <li>A confirmation message via {@code message}</li>
 * </ul>
 */
public class ReservationResponse {

    private EventResponse event;
    private RoomResponse roomDetails;
    private String message;

    public ReservationResponse() {}

    public ReservationResponse(EventResponse event, RoomResponse roomDetails, String message) {
        this.event = event;
        this.roomDetails = roomDetails;
        this.message = message;
    }

    public EventResponse getEvent() { return event; }
    public void setEvent(EventResponse event) { this.event = event; }

    public RoomResponse getRoomDetails() { return roomDetails; }
    public void setRoomDetails(RoomResponse roomDetails) { this.roomDetails = roomDetails; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
