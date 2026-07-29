package com.scheduleflow.event.client;

import com.scheduleflow.event.dto.RoomResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * ResourceServiceClient — Feign client for inter-service calls to RESOURCE-SERVICE.
 *
 * <p><strong>Phase 7B Status: Active.</strong>
 * Returns typed {@link RoomResponse} objects directly without wrapping in {@code ResponseEntity}.
 * Business logic delegates room metadata lookups to this client.
 *
 * <p>Uses direct URL resolution via {@code RESOURCE_SERVICE_URL} environment variable,
 * bypassing Eureka service discovery for compatibility with Render Free Tier.
 */
@FeignClient(name = "RESOURCE-SERVICE", url = "${RESOURCE_SERVICE_URL:http://localhost:8081}", path = "/api/rooms")
public interface ResourceServiceClient {

    /**
     * Retrieve room details by ID from RESOURCE-SERVICE.
     *
     * @param roomId ID of the target room
     * @return {@link RoomResponse} containing room metadata and active status
     */
    @GetMapping("/{roomId}")
    RoomResponse getRoomById(@PathVariable("roomId") Long roomId);

    /**
     * Retrieve all active rooms from RESOURCE-SERVICE.
     *
     * @return List of active {@link RoomResponse} objects
     */
    @GetMapping("/active")
    List<RoomResponse> getActiveRooms();
}
