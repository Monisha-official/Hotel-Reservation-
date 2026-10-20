package com.hotelease.repository;

import com.hotelease.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    List<RoomType> findByHotelId(Long hotelId);
    Optional<RoomType> findByHotelIdAndNameIgnoreCase(Long hotelId, String name);
    List<RoomType> findByNameIgnoreCase(String name);
}
