package com.scheduleflow.resource.service;

import com.scheduleflow.resource.dto.RoomDTO;
import com.scheduleflow.resource.dto.RoomSummaryDTO;
import com.scheduleflow.resource.exception.ResourceNotFoundException;
import com.scheduleflow.resource.exception.ValidationException;
import com.scheduleflow.resource.model.Room;
import com.scheduleflow.resource.model.RoomType;
import com.scheduleflow.resource.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<RoomDTO> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public RoomDTO getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", id));
        return toDTO(room);
    }

    public RoomDTO createRoom(RoomDTO dto) {
        if (roomRepository.existsByRoomNumber(dto.getRoomNumber())) {
            throw new ValidationException("Room number '" + dto.getRoomNumber() + "' already exists.");
        }
        Room room = toEntity(dto);
        Room saved = roomRepository.save(room);
        return toDTO(saved);
    }

    public RoomDTO updateRoom(Long id, RoomDTO dto) {
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
        return toDTO(updated);
    }

    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Room", id);
        }
        roomRepository.deleteById(id);
    }

    public RoomSummaryDTO getRoomSummary() {
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

    public List<RoomDTO> getAvailableRoomsForCapacity(int minCapacity) {
        return roomRepository.findByActiveTrueAndMaximumCapacityGreaterThanEqual(minCapacity).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<RoomDTO> getAvailableRoomsForType(RoomType roomType) {
        return roomRepository.findAll().stream()
                .filter(Room::isActive)
                .filter(r -> r.getRoomType() == roomType)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

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
