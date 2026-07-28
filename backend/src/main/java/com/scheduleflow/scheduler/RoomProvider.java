package com.scheduleflow.scheduler;

import com.scheduleflow.model.Room;

import java.util.List;
import java.util.Optional;

/**
 * Abstraction over room data access for the scheduling engine.
 *
 * <p><strong>Active implementation:</strong> {@link FeignRoomProvider} — fetches active room resources
 * over OpenFeign from {@code RESOURCE-SERVICE} and maps them to pure in-memory domain {@link Room} models.
 *
 * <p><strong>Why this exists:</strong> Dependency Inversion Principle — the scheduling
 * engine (high-level policy) depends strictly on this abstraction, remaining completely decoupled
 * from transport and database persistence details.
 */
public interface RoomProvider {

    /**
     * Returns all rooms that are currently active (available for scheduling).
     * The returned list must never be null; return an empty list if no rooms exist.
     */
    List<Room> findAllActiveRooms();

    /**
     * Finds a room by its database identifier.
     * Returns empty if the room does not exist or is not active.
     */
    Optional<Room> findRoomById(Long id);
}
