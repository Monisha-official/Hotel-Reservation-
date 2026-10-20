package com.hotelease.repository;

import com.hotelease.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByRoomTypeId(Long roomTypeId);
    List<Room> findByRoomTypeIdAndStatus(Long roomTypeId, String status);
    long countByRoomTypeIdAndStatus(Long roomTypeId, String status);
}
