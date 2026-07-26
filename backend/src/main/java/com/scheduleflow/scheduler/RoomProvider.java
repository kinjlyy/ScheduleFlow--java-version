package com.scheduleflow.scheduler;

import com.scheduleflow.model.Room;

import java.util.List;
import java.util.Optional;

/**
 * Abstraction over room data access for the scheduling engine.
 *
 * <p><strong>Current implementation:</strong> {@link LocalRoomProvider} — delegates to
 * {@code RoomRepository} (JPA / PostgreSQL).
 *
 * <p><strong>Future implementation:</strong> When Room data migrates to the Resource Service,
 * this interface will be implemented by a Feign client:
 * <pre>
 *   &#64;FeignClient(name = "resource-service", url = "${resource-service.url}")
 *   public interface ResourceServiceRoomClient extends RoomProvider { ... }
 * </pre>
 * The {@link com.scheduleflow.service.SchedulerService} will require zero changes when
 * that replacement happens, because it depends only on this interface.
 *
 * <p><strong>Why this exists:</strong> Dependency Inversion Principle — the scheduling
 * engine (high-level policy) must not depend on JPA repositories (low-level detail).
 * This abstraction severs that coupling cleanly.
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
