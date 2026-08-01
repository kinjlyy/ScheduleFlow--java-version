package com.scheduleflow.event.client;

import com.scheduleflow.event.dto.TimetableExecutionRequest;
import com.scheduleflow.event.dto.TimetableExecutionResultResponse;
import com.scheduleflow.event.dto.TimetableImpactResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * TimetableServiceClient — Feign client for TIMETABLE-SERVICE.
 *
 * <p><strong>Phase 7C Status: Active.</strong>
 * Used by Event Service (orchestrator) to analyze timetable impact and trigger execution.
 *
 * <p>Uses direct URL resolution via {@code TIMETABLE_SERVICE_URL} environment variable,
 * bypassing Eureka service discovery for compatibility with Render Free Tier.
 */
@FeignClient(name = "TIMETABLE-SERVICE", url = "${TIMETABLE_SERVICE_URL:http://localhost:8080}", path = "/api")
public interface TimetableServiceClient {

    /**
     * Read-only Impact Analysis query.
     */
    @GetMapping("/timetables/{timetableId}/impact")
    TimetableImpactResponse getImpactedLectures(
            @PathVariable("timetableId") Long timetableId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("startPeriod") Integer startPeriod,
            @RequestParam("endPeriod") Integer endPeriod,
            @RequestParam(value = "locationId", required = false) Long locationId
    );

    /**
     * Retrieve active timetable version metadata.
     */
    @GetMapping("/timetables/active")
    Map<String, Object> getActiveTimetable();

    /**
     * Retrieve all lectures from active timetable.
     */
    @GetMapping("/timetables/active/lectures")
    List<Map<String, Object>> getActiveLectures();

    /**
     * Returns distinct room IDs occupied for a specific timetable, day, and period range.
     * Periods are 1-indexed. Day is uppercase, e.g. "MONDAY".
     *
     * <p>This is the primary method for deriving room occupancy from lecture data
     * without a separate occupancy table. Used by checkAvailability.
     */
    @GetMapping("/timetables/{timetableId}/occupied-rooms")
    List<Long> getOccupiedRoomIds(
            @PathVariable("timetableId") Long timetableId,
            @RequestParam("day") String day,
            @RequestParam("startPeriod") int startPeriod,
            @RequestParam("endPeriod") int endPeriod
    );

    /**
     * Single orchestration endpoint for executing timetable modifications (reschedule / cancel).
     */
    @PostMapping("/timetables/{timetableId}/event-execution")
    TimetableExecutionResultResponse executeEventImpact(
            @PathVariable("timetableId") Long timetableId,
            @RequestBody TimetableExecutionRequest request
    );
}
