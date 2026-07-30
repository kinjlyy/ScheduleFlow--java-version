package com.scheduleflow.controller;

import com.scheduleflow.client.ResourceClient;
import com.scheduleflow.dto.RoomDTO;
import com.scheduleflow.repository.LocalRoomStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Delegating RoomController with resilient LocalRoomStore fallback.
 *
 * <p>Ensures room operations (POST, GET, PUT, DELETE) on {@code TIMETABLE-SERVICE} (e.g. deployed Render URLs)
 * NEVER fail with 500 or 404, even if {@code RESOURCE-SERVICE} is down or unconfigured.
 */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private static final Logger log = LoggerFactory.getLogger(RoomController.class);

    private final ResourceClient resourceClient;
    private final LocalRoomStore localRoomStore;

    public RoomController(ResourceClient resourceClient, LocalRoomStore localRoomStore) {
        this.resourceClient = resourceClient;
        this.localRoomStore = localRoomStore;
    }

    @GetMapping
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        try {
            List<RoomDTO> remote = resourceClient.getAllRooms();
            if (remote != null && !remote.isEmpty()) {
                remote.forEach(localRoomStore::createRoom);
                return ResponseEntity.ok(remote);
            }
        } catch (Exception e) {
            log.warn("RESOURCE-SERVICE unavailable for getAllRooms, using LocalRoomStore: {}", e.getMessage());
        }
        return ResponseEntity.ok(localRoomStore.getAllRooms());
    }

    @GetMapping("/active")
    public ResponseEntity<List<RoomDTO>> getActiveRooms() {
        try {
            List<RoomDTO> remote = resourceClient.getActiveRooms();
            if (remote != null && !remote.isEmpty()) {
                remote.forEach(localRoomStore::createRoom);
                return ResponseEntity.ok(remote);
            }
        } catch (Exception e) {
            log.warn("RESOURCE-SERVICE unavailable for getActiveRooms, using LocalRoomStore: {}", e.getMessage());
        }
        return ResponseEntity.ok(localRoomStore.getActiveRooms());
    }

    @GetMapping("/summary")
    public ResponseEntity<Object> getRoomSummary() {
        try {
            Object remoteSummary = resourceClient.getRoomSummary();
            if (remoteSummary != null) {
                return ResponseEntity.ok(remoteSummary);
            }
        } catch (Exception e) {
            log.warn("RESOURCE-SERVICE unavailable for getRoomSummary, building local summary: {}", e.getMessage());
        }
        List<RoomDTO> rooms = localRoomStore.getAllRooms();
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRooms", rooms.size());
        summary.put("activeRooms", rooms.stream().filter(RoomDTO::isActive).count());
        summary.put("inactiveRooms", rooms.stream().filter(r -> !r.isActive()).count());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable("id") Long id) {
        try {
            RoomDTO remote = resourceClient.getRoom(id);
            if (remote != null) return ResponseEntity.ok(remote);
        } catch (Exception e) {
            log.warn("RESOURCE-SERVICE unavailable for getRoomById({}), using LocalRoomStore: {}", id, e.getMessage());
        }
        return localRoomStore.getRoomById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RoomDTO> createRoom(@RequestBody RoomDTO roomDTO) {
        RoomDTO savedLocal = localRoomStore.createRoom(roomDTO);
        try {
            RoomDTO remote = resourceClient.createRoom(roomDTO);
            if (remote != null) {
                localRoomStore.createRoom(remote);
                return ResponseEntity.status(HttpStatus.CREATED).body(remote);
            }
        } catch (Exception e) {
            log.warn("RESOURCE-SERVICE unavailable during createRoom, room saved in LocalRoomStore: {}", e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(savedLocal);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomDTO> updateRoom(@PathVariable("id") Long id, @RequestBody RoomDTO roomDTO) {
        RoomDTO updatedLocal = localRoomStore.updateRoom(id, roomDTO);
        try {
            RoomDTO remote = resourceClient.updateRoom(id, roomDTO);
            if (remote != null) {
                return ResponseEntity.ok(remote);
            }
        } catch (Exception e) {
            log.warn("RESOURCE-SERVICE unavailable during updateRoom, updated in LocalRoomStore: {}", e.getMessage());
        }
        return ResponseEntity.ok(updatedLocal);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable("id") Long id) {
        localRoomStore.deleteRoom(id);
        try {
            resourceClient.deleteRoom(id);
        } catch (Exception e) {
            log.warn("RESOURCE-SERVICE unavailable during deleteRoom, deleted from LocalRoomStore: {}", e.getMessage());
        }
        return ResponseEntity.noContent().build();
    }
}
