package com.scheduleflow.controller;

import com.scheduleflow.dto.TimetableDTO;
import com.scheduleflow.service.TimetableService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller dedicated to Timetable Versioning and History Management.
 *
 * Exposes endpoints for querying all timetables, current active timetable, and specific timetable details.
 */
@RestController
@RequestMapping("/api/timetables")
public class TimetableController {

    private final TimetableService timetableService;

    public TimetableController(TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    @GetMapping
    public ResponseEntity<List<TimetableDTO>> getAllTimetables() {
        return ResponseEntity.ok(timetableService.getAllTimetables());
    }

    @GetMapping("/active")
    public ResponseEntity<TimetableDTO> getActiveTimetable() {
        return ResponseEntity.ok(timetableService.getActiveTimetable());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimetableDTO> getTimetableById(@PathVariable Long id) {
        return ResponseEntity.ok(timetableService.getTimetableById(id));
    }
}
