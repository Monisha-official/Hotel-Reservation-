package com.hotelease.dto;

public class PriceCalculationResponseDTO {
    private double baseRoomRatePerNight;
    private long numberOfNights;
    private int numberOfRooms;
    private double roomSubtotal;
    private double servicesSubtotal;
    private double taxAmount;
    private double totalAmount;
    private boolean available;
    private int remainingAvailableRooms;
    private String message;

    public PriceCalculationResponseDTO() {}

    public double getBaseRoomRatePerNight() { return baseRoomRatePerNight; }
    public void setBaseRoomRatePerNight(double baseRoomRatePerNight) { this.baseRoomRatePerNight = baseRoomRatePerNight; }

    public long getNumberOfNights() { return numberOfNights; }
    public void setNumberOfNights(long numberOfNights) { this.numberOfNights = numberOfNights; }

    public int getNumberOfRooms() { return numberOfRooms; }
    public void setNumberOfRooms(int numberOfRooms) { this.numberOfRooms = numberOfRooms; }

    public double getRoomSubtotal() { return roomSubtotal; }
    public void setRoomSubtotal(double roomSubtotal) { this.roomSubtotal = roomSubtotal; }

    public double getServicesSubtotal() { return servicesSubtotal; }
    public void setServicesSubtotal(double servicesSubtotal) { this.servicesSubtotal = servicesSubtotal; }

    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public int getRemainingAvailableRooms() { return remainingAvailableRooms; }
    public void setRemainingAvailableRooms(int remainingAvailableRooms) { this.remainingAvailableRooms = remainingAvailableRooms; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
