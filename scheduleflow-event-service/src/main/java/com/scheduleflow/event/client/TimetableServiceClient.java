package com.scheduleflow.event.client;

import com.scheduleflow.event.dto.TimetableExecutionRequest;
import com.scheduleflow.event.dto.TimetableExecutionResultResponse;
import com.scheduleflow.event.dto.TimetableImpactResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * TimetableServiceClient — Feign client for TIMETABLE-SERVICE.
 *
 * <p><strong>Phase 7C Status: Active.</strong>
 * Used by Event Service (orchestrator) to analyze timetable impact and trigger execution.
 */
@FeignClient(name = "TIMETABLE-SERVICE", path = "/api/timetables")
public interface TimetableServiceClient {

    /**
     * Read-only Impact Analysis query.
     */
    @GetMapping("/{timetableId}/impact")
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
    @GetMapping("/active")
    Map<String, Object> getActiveTimetable();

    /**
     * Single orchestration endpoint for executing timetable modifications (reschedule / cancel).
     */
    @PostMapping("/{timetableId}/event-execution")
    TimetableExecutionResultResponse executeEventImpact(
            @PathVariable("timetableId") Long timetableId,
            @RequestBody TimetableExecutionRequest request
    );
}
