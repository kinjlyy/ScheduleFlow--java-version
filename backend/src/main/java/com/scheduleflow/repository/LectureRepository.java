package com.scheduleflow.repository;

import com.scheduleflow.model.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {
    List<Lecture> findByTimetableId(Long timetableId);
    List<Lecture> findByTimetableIdAndSectionId(Long timetableId, String sectionId);
    List<Lecture> findByTimetableIdAndTeacherId(Long timetableId, String teacherId);
    List<Lecture> findByTimetableIdAndRoomId(Long timetableId, Long roomId);
    List<Lecture> findByTimetableIdAndDay(Long timetableId, String day);

    /**
     * Returns the distinct room IDs occupied by a specific timetable on a given day
     * within [startSlot, endSlot] (both 0-indexed, i.e. Period 1 = slot 0).
     *
     * <p>Used by the Event Service to compute free rooms without a separate occupancy table.
     * Equivalent SQL:
     * <pre>
     *   SELECT DISTINCT room_id FROM lectures
     *   WHERE timetable_id = :ttId AND day = :day
     *   AND lecture_slot BETWEEN :startSlot AND :endSlot
     *   AND room_id IS NOT NULL
     * </pre>
     */
    @Query("SELECT DISTINCT l.roomId FROM Lecture l " +
           "WHERE l.timetable.id = :ttId " +
           "AND UPPER(l.day) = UPPER(:day) " +
           "AND l.lectureSlot BETWEEN :startSlot AND :endSlot " +
           "AND l.roomId IS NOT NULL")
    List<Long> findOccupiedRoomIds(
            @Param("ttId")       Long   ttId,
            @Param("day")        String day,
            @Param("startSlot")  int    startSlot,
            @Param("endSlot")    int    endSlot
    );
}
