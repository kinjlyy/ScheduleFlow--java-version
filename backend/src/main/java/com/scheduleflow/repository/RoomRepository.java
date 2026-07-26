package com.scheduleflow.repository;

import com.scheduleflow.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByRoomNumber(String roomNumber);
    boolean existsByRoomNumberAndIdNot(String roomNumber, Long id);
    List<Room> findByActiveTrueAndMaximumCapacityGreaterThanEqual(int capacity);
}
