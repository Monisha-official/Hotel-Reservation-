package com.hotelease.dto;

public class PriceCalculationRequestDTO {
    private Long hotelId;
    private String roomTypeName; // or roomTypeId
    private String checkinDate;
    private String checkoutDate;
    private int numRooms = 1;
    private int numGuests = 1;
    private boolean airportPickup;
    private boolean breakfast;
    private boolean wifi;
    private boolean parking;
    private boolean spa;

    public PriceCalculationRequestDTO() {}

    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }

    public String getRoomTypeName() { return roomTypeName; }
    public void setRoomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; }

    public String getCheckinDate() { return checkinDate; }
    public void setCheckinDate(String checkinDate) { this.checkinDate = checkinDate; }

    public String getCheckoutDate() { return checkoutDate; }
    public void setCheckoutDate(String checkoutDate) { this.checkoutDate = checkoutDate; }

    public int getNumRooms() { return numRooms; }
    public void setNumRooms(int numRooms) { this.numRooms = numRooms; }

    public int getNumGuests() { return numGuests; }
    public void setNumGuests(int numGuests) { this.numGuests = numGuests; }

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
}
