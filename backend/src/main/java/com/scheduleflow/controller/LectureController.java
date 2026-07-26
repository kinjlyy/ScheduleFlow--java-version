package com.scheduleflow.controller;

import com.scheduleflow.dto.LectureDTO;
import com.scheduleflow.service.TimetableService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller dedicated to Lecture Query APIs.
 *
 * Provides endpoints for retrieving lectures by section, teacher, room, or day,
 * both for specific timetable versions and for the currently active timetable.
 */
@RestController
@RequestMapping("/api")
public class LectureController {

    private final TimetableService timetableService;

    public LectureController(TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    // ── Versioned Lecture Query APIs ─────────────────────────────────────────

    @GetMapping("/timetables/{id}/lectures")
    public ResponseEntity<List<LectureDTO>> getLecturesByTimetableId(@PathVariable Long id) {
        return ResponseEntity.ok(timetableService.getLecturesByTimetableId(id));
    }

    @GetMapping("/timetables/{id}/lectures/section/{sectionId}")
    public ResponseEntity<List<LectureDTO>> getLecturesBySection(
            @PathVariable Long id, @PathVariable String sectionId) {
        return ResponseEntity.ok(timetableService.getLecturesBySection(id, sectionId));
    }

    @GetMapping("/timetables/{id}/lectures/teacher/{teacherId}")
    public ResponseEntity<List<LectureDTO>> getLecturesByTeacher(
            @PathVariable Long id, @PathVariable String teacherId) {
        return ResponseEntity.ok(timetableService.getLecturesByTeacher(id, teacherId));
    }

    @GetMapping("/timetables/{id}/lectures/room/{roomId}")
    public ResponseEntity<List<LectureDTO>> getLecturesByRoom(
            @PathVariable Long id, @PathVariable Long roomId) {
        return ResponseEntity.ok(timetableService.getLecturesByRoom(id, roomId));
    }

    @GetMapping("/timetables/{id}/lectures/day/{day}")
    public ResponseEntity<List<LectureDTO>> getLecturesByDay(
            @PathVariable Long id, @PathVariable String day) {
        return ResponseEntity.ok(timetableService.getLecturesByDay(id, day));
    }

    // ── Active Timetable Convenience Queries ─────────────────────────────────

    @GetMapping("/timetables/active/lectures")
    public ResponseEntity<List<LectureDTO>> getActiveLectures() {
        return ResponseEntity.ok(timetableService.getActiveLectures());
    }

    @GetMapping("/timetables/active/lectures/section/{sectionId}")
    public ResponseEntity<List<LectureDTO>> getActiveLecturesBySection(@PathVariable String sectionId) {
        return ResponseEntity.ok(timetableService.getActiveLecturesBySection(sectionId));
    }

    @GetMapping("/timetables/active/lectures/teacher/{teacherId}")
    public ResponseEntity<List<LectureDTO>> getActiveLecturesByTeacher(@PathVariable String teacherId) {
        return ResponseEntity.ok(timetableService.getActiveLecturesByTeacher(teacherId));
    }

    @GetMapping("/timetables/active/lectures/room/{roomId}")
    public ResponseEntity<List<LectureDTO>> getActiveLecturesByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(timetableService.getActiveLecturesByRoom(roomId));
    }

    @GetMapping("/timetables/active/lectures/day/{day}")
    public ResponseEntity<List<LectureDTO>> getActiveLecturesByDay(@PathVariable String day) {
        return ResponseEntity.ok(timetableService.getActiveLecturesByDay(day));
    }
}
