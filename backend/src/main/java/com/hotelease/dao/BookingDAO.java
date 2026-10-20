package com.hotelease.dao;

import com.hotelease.model.Booking;
import com.hotelease.repository.BookingRepository;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class BookingDAO {

    private final BookingRepository bookingRepository;

    public BookingDAO(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public Long saveBooking(Booking booking) throws SQLException {
        Booking saved = bookingRepository.save(booking);
        return saved.getId();
    }
}
