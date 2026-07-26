package com.scheduleflow.mapper;

import com.scheduleflow.dto.RoomDTO;
import com.scheduleflow.model.Room;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Isolated Domain Mapper for Room Resources.
 *
 * <p>Phase 5 Microservice Migration:
 * Responsible strictly for converting {@link RoomDTO} transport objects (received over OpenFeign)
 * into internal {@link Room} domain entity models consumed by the scheduling core.
 */
@Component
public class RoomMapper {

    /**
     * Maps a single {@link RoomDTO} transport object to a domain {@link Room} entity.
     *
     * @param dto source DTO, may be null
     * @return mapped {@link Room} domain model, or null if input is null
     */
    public Room toDomain(RoomDTO dto) {
        if (dto == null) {
            return null;
        }
        Room room = new Room();
        room.setId(dto.getId());
        room.setRoomNumber(dto.getRoomNumber());
        room.setMaximumCapacity(dto.getMaximumCapacity());
        room.setRoomType(dto.getRoomType());
        room.setHasProjector(dto.isHasProjector());
        room.setHasAc(dto.isHasAc());
        room.setHasComputers(dto.isHasComputers());
        room.setActive(dto.isActive());
        return room;
    }

    /**
     * Maps a list of {@link RoomDTO} transport objects to a list of domain {@link Room} entities.
     *
     * @param dtos list of source DTOs, may be null or empty
     * @return list of mapped {@link Room} domain models, never null
     */
    public List<Room> toDomainList(List<RoomDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }
        return dtos.stream()
                .filter(RoomDTO::isActive)
                .map(this::toDomain)
                .toList();
    }
}
