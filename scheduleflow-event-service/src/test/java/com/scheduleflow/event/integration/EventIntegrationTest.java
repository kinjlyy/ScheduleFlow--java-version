package com.scheduleflow.event.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scheduleflow.event.client.ResourceServiceClient;
import com.scheduleflow.event.client.TimetableServiceClient;
import com.scheduleflow.event.dto.*;
import com.scheduleflow.event.enums.EventCategory;
import com.scheduleflow.event.enums.EventStatus;
import com.scheduleflow.event.enums.ExecutionStrategy;
import com.scheduleflow.event.enums.EventType;
import com.scheduleflow.event.enums.LocationType;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EventIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResourceServiceClient resourceServiceClient;

    @MockBean
    private TimetableServiceClient timetableServiceClient;

    private RoomResponse activeRoom;
    private TimetableImpactResponse mockImpact;

    @BeforeEach
    void setUp() {
        activeRoom = new RoomResponse();
        activeRoom.setId(201L);
        activeRoom.setRoomNumber("AUD-01");
        activeRoom.setMaximumCapacity(200);
        activeRoom.setRoomType("AUDITORIUM");
        activeRoom.setHasProjector(true);
        activeRoom.setHasAc(true);
        activeRoom.setActive(true);

        ImpactedLectureResponse lecture = new ImpactedLectureResponse(50L, "MATH101", "Prof. Euler", "Sec-A", "MONDAY", 1, "AUD-01", true);
        mockImpact = new TimetableImpactResponse();
        mockImpact.setTimetableId(1L);
        mockImpact.setAffectedLectures(List.of(lecture));
        mockImpact.setAffectedTeachers(List.of("Prof. Euler"));
        mockImpact.setAffectedSections(List.of("Sec-A"));
        mockImpact.setAffectedRooms(List.of("AUD-01"));
        mockImpact.setAffectedSubjects(List.of("MATH101"));
        mockImpact.setTotalAffectedLectures(1);
        mockImpact.setReschedulableLectures(1);
        mockImpact.setNonReschedulableLectures(0);
        mockImpact.setSummary("1 lecture impacted");
    }

    @Test
    @DisplayName("Integration: POST /api/events/reservations creates reservation successfully")
    void createReservation_integration_success() throws Exception {
        when(resourceServiceClient.getRoomById(201L)).thenReturn(activeRoom);

        CreateReservationRequest request = new CreateReservationRequest();
        request.setTitle("Orientation Ceremony");
        request.setEventType(EventType.SEMINAR);
        request.setDate(LocalDate.now().plusDays(2));
        request.setStartPeriod(1);
        request.setEndPeriod(3);
        request.setLocationId(201L);
        request.setLocationType(LocationType.AUDITORIUM);
        request.setOrganizer("Dean Office");
        request.setCreatedBy("admin");

        mockMvc.perform(post("/api/events/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.event.title").value("Orientation Ceremony"))
                .andExpect(jsonPath("$.event.eventCategory").value("ROOM_RESERVATION"))
                .andExpect(jsonPath("$.roomDetails.roomNumber").value("AUD-01"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Integration: GET /api/events/availability returns availability response")
    void checkAvailability_integration_success() throws Exception {
        when(resourceServiceClient.getActiveRooms()).thenReturn(List.of(activeRoom));

        mockMvc.perform(get("/api/events/availability")
                .param("date", LocalDate.now().plusDays(2).toString())
                .param("startPeriod", "1")
                .param("endPeriod", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableRooms").isArray())
                .andExpect(jsonPath("$.reservedRooms").isArray());
    }

    @Test
    @DisplayName("Integration: POST /api/events/impact-analysis returns 200 with impact details")
    void generateImpactAnalysis_integration_success() throws Exception {
        when(timetableServiceClient.getImpactedLectures(eq(1L), any(), any(), any(), any()))
                .thenReturn(mockImpact);

        ImpactAnalysisRequest request = new ImpactAnalysisRequest();
        request.setTimetableId(1L);
        request.setDate(LocalDate.now().plusDays(3));
        request.setStartPeriod(1);
        request.setEndPeriod(2);

        mockMvc.perform(post("/api/events/impact-analysis")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timetableId").value(1))
                .andExpect(jsonPath("$.status").value("IMPACT_ANALYZED"))
                .andExpect(jsonPath("$.impact.totalAffectedLectures").value(1));
    }

    @Test
    @DisplayName("Integration: Feign 404 from Resource Service surfaces as 404 with structured JSON")
    void feignNotFound_integration_returns404() throws Exception {
        Request dummyRequest = Request.create(Request.HttpMethod.GET, "/api/rooms/999", new HashMap<>(), null, new RequestTemplate());
        when(resourceServiceClient.getRoomById(999L)).thenThrow(new FeignException.NotFound("Room not found", dummyRequest, null, null));

        CreateReservationRequest request = new CreateReservationRequest();
        request.setTitle("Orientation Ceremony");
        request.setEventType(EventType.SEMINAR);
        request.setDate(LocalDate.now().plusDays(2));
        request.setStartPeriod(1);
        request.setEndPeriod(3);
        request.setLocationId(999L);
        request.setOrganizer("Dean Office");

        mockMvc.perform(post("/api/events/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                // Service layer catches FeignException.NotFound and re-throws as ResourceNotFoundException,
                // so GlobalExceptionHandler maps it to "Resource Not Found"
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
