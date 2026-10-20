package com.hotelease.service;

import com.hotelease.dto.BookingRequestDTO;
import com.hotelease.dto.PriceCalculationRequestDTO;
import com.hotelease.dto.PriceCalculationResponseDTO;
import com.hotelease.model.Booking;
import com.hotelease.model.Hotel;
import com.hotelease.model.Room;
import com.hotelease.model.RoomType;
import com.hotelease.repository.BookingRepository;
import com.hotelease.repository.HotelRepository;
import com.hotelease.repository.RoomRepository;
import com.hotelease.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;

    public BookingService(BookingRepository bookingRepository,
                          HotelRepository hotelRepository,
                          RoomTypeRepository roomTypeRepository,
                          RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.hotelRepository = hotelRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.roomRepository = roomRepository;
    }

    public PriceCalculationResponseDTO calculatePrice(PriceCalculationRequestDTO request) {
        PriceCalculationResponseDTO response = new PriceCalculationResponseDTO();

        LocalDate checkin = LocalDate.parse(request.getCheckinDate());
        LocalDate checkout = LocalDate.parse(request.getCheckoutDate());

        if (!checkout.isAfter(checkin)) {
            response.setAvailable(false);
            response.setMessage("Checkout date must be after checkin date.");
            return response;
        }

        long nights = ChronoUnit.DAYS.between(checkin, checkout);
        if (nights <= 0) nights = 1;

        // Find hotel and room type
        Hotel hotel = null;
        if (request.getHotelId() != null) {
            hotel = hotelRepository.findById(request.getHotelId()).orElse(null);
        }
        if (hotel == null && request.getRoomTypeName() != null) {
            // Find by location or fallback to default hotel
            List<Hotel> hotels = hotelRepository.findAll();
            if (!hotels.isEmpty()) hotel = hotels.get(0);
        }

        RoomType roomType = null;
        if (hotel != null && request.getRoomTypeName() != null) {
            roomType = roomTypeRepository.findByHotelIdAndNameIgnoreCase(hotel.getId(), request.getRoomTypeName())
                    .orElse(null);
        }
        if (roomType == null && hotel != null) {
            List<RoomType> rts = roomTypeRepository.findByHotelId(hotel.getId());
            if (!rts.isEmpty()) roomType = rts.get(0);
        }

        double baseRate = (roomType != null) ? roomType.getBasePrice() : 3500.0;
        int numRooms = Math.max(1, request.getNumRooms());
        int numGuests = Math.max(1, request.getNumGuests());

        // Check DB date-based availability
        int bookedRooms = 0;
        int totalInventory = (roomType != null) ? roomType.getTotalRooms() : 10;
        if (roomType != null) {
            bookedRooms = bookingRepository.countBookedRoomsInDateRange(roomType.getId(), checkin, checkout);
        }
        int availableRooms = Math.max(0, totalInventory - bookedRooms);

        boolean isAvailable = availableRooms >= numRooms;

        // Backend Price Calculation (Source of Truth)
        double roomSubtotal = baseRate * numRooms * nights;

        double servicesSubtotal = 0.0;
        if (request.isBreakfast()) servicesSubtotal += 500.0 * numGuests * nights;
        if (request.isAirportPickup()) servicesSubtotal += 1500.0;
        if (request.isWifi()) servicesSubtotal += 300.0 * nights;
        if (request.isParking()) servicesSubtotal += 200.0 * nights;
        if (request.isSpa()) servicesSubtotal += 2000.0;

        double taxAmount = Math.round((roomSubtotal + servicesSubtotal) * 0.18 * 100.0) / 100.0; // 18% GST
        double totalAmount = Math.round((roomSubtotal + servicesSubtotal + taxAmount) * 100.0) / 100.0;

        response.setBaseRoomRatePerNight(baseRate);
        response.setNumberOfNights(nights);
        response.setNumberOfRooms(numRooms);
        response.setRoomSubtotal(roomSubtotal);
        response.setServicesSubtotal(servicesSubtotal);
        response.setTaxAmount(taxAmount);
        response.setTotalAmount(totalAmount);
        response.setAvailable(isAvailable);
        response.setRemainingAvailableRooms(availableRooms);
        response.setMessage(isAvailable ? "Rooms available for selected dates." : "Requested rooms unavailable for dates.");

        return response;
    }

    @Transactional
    public Booking createBooking(BookingRequestDTO request) {
        LocalDate checkin = LocalDate.parse(request.getCheckinDate());
        LocalDate checkout = LocalDate.parse(request.getCheckoutDate());

        if (!checkout.isAfter(checkin)) {
            throw new IllegalArgumentException("Checkout date must be after check-in date.");
        }
        if (checkin.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past.");
        }

        // Determine Hotel & RoomType
        Hotel hotel = null;
        if (request.getHotelId() != null) {
            hotel = hotelRepository.findById(request.getHotelId()).orElse(null);
        }
        if (hotel == null && request.getLocation() != null) {
            List<Hotel> matchedHotels = hotelRepository.findByCityContainingIgnoreCase(request.getLocation());
            if (!matchedHotels.isEmpty()) hotel = matchedHotels.get(0);
        }
        if (hotel == null) {
            List<Hotel> allHotels = hotelRepository.findAll();
            if (!allHotels.isEmpty()) hotel = allHotels.get(0);
            else throw new IllegalStateException("No hotels available in database.");
        }

        RoomType roomType = null;
        if (request.getRoomType() != null) {
            roomType = roomTypeRepository.findByHotelIdAndNameIgnoreCase(hotel.getId(), request.getRoomType())
                    .orElse(null);
        }
        if (roomType == null) {
            List<RoomType> roomTypes = roomTypeRepository.findByHotelId(hotel.getId());
            if (!roomTypes.isEmpty()) roomType = roomTypes.get(0);
            else throw new IllegalStateException("No room types configured for hotel.");
        }

        // Anti-overbooking DB Check
        int bookedRooms = bookingRepository.countBookedRoomsInDateRange(roomType.getId(), checkin, checkout);
        int availableRooms = roomType.getTotalRooms() - bookedRooms;

        if (availableRooms < request.getNumRooms()) {
            throw new IllegalStateException("Sorry, only " + availableRooms + " room(s) available for selected dates.");
        }

        // Perform backend price calculation
        PriceCalculationRequestDTO priceReq = new PriceCalculationRequestDTO();
        priceReq.setHotelId(hotel.getId());
        priceReq.setRoomTypeName(roomType.getName());
        priceReq.setCheckinDate(request.getCheckinDate());
        priceReq.setCheckoutDate(request.getCheckoutDate());
        priceReq.setNumRooms(request.getNumRooms());
        priceReq.setNumGuests(request.getNumGuests());
        priceReq.setAirportPickup(request.isAirportPickup());
        priceReq.setBreakfast(request.isBreakfast());
        priceReq.setWifi(request.isWifi());
        priceReq.setParking(request.isParking());
        priceReq.setSpa(request.isSpa());

        PriceCalculationResponseDTO priceRes = calculatePrice(priceReq);

        Booking booking = new Booking();
        String bookingCode = "HE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        booking.setBookingCode(bookingCode);
        booking.setHotel(hotel);
        booking.setRoomType(roomType);
        booking.setRoomTypeString(roomType.getName());
        booking.setGuestName(request.getGuestName());
        booking.setGuestEmail(request.getGuestEmail());
        booking.setGuestPhone(request.getGuestPhone());
        booking.setGuestCountry(request.getGuestCountry());
        booking.setLocation(request.getLocation() != null && !request.getLocation().trim().isEmpty() ? request.getLocation() : hotel.getCity());
        booking.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "credit");
        booking.setCheckinDate(checkin);
        booking.setCheckoutDate(checkout);
        booking.setNumRooms(request.getNumRooms());
        booking.setNumGuests(request.getNumGuests());
        booking.setNumChildren(request.getNumChildren());
        booking.setBedPreference(request.getBedPreference());
        booking.setFloorPreference(request.getFloorPreference());
        booking.setViewPreference(request.getViewPreference());
        booking.setSmokingPreference(request.getSmokingPreference());
        booking.setAirportPickup(request.isAirportPickup());
        booking.setBreakfast(request.isBreakfast());
        booking.setWifi(request.isWifi());
        booking.setParking(request.isParking());
        booking.setSpa(request.isSpa());
        booking.setVisitPurpose(request.getVisitPurpose());
        booking.setArrivalTime(request.getArrivalTime());
        booking.setSpecialRequests(request.getSpecialRequests());

        booking.setRoomCost(priceRes.getRoomSubtotal());
        booking.setServicesCost(priceRes.getServicesSubtotal());
        booking.setTaxAmount(priceRes.getTaxAmount());
        booking.setTotalCost(priceRes.getTotalAmount());

        // Status setting
        if ("payathotel".equalsIgnoreCase(request.getPaymentMethod())) {
            booking.setStatus("CONFIRMED");
        } else {
            booking.setStatus("PENDING_PAYMENT");
        }

        booking.setQrCodeToken("QR-" + bookingCode);

        return bookingRepository.save(booking);
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id).orElse(null);
    }

    public Booking getBookingByCode(String bookingCode) {
        return bookingRepository.findByBookingCode(bookingCode).orElse(null);
    }

    public List<Booking> getBookingsByEmail(String email) {
        return bookingRepository.findByGuestEmailOrderByCreatedAtDesc(email);
    }

    @Transactional
    public Booking cancelBooking(Long bookingId, String guestEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));

        if (guestEmail != null && !guestEmail.equalsIgnoreCase(booking.getGuestEmail())) {
            throw new IllegalArgumentException("Guest email verification failed.");
        }

        booking.setStatus("CANCELLED");
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking checkinGuest(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));

        booking.setStatus("CHECKED_IN");

        // Assign physical room to OCCUPIED status if available
        List<Room> rooms = roomRepository.findByRoomTypeIdAndStatus(booking.getRoomType().getId(), "AVAILABLE");
        if (!rooms.isEmpty()) {
            Room r = rooms.get(0);
            r.setStatus("OCCUPIED");
            roomRepository.save(r);
        }

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking checkoutGuest(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));

        booking.setStatus("CHECKED_OUT");

        // Free physical room to CLEANING status
        List<Room> occupiedRooms = roomRepository.findByRoomTypeIdAndStatus(booking.getRoomType().getId(), "OCCUPIED");
        if (!occupiedRooms.isEmpty()) {
            Room r = occupiedRooms.get(0);
            r.setStatus("CLEANING");
            roomRepository.save(r);
        }

        return bookingRepository.save(booking);
    }
}
