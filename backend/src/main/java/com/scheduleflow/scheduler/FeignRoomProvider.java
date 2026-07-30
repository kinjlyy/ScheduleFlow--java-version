package com.scheduleflow.scheduler;

import com.scheduleflow.client.ResourceClient;
import com.scheduleflow.dto.RoomDTO;
import com.scheduleflow.mapper.RoomMapper;
import com.scheduleflow.model.Room;
import com.scheduleflow.repository.LocalRoomStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Production implementation of {@link RoomProvider} with LocalRoomStore fallback.
 *
 * <p>Queries {@code RESOURCE-SERVICE} via OpenFeign when available, and seamlessly
 * falls back to {@link LocalRoomStore} if {@code RESOURCE-SERVICE} is offline or unreachable.
 */
@Component
@Primary
public class FeignRoomProvider implements RoomProvider {

    private static final Logger log = LoggerFactory.getLogger(FeignRoomProvider.class);

    private final ResourceClient resourceClient;
    private final RoomMapper roomMapper;
    private final LocalRoomStore localRoomStore;

    public FeignRoomProvider(ResourceClient resourceClient, RoomMapper roomMapper, LocalRoomStore localRoomStore) {
        this.resourceClient = resourceClient;
        this.roomMapper = roomMapper;
        this.localRoomStore = localRoomStore;
    }

    /**
     * Retrieves all active rooms from {@code RESOURCE-SERVICE} with fallback to {@link LocalRoomStore}.
     *
     * @return list of active domain {@link Room} models
     */
    @Override
    public List<Room> findAllActiveRooms() {
        try {
            List<RoomDTO> dtos = resourceClient.getActiveRooms();
            if (dtos != null && !dtos.isEmpty()) {
                dtos.forEach(localRoomStore::createRoom);
                return roomMapper.toDomainList(dtos);
            }
        } catch (Exception e) {
            log.warn("RESOURCE-SERVICE unreachable for active rooms, falling back to LocalRoomStore: {}", e.getMessage());
        }
        return localRoomStore.getActiveDomainRooms();
    }

    /**
     * Looks up a room by ID from {@code RESOURCE-SERVICE} with fallback to {@link LocalRoomStore}.
     *
     * @param id unique room identifier
     * @return optional containing domain {@link Room} if present
     */
    @Override
    public Optional<Room> findRoomById(Long id) {
        try {
            RoomDTO dto = resourceClient.getRoom(id);
            if (dto != null && dto.isActive()) {
                return Optional.ofNullable(roomMapper.toDomain(dto));
            }
        } catch (Exception e) {
            log.warn("RESOURCE-SERVICE unreachable for room lookup ID {}, falling back to LocalRoomStore: {}", id, e.getMessage());
        }
        return localRoomStore.getRoomById(id)
                .filter(RoomDTO::isActive)
                .map(dto -> {
                    Room r = new Room();
                    r.setId(dto.getId());
                    r.setRoomNumber(dto.getRoomNumber());
                    r.setMaximumCapacity(dto.getMaximumCapacity());
                    r.setRoomType(dto.getRoomType());
                    r.setHasProjector(dto.isHasProjector());
                    r.setHasAc(dto.isHasAc());
                    r.setHasComputers(dto.isHasComputers());
                    r.setActive(dto.isActive());
                    return r;
                });
    }
}
