package com.scheduleflow.controller;

import com.scheduleflow.client.ResourceClient;
import com.scheduleflow.dto.RoomDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller delegating room resource queries and operations to {@code RESOURCE-SERVICE} via OpenFeign.
 *
 * <p>Ensures room operations sent to {@code TIMETABLE-SERVICE} (e.g. deployed monolithic Render endpoints)
 * are transparently routed to {@code RESOURCE-SERVICE} and saved in the database.
 */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final ResourceClient resourceClient;

    public RoomController(ResourceClient resourceClient) {
        this.resourceClient = resourceClient;
    }

    @GetMapping
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        return ResponseEntity.ok(resourceClient.getAllRooms());
    }

    @GetMapping("/active")
    public ResponseEntity<List<RoomDTO>> getActiveRooms() {
        return ResponseEntity.ok(resourceClient.getActiveRooms());
    }

    @GetMapping("/summary")
    public ResponseEntity<Object> getRoomSummary() {
        return ResponseEntity.ok(resourceClient.getRoomSummary());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(resourceClient.getRoom(id));
    }

    @PostMapping
    public ResponseEntity<RoomDTO> createRoom(@RequestBody RoomDTO roomDTO) {
        RoomDTO created = resourceClient.createRoom(roomDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomDTO> updateRoom(@PathVariable("id") Long id, @RequestBody RoomDTO roomDTO) {
        RoomDTO updated = resourceClient.updateRoom(id, roomDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable("id") Long id) {
        resourceClient.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}
