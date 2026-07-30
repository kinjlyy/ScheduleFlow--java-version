package com.scheduleflow.client;

import com.scheduleflow.config.ResourceFeignConfiguration;
import com.scheduleflow.dto.RoomDTO;
import com.scheduleflow.model.RoomType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Declarative OpenFeign client for communicating with {@code RESOURCE-SERVICE}.
 *
 * <p>Uses environment-variable URL resolution instead of Eureka service discovery,
 * required for Render Free Tier where sleeping services cause Eureka heartbeat
 * expiry and Feign connect timeouts before cold-start completes.
 *
 * <p>Set {@code RESOURCE_SERVICE_URL} in production environment variables
 * to the Render public URL, e.g. {@code https://scheduleflow-java-version-1.onrender.com}.
 */
@FeignClient(
    name = "RESOURCE-SERVICE",
    url = "${RESOURCE_SERVICE_URL:http://localhost:8081}",
    configuration = ResourceFeignConfiguration.class
)
public interface ResourceClient {

    /**
     * Business Capability: Fetches all active rooms available for scheduling.
     */
    @GetMapping("/api/rooms/active")
    List<RoomDTO> getActiveRooms();

    /**
     * Business Capability: Fetches active rooms filtered by room type (e.g. CLASSROOM, LABORATORY).
     */
    @GetMapping("/api/rooms/type/{type}")
    List<RoomDTO> getRoomsByType(@PathVariable("type") RoomType type);

    /**
     * Business Capability: Fetches all rooms (active and inactive).
     */
    @GetMapping("/api/rooms")
    List<RoomDTO> getAllRooms();

    /**
     * Business Capability: Fetches a specific room by its unique identifier.
     */
    @GetMapping("/api/rooms/{id}")
    RoomDTO getRoom(@PathVariable("id") Long id);

    /**
     * Business Capability: Fetches room summary statistics.
     */
    @GetMapping("/api/rooms/summary")
    Object getRoomSummary();

    /**
     * Business Capability: Creates a new room in RESOURCE-SERVICE.
     */
    @org.springframework.web.bind.annotation.PostMapping("/api/rooms")
    RoomDTO createRoom(@org.springframework.web.bind.annotation.RequestBody RoomDTO roomDTO);

    /**
     * Business Capability: Updates an existing room in RESOURCE-SERVICE.
     */
    @org.springframework.web.bind.annotation.PutMapping("/api/rooms/{id}")
    RoomDTO updateRoom(@PathVariable("id") Long id, @org.springframework.web.bind.annotation.RequestBody RoomDTO roomDTO);

    /**
     * Business Capability: Deletes a room from RESOURCE-SERVICE.
     */
    @org.springframework.web.bind.annotation.DeleteMapping("/api/rooms/{id}")
    void deleteRoom(@PathVariable("id") Long id);
}
