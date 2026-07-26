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
 * <p>Phase 5 Microservice Migration:
 * Discovers {@code RESOURCE-SERVICE} dynamically via Eureka Service Registry.
 * Exposes business-oriented capabilities rather than low-level database query APIs.
 */
@FeignClient(
    name = "RESOURCE-SERVICE",
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
}
