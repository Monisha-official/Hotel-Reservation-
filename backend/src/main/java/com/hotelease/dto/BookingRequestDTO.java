package com.hotelease.dto;

public class BookingRequestDTO {
    private Long hotelId;
    private String location;
    private String roomType; // or roomTypeId
    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private String guestCountry;
    private String checkinDate;
    private String checkoutDate;
    private int numRooms = 1;
    private int numGuests = 1;
    private int numChildren = 0;
    private String bedPreference;
    private String floorPreference;
    private String viewPreference;
    private String smokingPreference;
    private boolean airportPickup;
    private boolean breakfast;
    private boolean wifi;
    private boolean parking;
    private boolean spa;
    private String visitPurpose;
    private String arrivalTime;
    private String specialRequests;
    private String paymentMethod;

    public BookingRequestDTO() {}

    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }

    public String getGuestPhone() { return guestPhone; }
    public void setGuestPhone(String guestPhone) { this.guestPhone = guestPhone; }

    public String getGuestCountry() { return guestCountry; }
    public void setGuestCountry(String guestCountry) { this.guestCountry = guestCountry; }

    public String getCheckinDate() { return checkinDate; }
    public void setCheckinDate(String checkinDate) { this.checkinDate = checkinDate; }

    public String getCheckoutDate() { return checkoutDate; }
    public void setCheckoutDate(String checkoutDate) { this.checkoutDate = checkoutDate; }

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

    public String getVisitPurpose() { return visitPurpose; }
    public void setVisitPurpose(String visitPurpose) { this.visitPurpose = visitPurpose; }

    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
