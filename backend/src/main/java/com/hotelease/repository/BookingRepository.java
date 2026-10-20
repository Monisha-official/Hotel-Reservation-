package com.hotelease.repository;

import com.hotelease.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingCode(String bookingCode);

    List<Booking> findByGuestEmailOrderByCreatedAtDesc(String guestEmail);

    List<Booking> findByStatus(String status);

    @Query("SELECT COALESCE(SUM(b.numRooms), 0) FROM Booking b " +
           "WHERE b.roomType.id = :roomTypeId " +
           "AND b.status IN ('CONFIRMED', 'CHECKED_IN', 'PENDING_PAYMENT') " +
           "AND b.checkinDate < :checkoutDate " +
           "AND b.checkoutDate > :checkinDate")
    int countBookedRoomsInDateRange(
            @Param("roomTypeId") Long roomTypeId,
            @Param("checkinDate") LocalDate checkinDate,
            @Param("checkoutDate") LocalDate checkoutDate
    );

    @Query("SELECT b FROM Booking b WHERE b.checkinDate = :today AND b.status = 'CONFIRMED'")
    List<Booking> findTodayCheckins(@Param("today") LocalDate today);

    @Query("SELECT b FROM Booking b WHERE b.checkoutDate = :today AND b.status = 'CHECKED_IN'")
    List<Booking> findTodayCheckouts(@Param("today") LocalDate today);

    @Query("SELECT COALESCE(SUM(b.totalCost), 0.0) FROM Booking b WHERE b.status IN ('CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT')")
    double calculateTotalRevenue();
}
