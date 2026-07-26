package com.scheduleflow.repository;

import com.scheduleflow.model.Timetable;
import com.scheduleflow.model.TimetableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    List<Timetable> findByStatus(TimetableStatus status);
    Optional<Timetable> findFirstByStatusOrderByCreatedAtDesc(TimetableStatus status);
    List<Timetable> findByStatusOrderByCreatedAtDesc(TimetableStatus status);
    List<Timetable> findAllByOrderByCreatedAtDesc();
}
