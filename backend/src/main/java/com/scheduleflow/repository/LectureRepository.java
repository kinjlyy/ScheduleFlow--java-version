package com.scheduleflow.repository;

import com.scheduleflow.model.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {
    List<Lecture> findByTimetableId(Long timetableId);
    List<Lecture> findByTimetableIdAndSectionId(Long timetableId, String sectionId);
    List<Lecture> findByTimetableIdAndTeacherId(Long timetableId, String teacherId);
    List<Lecture> findByTimetableIdAndRoomId(Long timetableId, Long roomId);
    List<Lecture> findByTimetableIdAndDay(Long timetableId, String day);
}
