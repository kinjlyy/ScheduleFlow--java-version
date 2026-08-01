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

    /**
     * Returns the distinct room IDs that are occupied (have a lecture) in a specific
     * timetable, on a given day, within the requested period range.
     *
     * <p>Periods are 1-indexed. Example:
     * <pre>GET /api/timetables/20/occupied-rooms?day=MONDAY&amp;startPeriod=3&amp;endPeriod=5</pre>
     * returns all room IDs that have at least one lecture on Monday in periods 3, 4, or 5
     * for timetable #20.
     *
     * <p>Used by EVENT-SERVICE to compute available rooms without a separate occupancy table.
     */
    @GetMapping("/timetables/{id}/occupied-rooms")
    public ResponseEntity<List<Long>> getOccupiedRoomIds(
            @PathVariable Long id,
            @RequestParam String day,
            @RequestParam int startPeriod,
            @RequestParam int endPeriod) {
        return ResponseEntity.ok(timetableService.getOccupiedRoomIds(id, day, startPeriod, endPeriod));
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
