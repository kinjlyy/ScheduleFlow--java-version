package com.scheduleflow.event.service;

import com.scheduleflow.event.client.ResourceServiceClient;
import com.scheduleflow.event.dto.*;
import com.scheduleflow.event.entity.Event;
import com.scheduleflow.event.enums.EventStatus;
import com.scheduleflow.event.enums.EventType;
import com.scheduleflow.event.enums.LocationType;
import com.scheduleflow.event.exception.ReservationConflictException;
import com.scheduleflow.event.exception.ResourceNotFoundException;
import com.scheduleflow.event.exception.ValidationException;
import com.scheduleflow.event.mapper.EventMapper;
import com.scheduleflow.event.repository.EventRepository;
import com.scheduleflow.event.service.impl.EventServiceImpl;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Spy
    private EventMapper eventMapper;

    @Mock
    private ResourceServiceClient resourceServiceClient;

    @InjectMocks
    private EventServiceImpl eventService;

    private RoomResponse activeRoom;
    private RoomResponse inactiveRoom;

    @BeforeEach
    void setUp() {
        activeRoom = new RoomResponse();
        activeRoom.setId(101L);
        activeRoom.setRoomNumber("CS-101");
        activeRoom.setMaximumCapacity(60);
        activeRoom.setRoomType("CLASSROOM");
        activeRoom.setHasProjector(true);
        activeRoom.setActive(true);

        inactiveRoom = new RoomResponse();
        inactiveRoom.setId(102L);
        inactiveRoom.setRoomNumber("CS-102");
        inactiveRoom.setMaximumCapacity(30);
        inactiveRoom.setRoomType("CLASSROOM");
        inactiveRoom.setActive(false);
    }

    private CreateReservationRequest buildRequest(Long locationId, LocalDate date, int startPeriod, int endPeriod) {
        CreateReservationRequest request = new CreateReservationRequest();
        request.setTitle("Faculty Meeting");
        request.setDescription("Quarterly review");
        request.setEventType(EventType.MEETING);
        request.setDate(date);
        request.setStartPeriod(startPeriod);
        request.setEndPeriod(endPeriod);
        request.setLocationId(locationId);
        request.setLocationType(LocationType.CLASSROOM);
        request.setOrganizer("Dr. Smith");
        request.setCreatedBy("admin");
        return request;
    }

    @Test
    @DisplayName("1. Successful room reservation creates event and returns full response")
    void reserveRoom_success() {
        LocalDate date = LocalDate.now().plusDays(1);
        CreateReservationRequest request = buildRequest(101L, date, 2, 4);

        when(resourceServiceClient.getRoomById(101L)).thenReturn(activeRoom);
        when(eventRepository.existsConflictingReservation(
                eq(101L), eq(date), eq(2), eq(4),
                eq(EventCategory.ROOM_RESERVATION), eq(EventStatus.CANCELLED), isNull()))
                .thenReturn(false);

        Event savedEvent = new Event();
        savedEvent.setId(1L);
        savedEvent.setTitle(request.getTitle());
        savedEvent.setEventCategory(EventCategory.ROOM_RESERVATION);
        savedEvent.setStatus(EventStatus.SCHEDULED);
        savedEvent.setDate(date);
        savedEvent.setStartPeriod(2);
        savedEvent.setEndPeriod(4);
        savedEvent.setLocationId(101L);
        savedEvent.setOrganizer("Dr. Smith");

        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        ReservationResponse response = eventService.reserveRoom(request);

        assertNotNull(response);
        assertNotNull(response.getEvent());
        assertEquals("Faculty Meeting", response.getEvent().getTitle());
        assertEquals(EventCategory.ROOM_RESERVATION, response.getEvent().getEventCategory());
        assertEquals(EventStatus.SCHEDULED, response.getEvent().getStatus());
        assertNotNull(response.getRoomDetails());
        assertEquals("CS-101", response.getRoomDetails().getRoomNumber());
        assertTrue(response.getMessage().contains("CS-101"));

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    @DisplayName("2. Overlapping reservation throws ReservationConflictException (409)")
    void reserveRoom_overlap_rejected() {
        LocalDate date = LocalDate.now().plusDays(1);
        CreateReservationRequest request = buildRequest(101L, date, 3, 5); // Overlaps with P2-P4

        when(resourceServiceClient.getRoomById(101L)).thenReturn(activeRoom);
        when(eventRepository.existsConflictingReservation(
                eq(101L), eq(date), eq(3), eq(5),
                eq(EventCategory.ROOM_RESERVATION), eq(EventStatus.CANCELLED), isNull()))
                .thenReturn(true);

        assertThrows(ReservationConflictException.class, () -> eventService.reserveRoom(request));
        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("3. Same slot on different room succeeds")
    void reserveRoom_differentRoom_succeeds() {
        LocalDate date = LocalDate.now().plusDays(1);
        CreateReservationRequest request = buildRequest(103L, date, 2, 4);

        RoomResponse otherRoom = new RoomResponse();
        otherRoom.setId(103L);
        otherRoom.setRoomNumber("CS-103");
        otherRoom.setActive(true);

        when(resourceServiceClient.getRoomById(103L)).thenReturn(otherRoom);
        when(eventRepository.existsConflictingReservation(
                eq(103L), eq(date), eq(2), eq(4),
                eq(EventCategory.ROOM_RESERVATION), eq(EventStatus.CANCELLED), isNull()))
                .thenReturn(false);

        Event savedEvent = new Event();
        savedEvent.setId(2L);
        savedEvent.setTitle(request.getTitle());
        savedEvent.setLocationId(103L);
        savedEvent.setDate(date);

        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        ReservationResponse response = eventService.reserveRoom(request);
        assertNotNull(response);
        assertEquals("CS-103", response.getRoomDetails().getRoomNumber());
    }

    @Test
    @DisplayName("4. Same room on different date succeeds")
    void reserveRoom_sameRoomDifferentDate_succeeds() {
        LocalDate date1 = LocalDate.now().plusDays(1);
        LocalDate date2 = LocalDate.now().plusDays(2);
        CreateReservationRequest request = buildRequest(101L, date2, 2, 4);

        when(resourceServiceClient.getRoomById(101L)).thenReturn(activeRoom);
        when(eventRepository.existsConflictingReservation(
                eq(101L), eq(date2), eq(2), eq(4),
                eq(EventCategory.ROOM_RESERVATION), eq(EventStatus.CANCELLED), isNull()))
                .thenReturn(false);

        Event savedEvent = new Event();
        savedEvent.setId(3L);
        savedEvent.setTitle(request.getTitle());
        savedEvent.setLocationId(101L);
        savedEvent.setDate(date2);

        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        ReservationResponse response = eventService.reserveRoom(request);
        assertNotNull(response);
        assertEquals(date2, response.getEvent().getDate());
    }

    @Test
    @DisplayName("5. Adjacent periods (e.g. P5-P6 after P2-P4) succeed")
    void reserveRoom_adjacentPeriods_succeeds() {
        LocalDate date = LocalDate.now().plusDays(1);
        CreateReservationRequest request = buildRequest(101L, date, 5, 6);

        when(resourceServiceClient.getRoomById(101L)).thenReturn(activeRoom);
        when(eventRepository.existsConflictingReservation(
                eq(101L), eq(date), eq(5), eq(6),
                eq(EventCategory.ROOM_RESERVATION), eq(EventStatus.CANCELLED), isNull()))
                .thenReturn(false);

        Event savedEvent = new Event();
        savedEvent.setId(4L);
        savedEvent.setLocationId(101L);
        savedEvent.setDate(date);
        savedEvent.setStartPeriod(5);
        savedEvent.setEndPeriod(6);

        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        ReservationResponse response = eventService.reserveRoom(request);
        assertNotNull(response);
    }

    @Test
    @DisplayName("6. Reservation succeeds when previous booking was CANCELLED")
    void reserveRoom_sameRoomSameDateCancelled_succeeds() {
        LocalDate date = LocalDate.now().plusDays(1);
        CreateReservationRequest request = buildRequest(101L, date, 2, 4);

        when(resourceServiceClient.getRoomById(101L)).thenReturn(activeRoom);
        // Excludes CANCELLED status in query check -> returns false (no active conflict)
        when(eventRepository.existsConflictingReservation(
                eq(101L), eq(date), eq(2), eq(4),
                eq(EventCategory.ROOM_RESERVATION), eq(EventStatus.CANCELLED), isNull()))
                .thenReturn(false);

        Event savedEvent = new Event();
        savedEvent.setId(5L);
        savedEvent.setLocationId(101L);
        savedEvent.setDate(date);

        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        ReservationResponse response = eventService.reserveRoom(request);
        assertNotNull(response);
    }

    @Test
    @DisplayName("7. Reserving an inactive room throws ValidationException (400)")
    void reserveRoom_inactiveRoom_rejected() {
        LocalDate date = LocalDate.now().plusDays(1);
        CreateReservationRequest request = buildRequest(102L, date, 2, 4);

        when(resourceServiceClient.getRoomById(102L)).thenReturn(inactiveRoom);

        ValidationException ex = assertThrows(ValidationException.class, () -> eventService.reserveRoom(request));
        assertTrue(ex.getMessage().contains("inactive"));
        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("8. Non-existent room in Resource Service throws ResourceNotFoundException (404)")
    void reserveRoom_roomNotFound_throws404() {
        LocalDate date = LocalDate.now().plusDays(1);
        CreateReservationRequest request = buildRequest(999L, date, 2, 4);

        Request dummyRequest = Request.create(Request.HttpMethod.GET, "/api/rooms/999", new HashMap<>(), null, new RequestTemplate());
        when(resourceServiceClient.getRoomById(999L)).thenThrow(new FeignException.NotFound("Not Found", dummyRequest, null, null));

        assertThrows(ResourceNotFoundException.class, () -> eventService.reserveRoom(request));
    }

    @Test
    @DisplayName("9. Invalid period (startPeriod > endPeriod) throws ValidationException")
    void reserveRoom_invalidPeriod_throwsValidation() {
        LocalDate date = LocalDate.now().plusDays(1);
        CreateReservationRequest request = buildRequest(101L, date, 5, 2); // invalid: 5 > 2

        when(resourceServiceClient.getRoomById(101L)).thenReturn(activeRoom);

        ValidationException ex = assertThrows(ValidationException.class, () -> eventService.reserveRoom(request));
        assertTrue(ex.getMessage().contains("End period"));
    }

    @Test
    @DisplayName("10. Null locationId throws ValidationException")
    void reserveRoom_nullLocationId_throwsValidation() {
        LocalDate date = LocalDate.now().plusDays(1);
        CreateReservationRequest request = buildRequest(null, date, 2, 4);

        ValidationException ex = assertThrows(ValidationException.class, () -> eventService.reserveRoom(request));
        assertTrue(ex.getMessage().contains("locationId"));
    }

    @Test
    @DisplayName("11. Cancel reservation sets status to CANCELLED (soft delete)")
    void cancelReservation_success() {
        Event event = new Event();
        event.setId(10L);
        event.setEventCategory(EventCategory.ROOM_RESERVATION);
        event.setStatus(EventStatus.SCHEDULED);

        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        eventService.cancelReservation(10L);

        assertEquals(EventStatus.CANCELLED, event.getStatus());
        verify(eventRepository).save(event);
    }

    @Test
    @DisplayName("12. Cancelling an already cancelled reservation throws ValidationException")
    void cancelReservation_alreadyCancelled_throws() {
        Event event = new Event();
        event.setId(10L);
        event.setEventCategory(EventCategory.ROOM_RESERVATION);
        event.setStatus(EventStatus.CANCELLED);

        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        ValidationException ex = assertThrows(ValidationException.class, () -> eventService.cancelReservation(10L));
        assertTrue(ex.getMessage().contains("already cancelled"));
    }

    @Test
    @DisplayName("13. Check availability partitions rooms and includes full-day occupied periods")
    void checkAvailability_success() {
        LocalDate date = LocalDate.now().plusDays(1);

        when(resourceServiceClient.getActiveRooms()).thenReturn(List.of(activeRoom));

        Event booking = new Event();
        booking.setId(20L);
        booking.setTitle("Seminar");
        booking.setLocationId(101L);
        booking.setDate(date);
        booking.setStartPeriod(2);
        booking.setEndPeriod(4);
        booking.setStatus(EventStatus.SCHEDULED);

        // Stub the multi-source date-based query used by checkAvailability
        when(eventRepository.findByDateAndStatusNot(date, EventStatus.CANCELLED))
                .thenReturn(List.of(booking));

        // Query slot P3-P5 conflicts with P2-P4
        AvailabilityResponse response = eventService.checkAvailability(date, 3, 5);

        assertNotNull(response);
        assertEquals(date, response.getDate());
        assertEquals(0, response.getAvailableRooms().size());
        assertEquals(1, response.getReservedRooms().size());

        RoomAvailabilityInfo reservedRoom = response.getReservedRooms().get(0);
        assertEquals("CS-101", reservedRoom.getRoom().getRoomNumber());
        assertFalse(reservedRoom.isAvailableInRequestedSlot());
        assertEquals(1, reservedRoom.getOccupiedPeriods().size());
        assertEquals("Seminar", reservedRoom.getOccupiedPeriods().get(0).getEventTitle());
    }
}
