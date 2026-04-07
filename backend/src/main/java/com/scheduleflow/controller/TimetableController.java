package com.scheduleflow.controller;

import com.scheduleflow.dto.TimetableRequestDTO;
import com.scheduleflow.dto.TimetableResponseDTO;
import com.scheduleflow.service.SchedulerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TimetableController {

    private final SchedulerService schedulerService;

    public TimetableController(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    /**
     * POST /api/generate
     * Accepts sections, constraints, and generates a clash-free timetable.
     */
    @PostMapping("/generate")
    public ResponseEntity<TimetableResponseDTO> generateTimetable(
            @RequestBody TimetableRequestDTO request) {
        TimetableResponseDTO response = schedulerService.generate(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ScheduleFlow API is running");
    }
}
