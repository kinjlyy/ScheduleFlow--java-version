package com.scheduleflow.event.service;

import com.scheduleflow.event.client.ResourceServiceClient;
import com.scheduleflow.event.client.TimetableServiceClient;
import com.scheduleflow.event.dto.*;
import com.scheduleflow.event.entity.Event;
import com.scheduleflow.event.enums.EventCategory;
import com.scheduleflow.event.enums.EventStatus;
import com.scheduleflow.event.enums.ExecutionStrategy;
import com.scheduleflow.event.enums.EventType;
import com.scheduleflow.event.enums.LocationType;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcademicEventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Spy
    private EventMapper eventMapper;

    @Mock
    private ResourceServiceClient resourceServiceClient;

    @Mock
    private TimetableServiceClient timetableServiceClient;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event academicEvent;
    private TimetableImpactResponse mockImpact;

    @BeforeEach
    void setUp() {
        academicEvent = new Event();
        academicEvent.setId(100L);
        academicEvent.setTitle("Annual Sports Meet");
        academicEvent.setDescription("Campus wide event");
        academicEvent.setEventType(EventType.SPORTS);
        academicEvent.setEventCategory(EventCategory.TIMETABLE_EVENT);
        academicEvent.setTimetableId(1L);
        academicEvent.setDate(LocalDate.now().plusDays(5));
        academicEvent.setStartPeriod(1);
        academicEvent.setEndPeriod(4);
        academicEvent.setLocationId(101L);
        academicEvent.setLocationType(LocationType.CLASSROOM);
        academicEvent.setStatus(EventStatus.DRAFT);
        academicEvent.setOrganizer("Sports Committee");
        academicEvent.setCreatedBy("admin");

        ImpactedLectureResponse lecture1 = new ImpactedLectureResponse(10L, "CS101", "Dr. Alan", "Sec-A", "MONDAY", 1, "101", true);
        ImpactedLectureResponse lecture2 = new ImpactedLectureResponse(11L, "CS102", "Prof. Grace", "Sec-B", "MONDAY", 2, "101", true);
        ImpactedLectureResponse labLecture = new ImpactedLectureResponse(12L, "CS103L", "Dr. Alan", "Sec-A", "MONDAY", 3, "101", false);

        mockImpact = new TimetableImpactResponse();
        mockImpact.setTimetableId(1L);
        mockImpact.setAffectedLectures(List.of(lecture1, lecture2, labLecture));
        mockImpact.setAffectedTeachers(List.of("Dr. Alan", "Prof. Grace"));
        mockImpact.setAffectedSections(List.of("Sec-A", "Sec-B"));
        mockImpact.setAffectedRooms(List.of("101"));
        mockImpact.setAffectedSubjects(List.of("CS101", "CS102", "CS103L"));
        mockImpact.setTotalAffectedLectures(3);
        mockImpact.setReschedulableLectures(2);
        mockImpact.setNonReschedulableLectures(1);
        mockImpact.setConflicts(List.of("3 lectures conflict"));
        mockImpact.setSummary("Impact Analysis: 3 lectures affected (2 reschedulable, 1 non-reschedulable)");
    }

    @Test
    @DisplayName("1. Successful Impact Analysis calculates impact and updates event status to IMPACT_ANALYZED")
    void generateImpactAnalysis_success() {
        when(eventRepository.findById(100L)).thenReturn(Optional.of(academicEvent));
        when(timetableServiceClient.getImpactedLectures(eq(1L), any(), anyInt(), anyInt(), any()))
                .thenReturn(mockImpact);

        ImpactAnalysisRequest request = new ImpactAnalysisRequest();
        request.setEventId(100L);

        ImpactAnalysisResponse response = eventService.generateImpactAnalysis(request);

        assertNotNull(response);
        assertEquals(100L, response.getEventId());
        assertEquals(EventStatus.IMPACT_ANALYZED, response.getStatus());
        assertEquals(3, response.getImpact().getTotalAffectedLectures());
        assertEquals(EventStatus.IMPACT_ANALYZED, academicEvent.getStatus());

        verify(eventRepository).save(academicEvent);
    }

    @Test
    @DisplayName("2. Successful Execution Plan generation calculates reschedule vs cancel lists and updates status to READY_FOR_EXECUTION")
    void generateExecutionPlan_success() {
        when(eventRepository.findById(100L)).thenReturn(Optional.of(academicEvent));
        when(timetableServiceClient.getImpactedLectures(eq(1L), any(), anyInt(), anyInt(), any()))
                .thenReturn(mockImpact);

        ExecutionPlanRequest request = new ExecutionPlanRequest(100L, ExecutionStrategy.RESCHEDULE_AND_CANCEL);

        ExecutionPlanResponse response = eventService.generateExecutionPlan(request);

        assertNotNull(response);
        assertEquals(100L, response.getEventId());
        assertEquals(ExecutionStrategy.RESCHEDULE_AND_CANCEL, response.getExecutionStrategy());
        assertEquals(2, response.getLecturesToReschedule().size());
        assertEquals(1, response.getLecturesToCancel().size()); // Lab lecture is non-reschedulable
        assertEquals(EventStatus.READY_FOR_EXECUTION, response.getStatus());
        assertEquals(EventStatus.READY_FOR_EXECUTION, academicEvent.getStatus());

        verify(eventRepository, times(2)).save(academicEvent);
    }

    @Test
    @DisplayName("3. Execution with RESCHEDULE_AND_CANCEL calls Timetable Service and updates Event to COMPLETED")
    void executeStrategy_rescheduleAndCancel_success() {
        when(eventRepository.findById(100L)).thenReturn(Optional.of(academicEvent));
        when(timetableServiceClient.getImpactedLectures(eq(1L), any(), anyInt(), anyInt(), any()))
                .thenReturn(mockImpact);

        TimetableExecutionResultResponse ttResult = new TimetableExecutionResultResponse();
        ttResult.setStatus("SUCCESS");
        ttResult.setSummary("Rescheduled 2 lectures, cancelled 1 lecture");
        ttResult.setRescheduledCount(2);
        ttResult.setCancelledCount(1);
        ttResult.setRescheduledLectureIds(List.of(10L, 11L));
        ttResult.setCancelledLectureIds(List.of(12L));
        ttResult.setWarnings(List.of("Lab lecture cancelled"));

        when(timetableServiceClient.executeEventImpact(eq(1L), any())).thenReturn(ttResult);

        ExecutionRequest request = new ExecutionRequest(ExecutionStrategy.RESCHEDULE_AND_CANCEL, "admin");

        ExecutionResponse response = eventService.executeStrategy(100L, request);

        assertNotNull(response);
        assertEquals(EventStatus.COMPLETED, response.getStatus());
        assertEquals(2, response.getRescheduledCount());
        assertEquals(1, response.getCancelledCount());
        assertEquals(EventStatus.COMPLETED, academicEvent.getStatus());
        assertNotNull(academicEvent.getExecutionCompletedAt());
        assertEquals("admin", academicEvent.getExecutedBy());
    }

    @Test
    @DisplayName("4. Execution with CANCEL_ALL cancels all affected lectures")
    void executeStrategy_cancelAll_success() {
        when(eventRepository.findById(100L)).thenReturn(Optional.of(academicEvent));
        when(timetableServiceClient.getImpactedLectures(eq(1L), any(), anyInt(), anyInt(), any()))
                .thenReturn(mockImpact);

        TimetableExecutionResultResponse ttResult = new TimetableExecutionResultResponse();
        ttResult.setStatus("SUCCESS");
        ttResult.setSummary("Cancelled all 3 lectures");
        ttResult.setRescheduledCount(0);
        ttResult.setCancelledCount(3);
        ttResult.setCancelledLectureIds(List.of(10L, 11L, 12L));

        when(timetableServiceClient.executeEventImpact(eq(1L), any())).thenReturn(ttResult);

        ExecutionRequest request = new ExecutionRequest(ExecutionStrategy.CANCEL_ALL, "admin");

        ExecutionResponse response = eventService.executeStrategy(100L, request);

        assertNotNull(response);
        assertEquals(EventStatus.COMPLETED, response.getStatus());
        assertEquals(0, response.getRescheduledCount());
        assertEquals(3, response.getCancelledCount());
    }

    @Test
    @DisplayName("5. Impact analysis with zero affected lectures handles gracefully")
    void generateImpactAnalysis_zeroAffectedLectures() {
        TimetableImpactResponse emptyImpact = new TimetableImpactResponse();
        emptyImpact.setTimetableId(1L);
        emptyImpact.setAffectedLectures(List.of());
        emptyImpact.setTotalAffectedLectures(0);
        emptyImpact.setSummary("Impact Analysis: 0 lectures affected");

        when(eventRepository.findById(100L)).thenReturn(Optional.of(academicEvent));
        when(timetableServiceClient.getImpactedLectures(eq(1L), any(), anyInt(), anyInt(), any()))
                .thenReturn(emptyImpact);

        ImpactAnalysisRequest request = new ImpactAnalysisRequest();
        request.setEventId(100L);

        ImpactAnalysisResponse response = eventService.generateImpactAnalysis(request);

        assertNotNull(response);
        assertEquals(0, response.getImpact().getTotalAffectedLectures());
    }

    @Test
    @DisplayName("6. Execution plan where all lectures are reschedulable")
    void generateExecutionPlan_allLecturesReschedulable() {
        ImpactedLectureResponse lecture1 = new ImpactedLectureResponse(10L, "CS101", "Dr. Alan", "Sec-A", "MONDAY", 1, "101", true);
        ImpactedLectureResponse lecture2 = new ImpactedLectureResponse(11L, "CS102", "Prof. Grace", "Sec-B", "MONDAY", 2, "101", true);

        TimetableImpactResponse allTheoryImpact = new TimetableImpactResponse();
        allTheoryImpact.setTimetableId(1L);
        allTheoryImpact.setAffectedLectures(List.of(lecture1, lecture2));
        allTheoryImpact.setTotalAffectedLectures(2);

        when(eventRepository.findById(100L)).thenReturn(Optional.of(academicEvent));
        when(timetableServiceClient.getImpactedLectures(eq(1L), any(), anyInt(), anyInt(), any()))
                .thenReturn(allTheoryImpact);

        ExecutionPlanRequest request = new ExecutionPlanRequest(100L, ExecutionStrategy.RESCHEDULE_AND_CANCEL);

        ExecutionPlanResponse response = eventService.generateExecutionPlan(request);

        assertEquals(2, response.getLecturesToReschedule().size());
        assertEquals(0, response.getLecturesToCancel().size());
    }

    @Test
    @DisplayName("7. Execution plan where no lectures are reschedulable (all labs)")
    void generateExecutionPlan_noneReschedulable() {
        ImpactedLectureResponse lab1 = new ImpactedLectureResponse(10L, "CS101L", "Dr. Alan", "Sec-A", "MONDAY", 1, "101", false);
        ImpactedLectureResponse lab2 = new ImpactedLectureResponse(11L, "CS102L", "Prof. Grace", "Sec-B", "MONDAY", 2, "101", false);

        TimetableImpactResponse allLabImpact = new TimetableImpactResponse();
        allLabImpact.setTimetableId(1L);
        allLabImpact.setAffectedLectures(List.of(lab1, lab2));
        allLabImpact.setTotalAffectedLectures(2);

        when(eventRepository.findById(100L)).thenReturn(Optional.of(academicEvent));
        when(timetableServiceClient.getImpactedLectures(eq(1L), any(), anyInt(), anyInt(), any()))
                .thenReturn(allLabImpact);

        ExecutionPlanRequest request = new ExecutionPlanRequest(100L, ExecutionStrategy.RESCHEDULE_AND_CANCEL);

        ExecutionPlanResponse response = eventService.generateExecutionPlan(request);

        assertEquals(0, response.getLecturesToReschedule().size());
        assertEquals(2, response.getLecturesToCancel().size());
    }

    @Test
    @DisplayName("8. Timetable Service failure during execution sets Event status to FAILED and records reason without crashing")
    void executeStrategy_timetableServiceFailure_marksFailed() {
        when(eventRepository.findById(100L)).thenReturn(Optional.of(academicEvent));
        when(timetableServiceClient.getImpactedLectures(eq(1L), any(), anyInt(), anyInt(), any()))
                .thenReturn(mockImpact);

        Request dummyRequest = Request.create(Request.HttpMethod.POST, "/api/timetables/1/event-execution", new HashMap<>(), null, new RequestTemplate());
        when(timetableServiceClient.executeEventImpact(eq(1L), any()))
                .thenThrow(new FeignException.InternalServerError("Internal Server Error in Timetable Service", dummyRequest, null, null));

        ExecutionRequest request = new ExecutionRequest(ExecutionStrategy.RESCHEDULE_AND_CANCEL, "admin");

        ExecutionResponse response = eventService.executeStrategy(100L, request);

        assertNotNull(response);
        assertEquals(EventStatus.FAILED, response.getStatus());
        assertTrue(response.getSummary().contains("Execution failed"));
        assertEquals(EventStatus.FAILED, academicEvent.getStatus());
        assertTrue(academicEvent.getExecutionSummary().contains("Internal Server Error"));
    }

    @Test
    @DisplayName("9. Get execution history retrieves metadata stored on Event entity")
    void getExecutionHistory_success() {
        academicEvent.setStatus(EventStatus.COMPLETED);
        academicEvent.setExecutionStrategy(ExecutionStrategy.RESCHEDULE_AND_CANCEL);
        academicEvent.setExecutedBy("admin");
        academicEvent.setExecutionStartedAt(java.time.LocalDateTime.now().minusMinutes(5));
        academicEvent.setExecutionCompletedAt(java.time.LocalDateTime.now());
        academicEvent.setExecutionSummary("Rescheduled 2, cancelled 1");
        academicEvent.setExecutionResult("Rescheduled: 2, Cancelled: 1");

        when(eventRepository.findById(100L)).thenReturn(Optional.of(academicEvent));

        ExecutionHistoryResponse history = eventService.getExecutionHistory(100L);

        assertNotNull(history);
        assertEquals(100L, history.getEventId());
        assertEquals(EventStatus.COMPLETED, history.getStatus());
        assertEquals(ExecutionStrategy.RESCHEDULE_AND_CANCEL, history.getExecutionStrategy());
        assertEquals("admin", history.getExecutedBy());
        assertEquals("Rescheduled 2, cancelled 1", history.getExecutionSummary());
    }
}
