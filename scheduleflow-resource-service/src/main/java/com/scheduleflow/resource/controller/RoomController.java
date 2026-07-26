package com.scheduleflow.resource.controller;

import com.scheduleflow.resource.dto.RoomDTO;
import com.scheduleflow.resource.dto.RoomSummaryDTO;
import com.scheduleflow.resource.model.RoomType;
import com.scheduleflow.resource.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Room Resource Management in {@code RESOURCE-SERVICE}.
 *
 * <p>Phase 4 & Phase 5 Microservice Migration:
 * Target controller for all room resource queries routed by {@code API-GATEWAY} or invoked via
 * OpenFeign clients from other microservices.
 */
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

    @GetMapping("/active")
    public ResponseEntity<List<RoomDTO>> getActiveRooms() {
        return ResponseEntity.ok(roomService.getAvailableRoomsForCapacity(1));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<RoomDTO>> getRoomsByType(@PathVariable("type") RoomType type) {
        return ResponseEntity.ok(roomService.getAvailableRoomsForType(type));
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
