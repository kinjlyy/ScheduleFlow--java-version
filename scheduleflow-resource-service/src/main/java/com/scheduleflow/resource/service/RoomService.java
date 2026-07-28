package com.scheduleflow.resource.service;

import com.scheduleflow.resource.dto.RoomDTO;
import com.scheduleflow.resource.dto.RoomSummaryDTO;
import com.scheduleflow.resource.exception.ResourceNotFoundException;
import com.scheduleflow.resource.exception.ValidationException;
import com.scheduleflow.resource.model.Room;
import com.scheduleflow.resource.model.RoomType;
import com.scheduleflow.resource.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RoomService — Business logic for room resource management.
 *
 * <p>Owns all CRUD operations and validation for room entities. This is the single
 * source of truth for room metadata consumed by EVENT-SERVICE via OpenFeign.
 */
@Service
@Transactional
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Transactional(readOnly = true)
    public List<RoomDTO> getAllRooms() {
        log.debug("Fetching all rooms");
        return roomRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoomDTO getRoomById(Long id) {
        log.debug("Fetching room by id={}", id);
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", id));
        return toDTO(room);
    }

    public RoomDTO createRoom(RoomDTO dto) {
        log.info("Creating room: roomNumber={}, type={}, capacity={}", dto.getRoomNumber(), dto.getRoomType(), dto.getMaximumCapacity());
        if (roomRepository.existsByRoomNumber(dto.getRoomNumber())) {
            throw new ValidationException("Room number '" + dto.getRoomNumber() + "' already exists.");
        }
        Room room = toEntity(dto);
        Room saved = roomRepository.save(room);
        log.info("Room created: id={}, roomNumber={}", saved.getId(), saved.getRoomNumber());
        return toDTO(saved);
    }

    public RoomDTO updateRoom(Long id, RoomDTO dto) {
        log.info("Updating room id={}", id);
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", id));

        if (roomRepository.existsByRoomNumberAndIdNot(dto.getRoomNumber(), id)) {
            throw new ValidationException("Room number '" + dto.getRoomNumber() + "' already exists.");
        }

        room.setRoomNumber(dto.getRoomNumber());
        room.setMaximumCapacity(dto.getMaximumCapacity());
        room.setRoomType(dto.getRoomType());
        room.setHasProjector(dto.isHasProjector());
        room.setHasAc(dto.isHasAc());
        room.setHasComputers(dto.isHasComputers());
        room.setActive(dto.isActive());

        Room updated = roomRepository.save(room);
        log.info("Room updated: id={}, roomNumber={}, active={}", updated.getId(), updated.getRoomNumber(), updated.isActive());
        return toDTO(updated);
    }

    public void deleteRoom(Long id) {
        log.info("Deleting room id={}", id);
        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Room", id);
        }
        roomRepository.deleteById(id);
        log.info("Room deleted: id={}", id);
    }

    @Transactional(readOnly = true)
    public RoomSummaryDTO getRoomSummary() {
        log.debug("Computing room summary");
        List<Room> rooms = roomRepository.findAll();
        long totalRooms = rooms.size();
        long activeRooms = rooms.stream().filter(Room::isActive).count();
        long inactiveRooms = totalRooms - activeRooms;
        long classrooms = rooms.stream().filter(r -> r.getRoomType() == RoomType.CLASSROOM).count();
        long laboratories = rooms.stream().filter(r -> r.getRoomType() == RoomType.LABORATORY).count();
        long seminarHalls = rooms.stream().filter(r -> r.getRoomType() == RoomType.SEMINAR_HALL).count();
        long auditoriums = rooms.stream().filter(r -> r.getRoomType() == RoomType.AUDITORIUM).count();
        long projectorEnabled = rooms.stream().filter(Room::isHasProjector).count();
        int largestCap = rooms.stream().mapToInt(Room::getMaximumCapacity).max().orElse(0);

        return new RoomSummaryDTO(
                totalRooms, activeRooms, inactiveRooms,
                classrooms, laboratories, seminarHalls, auditoriums,
                projectorEnabled, largestCap
        );
    }

    @Transactional(readOnly = true)
    public List<RoomDTO> getAvailableRoomsForCapacity(int minCapacity) {
        log.debug("Fetching active rooms with capacity >= {}", minCapacity);
        return roomRepository.findByActiveTrueAndMaximumCapacityGreaterThanEqual(minCapacity).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RoomDTO> getAvailableRoomsForType(RoomType roomType) {
        log.debug("Fetching active rooms of type={}", roomType);
        return roomRepository.findAll().stream()
                .filter(Room::isActive)
                .filter(r -> r.getRoomType() == roomType)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Mapping helpers ────────────────────────────────────────────────────────

    private RoomDTO toDTO(Room room) {
        return new RoomDTO(
                room.getId(),
                room.getRoomNumber(),
                room.getMaximumCapacity(),
                room.getRoomType(),
                room.isHasProjector(),
                room.isHasAc(),
                room.isHasComputers(),
                room.isActive()
        );
    }

    private Room toEntity(RoomDTO dto) {
        return new Room(
                dto.getRoomNumber(),
                dto.getMaximumCapacity(),
                dto.getRoomType(),
                dto.isHasProjector(),
                dto.isHasAc(),
                dto.isHasComputers(),
                dto.isActive()
        );
    }
}
