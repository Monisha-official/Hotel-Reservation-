package com.hotelease.controller;

import com.hotelease.dto.BookingRequestDTO;
import com.hotelease.dto.PriceCalculationRequestDTO;
import com.hotelease.dto.PriceCalculationResponseDTO;
import com.hotelease.model.Booking;
import com.hotelease.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/calculate-price")
    public ResponseEntity<PriceCalculationResponseDTO> calculatePrice(@RequestBody PriceCalculationRequestDTO request) {
        PriceCalculationResponseDTO response = bookingService.calculatePrice(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createBooking(@RequestBody BookingRequestDTO request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Booking booking = bookingService.createBooking(request);
            response.put("success", true);
            response.put("bookingId", booking.getId());
            response.put("bookingCode", booking.getBookingCode());
            response.put("totalCost", booking.getTotalCost());
            response.put("status", booking.getStatus());
            response.put("qrCodeToken", booking.getQrCodeToken());
            response.put("message", "Booking created successfully!");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable("id") Long id) {
        Booking booking = bookingService.getBookingById(id);
        if (booking != null) return ResponseEntity.ok(booking);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/lookup")
    public ResponseEntity<Booking> getBookingByCode(@RequestParam("code") String code) {
        Booking booking = bookingService.getBookingByCode(code);
        if (booking != null) return ResponseEntity.ok(booking);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<List<Booking>> getMyBookings(@RequestParam("email") String email) {
        List<Booking> bookings = bookingService.getBookingsByEmail(email);
        return ResponseEntity.ok(bookings);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelBooking(@PathVariable("id") Long id,
                                                            @RequestParam(value = "email", required = false) String email) {
        Map<String, Object> response = new HashMap<>();
        try {
            Booking cancelled = bookingService.cancelBooking(id, email);
            response.put("success", true);
            response.put("message", "Booking " + cancelled.getBookingCode() + " has been cancelled.");
            response.put("status", cancelled.getStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/{id}/checkin")
    public ResponseEntity<Map<String, Object>> checkin(@PathVariable("id") Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Booking b = bookingService.checkinGuest(id);
            response.put("success", true);
            response.put("message", "Guest checked in successfully.");
            response.put("status", b.getStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/{id}/checkout")
    public ResponseEntity<Map<String, Object>> checkout(@PathVariable("id") Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Booking b = bookingService.checkoutGuest(id);
            response.put("success", true);
            response.put("message", "Guest checked out successfully.");
            response.put("status", b.getStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
