package com.hotelease.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_code", nullable = false, unique = true, length = 30)
    private String bookingCode;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @ManyToOne
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(name = "room_type", length = 50)
    private String roomTypeString;

    @Column(name = "guest_name", nullable = false, length = 100)
    private String guestName;

    @Column(name = "guest_email", nullable = false, length = 100)
    private String guestEmail;

    @Column(name = "guest_phone", nullable = false, length = 20)
    private String guestPhone;

    @Column(name = "guest_country", length = 50)
    private String guestCountry;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "checkin_date", nullable = false)
    private LocalDate checkinDate;

    @Column(name = "checkout_date", nullable = false)
    private LocalDate checkoutDate;

    @Column(name = "num_rooms", nullable = false)
    private int numRooms = 1;

    @Column(name = "num_guests", nullable = false)
    private int numGuests = 1;

    @Column(name = "num_children")
    private int numChildren = 0;

    @Column(name = "bed_preference", length = 30)
    private String bedPreference;

    @Column(name = "floor_preference", length = 30)
    private String floorPreference;

    @Column(name = "view_preference", length = 30)
    private String viewPreference;

    @Column(name = "smoking_preference", length = 30)
    private String smokingPreference;

    @Column(name = "airport_pickup")
    private boolean airportPickup;

    @Column(name = "breakfast")
    private boolean breakfast;

    @Column(name = "wifi")
    private boolean wifi;

    @Column(name = "parking")
    private boolean parking;

    @Column(name = "spa")
    private boolean spa;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @Column(name = "visit_purpose", length = 50)
    private String visitPurpose;

    @Column(name = "arrival_time", length = 20)
    private String arrivalTime;

    @Column(name = "room_cost", nullable = false)
    private double roomCost;

    @Column(name = "services_cost")
    private double servicesCost;

    @Column(name = "tax_amount")
    private double taxAmount;

    @Column(name = "total_cost", nullable = false)
    private double totalCost;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "PENDING_PAYMENT"; // PENDING_PAYMENT, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED

    @Column(name = "qr_code_token", length = 100)
    private String qrCodeToken;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Booking() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Hotel getHotel() { return hotel; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public String getRoomTypeString() { return roomTypeString; }
    public void setRoomTypeString(String roomTypeString) { this.roomTypeString = roomTypeString; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }

    public String getGuestPhone() { return guestPhone; }
    public void setGuestPhone(String guestPhone) { this.guestPhone = guestPhone; }

    public String getGuestCountry() { return guestCountry; }
    public void setGuestCountry(String guestCountry) { this.guestCountry = guestCountry; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public LocalDate getCheckinDate() { return checkinDate; }
    public void setCheckinDate(LocalDate checkinDate) { this.checkinDate = checkinDate; }

    public LocalDate getCheckoutDate() { return checkoutDate; }
    public void setCheckoutDate(LocalDate checkoutDate) { this.checkoutDate = checkoutDate; }

    public int getNumRooms() { return numRooms; }
    public void setNumRooms(int numRooms) { this.numRooms = numRooms; }

    public int getNumGuests() { return numGuests; }
    public void setNumGuests(int numGuests) { this.numGuests = numGuests; }

    public int getNumChildren() { return numChildren; }
    public void setNumChildren(int numChildren) { this.numChildren = numChildren; }

    public String getBedPreference() { return bedPreference; }
    public void setBedPreference(String bedPreference) { this.bedPreference = bedPreference; }

    public String getFloorPreference() { return floorPreference; }
    public void setFloorPreference(String floorPreference) { this.floorPreference = floorPreference; }

    public String getViewPreference() { return viewPreference; }
    public void setViewPreference(String viewPreference) { this.viewPreference = viewPreference; }

    public String getSmokingPreference() { return smokingPreference; }
    public void setSmokingPreference(String smokingPreference) { this.smokingPreference = smokingPreference; }

    public boolean isAirportPickup() { return airportPickup; }
    public void setAirportPickup(boolean airportPickup) { this.airportPickup = airportPickup; }

    public boolean isBreakfast() { return breakfast; }
    public void setBreakfast(boolean breakfast) { this.breakfast = breakfast; }

    public boolean isWifi() { return wifi; }
    public void setWifi(boolean wifi) { this.wifi = wifi; }

    public boolean isParking() { return parking; }
    public void setParking(boolean parking) { this.parking = parking; }

    public boolean isSpa() { return spa; }
    public void setSpa(boolean spa) { this.spa = spa; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public String getVisitPurpose() { return visitPurpose; }
    public void setVisitPurpose(String visitPurpose) { this.visitPurpose = visitPurpose; }

    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }

    public double getRoomCost() { return roomCost; }
    public void setRoomCost(double roomCost) { this.roomCost = roomCost; }

    public double getServicesCost() { return servicesCost; }
    public void setServicesCost(double servicesCost) { this.servicesCost = servicesCost; }

    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getQrCodeToken() { return qrCodeToken; }
    public void setQrCodeToken(String qrCodeToken) { this.qrCodeToken = qrCodeToken; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
