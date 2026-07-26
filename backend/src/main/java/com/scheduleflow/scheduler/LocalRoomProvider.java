package com.scheduleflow.scheduler;

import com.scheduleflow.model.Room;
import com.scheduleflow.repository.RoomRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Legacy (local database) implementation of {@link RoomProvider}.
 *
 * <p><strong>Phase 5 Migration Notice:</strong>
 * Replaced by {@link FeignRoomProvider} as the primary {@link RoomProvider} bean.
 * Retained temporarily for backward compatibility and fallback purposes until Phase 6 cleanup.
 *
 * @deprecated since Phase 5, scheduled for removal in Phase 6.
 */
@Deprecated(since = "Phase 5", forRemoval = true)
@Component
public class LocalRoomProvider implements RoomProvider {

    private final RoomRepository roomRepository;

    public LocalRoomProvider(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    /**
     * Returns all rooms where {@code active = true}.
     */
    @Override
    public List<Room> findAllActiveRooms() {
        return roomRepository.findAll().stream()
                .filter(Room::isActive)
                .toList();
    }

    /**
     * Looks up a room by ID.
     */
    @Override
    public Optional<Room> findRoomById(Long id) {
        return roomRepository.findById(id);
    }
}
