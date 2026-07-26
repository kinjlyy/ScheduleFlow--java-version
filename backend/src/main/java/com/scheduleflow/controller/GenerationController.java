package com.scheduleflow.controller;

import com.scheduleflow.dto.TimetableRequestDTO;
import com.scheduleflow.dto.TimetableResponseDTO;
import com.scheduleflow.service.TimetableService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller dedicated to Timetable Generation.
 *
 * Exposes endpoint for running graph-coloring scheduling engine and persisting generated timetables.
 */
@RestController
@RequestMapping("/api")
public class GenerationController {

    private final TimetableService timetableService;

    public GenerationController(TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    /**
     * POST /api/generate
     * Generates a clash-free timetable, persists it, and archives previous active versions.
     */
    @PostMapping("/generate")
    public ResponseEntity<TimetableResponseDTO> generateTimetable(@RequestBody TimetableRequestDTO request) {
        TimetableResponseDTO response = timetableService.generateAndPersistTimetable(request);
        return ResponseEntity.ok(response);
    }
}
