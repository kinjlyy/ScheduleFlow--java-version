package com.scheduleflow.controller;

import com.scheduleflow.dto.TimetableExecutionRequest;
import com.scheduleflow.dto.TimetableExecutionResultDTO;
import com.scheduleflow.dto.TimetableImpactResponse;
import com.scheduleflow.service.TimetableService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * TimetableImpactController — Controller for Timetable Impact Analysis and Orchestrated Execution.
 *
 * <p>Invoked by EVENT-SERVICE via Feign during Academic Event Scheduling workflows.
 */
@RestController
@RequestMapping("/api/timetables")
public class TimetableImpactController {

    private final TimetableService timetableService;

    public TimetableImpactController(TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    /**
     * Read-only Impact Analysis query.
     */
    @GetMapping("/{timetableId}/impact")
    public ResponseEntity<TimetableImpactResponse> getImpactedLectures(
            @PathVariable("timetableId") Long timetableId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("startPeriod") Integer startPeriod,
            @RequestParam("endPeriod") Integer endPeriod,
            @RequestParam(value = "locationId", required = false) Long locationId) {
        TimetableImpactResponse response = timetableService.calculateImpact(timetableId, date, startPeriod, endPeriod, locationId);
        return ResponseEntity.ok(response);
    }

    /**
     * Single orchestration endpoint for executing timetable modifications (reschedule / cancel).
     */
    @PostMapping("/{timetableId}/event-execution")
    public ResponseEntity<TimetableExecutionResultDTO> executeEventImpact(
            @PathVariable("timetableId") Long timetableId,
            @RequestBody TimetableExecutionRequest request) {
        TimetableExecutionResultDTO result = timetableService.executeEventImpact(timetableId, request);
        return ResponseEntity.ok(result);
    }
}
