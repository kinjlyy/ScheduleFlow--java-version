package com.scheduleflow.repository;

import com.scheduleflow.dto.RoomDTO;
import com.scheduleflow.model.Room;
import com.scheduleflow.model.RoomType;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Thread-safe local room store in TIMETABLE-SERVICE.
 *
 * <p>Serves as a resilient fallback when RESOURCE-SERVICE is unavailable or unreachable.
 * Ensures room CRUD and timetable scheduling succeed seamlessly.
 */
@Component
public class LocalRoomStore {

    private final Map<Long, RoomDTO> roomMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(100);

    public List<RoomDTO> getAllRooms() {
        return new ArrayList<>(roomMap.values());
    }

    public List<RoomDTO> getActiveRooms() {
        return roomMap.values().stream()
                .filter(RoomDTO::isActive)
                .collect(Collectors.toList());
    }

    public Optional<RoomDTO> getRoomById(Long id) {
        return Optional.ofNullable(roomMap.get(id));
    }

    public RoomDTO createRoom(RoomDTO dto) {
        Long id = dto.getId() != null ? dto.getId() : idGenerator.incrementAndGet();
        RoomDTO saved = new RoomDTO(
                id,
                dto.getRoomNumber(),
                dto.getMaximumCapacity() > 0 ? dto.getMaximumCapacity() : 60,
                dto.getRoomType() != null ? dto.getRoomType() : RoomType.CLASSROOM,
                dto.isHasProjector(),
                dto.isHasAc(),
                dto.isHasComputers(),
                dto.isActive()
        );
        roomMap.put(id, saved);
        return saved;
    }

    public RoomDTO updateRoom(Long id, RoomDTO dto) {
        dto.setId(id);
        roomMap.put(id, dto);
        return dto;
    }

    public void deleteRoom(Long id) {
        roomMap.remove(id);
    }

    public List<Room> getActiveDomainRooms() {
        return getActiveRooms().stream().map(dto -> {
            Room r = new Room();
            r.setId(dto.getId());
            r.setRoomNumber(dto.getRoomNumber());
            r.setMaximumCapacity(dto.getMaximumCapacity());
            r.setRoomType(dto.getRoomType() != null ? dto.getRoomType() : RoomType.CLASSROOM);
            r.setHasProjector(dto.isHasProjector());
            r.setHasAc(dto.isHasAc());
            r.setHasComputers(dto.isHasComputers());
            r.setActive(dto.isActive());
            return r;
        }).collect(Collectors.toList());
    }
}
