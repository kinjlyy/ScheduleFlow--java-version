package com.scheduleflow.scheduler;

import com.scheduleflow.client.ResourceClient;
import com.scheduleflow.dto.RoomDTO;
import com.scheduleflow.exception.RoomServiceUnavailableException;
import com.scheduleflow.mapper.RoomMapper;
import com.scheduleflow.model.Room;
import com.scheduleflow.model.RoomType;
import feign.FeignException;
import feign.Request;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FeignRoomProviderTest {

    @Mock
    private ResourceClient resourceClient;

    private RoomMapper roomMapper;
    private FeignRoomProvider feignRoomProvider;

    @BeforeEach
    void setUp() {
        roomMapper = new RoomMapper();
        feignRoomProvider = new FeignRoomProvider(resourceClient, roomMapper);
    }

    @Test
    void findAllActiveRooms_shouldFetchAndMapActiveRooms() {
        RoomDTO room1 = new RoomDTO(1L, "R101", 50, RoomType.CLASSROOM, true, true, false, true);
        RoomDTO room2 = new RoomDTO(2L, "R102", 30, RoomType.LABORATORY, false, false, true, false); // inactive

        given(resourceClient.getActiveRooms()).willReturn(List.of(room1, room2));

        List<Room> rooms = feignRoomProvider.findAllActiveRooms();

        assertThat(rooms).hasSize(1);
        assertThat(rooms.get(0).getRoomNumber()).isEqualTo("R101");
        assertThat(rooms.get(0).getMaximumCapacity()).isEqualTo(50);
        assertThat(rooms.get(0).getRoomType()).isEqualTo(RoomType.CLASSROOM);
    }

    @Test
    void findAllActiveRooms_shouldThrowRoomServiceUnavailableExceptionOnFeignException() {
        Request request = Request.create(Request.HttpMethod.GET, "/api/rooms/active", Collections.emptyMap(), null, null, null);
        given(resourceClient.getActiveRooms()).willThrow(new RetryableException(503, "Service Unavailable", Request.HttpMethod.GET, new Date(), request));

        assertThatThrownBy(() -> feignRoomProvider.findAllActiveRooms())
                .isInstanceOf(RoomServiceUnavailableException.class)
                .hasMessageContaining("Resource Service is currently unreachable");
    }

    @Test
    void findRoomById_shouldReturnRoomWhenActive() {
        RoomDTO room = new RoomDTO(10L, "Lab1", 25, RoomType.LABORATORY, true, true, true, true);
        given(resourceClient.getRoom(10L)).willReturn(room);

        Optional<Room> result = feignRoomProvider.findRoomById(10L);

        assertThat(result).isPresent();
        assertThat(result.get().getRoomNumber()).isEqualTo("Lab1");
    }

    @Test
    void findRoomById_shouldReturnEmptyOnNotFound404() {
        Request request = Request.create(Request.HttpMethod.GET, "/api/rooms/99", Collections.emptyMap(), null, null, null);
        given(resourceClient.getRoom(99L)).willThrow(new FeignException.NotFound("Not Found", request, null, null));

        Optional<Room> result = feignRoomProvider.findRoomById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void findRoomById_shouldThrowRoomServiceUnavailableExceptionOnConnectionFailure() {
        Request request = Request.create(Request.HttpMethod.GET, "/api/rooms/1", Collections.emptyMap(), null, null, null);
        given(resourceClient.getRoom(1L)).willThrow(new FeignException.ServiceUnavailable("Service Unavailable", request, null, null));

        assertThatThrownBy(() -> feignRoomProvider.findRoomById(1L))
                .isInstanceOf(RoomServiceUnavailableException.class)
                .hasMessageContaining("Resource Service returned error status");
    }
}
