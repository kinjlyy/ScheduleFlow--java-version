package com.scheduleflow.controller;

import com.scheduleflow.dto.RoomDTO;
import com.scheduleflow.dto.RoomSummaryDTO;
import com.scheduleflow.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Legacy Room Controller in {@code TIMETABLE-SERVICE}.
 *
 * <p><strong>Phase 4 Migration Notice:</strong>
 * Room resource management has been extracted into {@code RESOURCE-SERVICE}. External HTTP client
 * traffic for {@code /api/rooms/**} is now routed exclusively to {@code RESOURCE-SERVICE} by {@code API-GATEWAY}.
 *
 * <p>This class is temporarily retained for zero-downtime backward compatibility until Phase 5
 * (OpenFeign inter-service communication migration). It will be permanently removed in Phase 5.
 *
 * @deprecated since Phase 4, scheduled for removal in Phase 5.
 */
@Deprecated(since = "Phase 4", forRemoval = true)
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/summary")
    public ResponseEntity<RoomSummaryDTO> getRoomSummary() {
        return ResponseEntity.ok(roomService.getRoomSummary());
    }

    @GetMapping("/capacity/{capacity}")
    public ResponseEntity<List<RoomDTO>> getRoomsByCapacity(@PathVariable("capacity") int capacity) {
        return ResponseEntity.ok(roomService.getAvailableRoomsForCapacity(capacity));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @PostMapping
    public ResponseEntity<RoomDTO> createRoom(@Valid @RequestBody RoomDTO roomDTO) {
        RoomDTO created = roomService.createRoom(roomDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomDTO> updateRoom(@PathVariable("id") Long id, @Valid @RequestBody RoomDTO roomDTO) {
        RoomDTO updated = roomService.updateRoom(id, roomDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable("id") Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}
