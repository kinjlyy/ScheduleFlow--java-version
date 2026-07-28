package com.scheduleflow.scheduler;

import com.scheduleflow.client.ResourceClient;
import com.scheduleflow.dto.RoomDTO;
import com.scheduleflow.exception.RoomServiceUnavailableException;
import com.scheduleflow.mapper.RoomMapper;
import com.scheduleflow.model.Room;
import feign.FeignException;
import feign.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Production OpenFeign implementation of {@link RoomProvider}.
 *
 * <p>Phase 5/6 Microservice Migration:
 * Serves as the sole {@link RoomProvider} implementation in {@code TIMETABLE-SERVICE}.
 * Delegates HTTP calls to {@link ResourceClient} and relies on {@link RoomMapper} for DTO-to-domain mapping.
 *
 * <p><strong>Clean Architecture & Exception Flow:</strong>
 * Catches low-level infrastructure exceptions ({@link FeignException}, {@link RetryableException})
 * and translates them into a transport-agnostic {@link RoomServiceUnavailableException}.
 * This ensures {@link com.scheduleflow.service.SchedulerService} remains clean and framework-independent,
 * while web REST requests automatically map to HTTP 503 Service Unavailable.
 */
@Component
@Primary
public class FeignRoomProvider implements RoomProvider {

    private static final Logger log = LoggerFactory.getLogger(FeignRoomProvider.class);

    private final ResourceClient resourceClient;
    private final RoomMapper roomMapper;

    public FeignRoomProvider(ResourceClient resourceClient, RoomMapper roomMapper) {
        this.resourceClient = resourceClient;
        this.roomMapper = roomMapper;
    }

    /**
     * Retrieves all active rooms from {@code RESOURCE-SERVICE}.
     *
     * @return list of active domain {@link Room} models
     * @throws RoomServiceUnavailableException if {@code RESOURCE-SERVICE} is unreachable or fails
     */
    @Override
    public List<Room> findAllActiveRooms() {
        try {
            List<RoomDTO> dtos = resourceClient.getActiveRooms();
            return roomMapper.toDomainList(dtos);
        } catch (RetryableException e) {
            log.error("Network connectivity/timeout error contacting RESOURCE-SERVICE: {}", e.getMessage());
            throw new RoomServiceUnavailableException("Resource Service is currently unreachable. Unable to fetch room resources.", e);
        } catch (FeignException e) {
            log.error("HTTP error {} received from RESOURCE-SERVICE: {}", e.status(), e.getMessage());
            throw new RoomServiceUnavailableException("Resource Service returned error status: " + e.status(), e);
        } catch (Exception e) {
            log.error("Unexpected infrastructure failure while communicating with RESOURCE-SERVICE: {}", e.getMessage(), e);
            throw new RoomServiceUnavailableException("Unexpected error communicating with Resource Service", e);
        }
    }

    /**
     * Looks up a room by ID from {@code RESOURCE-SERVICE}.
     *
     * @param id unique room identifier
     * @return optional containing domain {@link Room} if present and active; empty if 404 Not Found
     * @throws RoomServiceUnavailableException if {@code RESOURCE-SERVICE} is unreachable or fails
     */
    @Override
    public Optional<Room> findRoomById(Long id) {
        try {
            RoomDTO dto = resourceClient.getRoom(id);
            if (dto == null || !dto.isActive()) {
                return Optional.empty();
            }
            return Optional.ofNullable(roomMapper.toDomain(dto));
        } catch (FeignException.NotFound e) {
            log.warn("Room ID {} not found in RESOURCE-SERVICE", id);
            return Optional.empty();
        } catch (RetryableException e) {
            log.error("Network connectivity error fetching room ID {} from RESOURCE-SERVICE: {}", id, e.getMessage());
            throw new RoomServiceUnavailableException("Resource Service is currently unreachable for room lookup.", e);
        } catch (FeignException e) {
            log.error("HTTP error {} fetching room ID {} from RESOURCE-SERVICE: {}", e.status(), id, e.getMessage());
            throw new RoomServiceUnavailableException("Resource Service returned error status: " + e.status(), e);
        } catch (Exception e) {
            log.error("Unexpected failure fetching room ID {} from RESOURCE-SERVICE: {}", id, e.getMessage(), e);
            throw new RoomServiceUnavailableException("Unexpected error communicating with Resource Service", e);
        }
    }
}
