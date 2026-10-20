package com.hotelease.model;

import jakarta.persistence.*;

@Entity
@Table(name = "room_types")
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Column(nullable = false, length = 100)
    private String name; // Standard Room, Deluxe Room, Executive Suite, Family Room, Presidential Suite

    @Column(name = "base_price", nullable = false)
    private double basePrice;

    @Column(name = "capacity_adults", nullable = false)
    private int capacityAdults = 2;

    @Column(name = "capacity_children")
    private int capacityChildren = 1;

    @Column(name = "total_rooms", nullable = false)
    private int totalRooms = 10;

    @Column(name = "room_category", length = 50)
    private String roomCategory = "STANDARD"; // BUDGET, ECONOMY, STANDARD, DELUXE, SUITE, DORMITORY

    @Column(name = "has_ac")
    private Boolean hasAc = true;

    @Column(columnDefinition = "TEXT")

    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    public RoomType() {}

    public RoomType(Hotel hotel, String name, double basePrice, int capacityAdults, int capacityChildren, int totalRooms, String description, String imageUrl) {
        this.hotel = hotel;
        this.name = name;
        this.basePrice = basePrice;
        this.capacityAdults = capacityAdults;
        this.capacityChildren = capacityChildren;
        this.totalRooms = totalRooms;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public RoomType(Hotel hotel, String name, String roomCategory, boolean hasAc, double basePrice, int capacityAdults, int capacityChildren, int totalRooms, String description, String imageUrl) {
        this.hotel = hotel;
        this.name = name;
        this.roomCategory = roomCategory;
        this.hasAc = hasAc;
        this.basePrice = basePrice;
        this.capacityAdults = capacityAdults;
        this.capacityChildren = capacityChildren;
        this.totalRooms = totalRooms;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Hotel getHotel() { return hotel; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

    public int getCapacityAdults() { return capacityAdults; }
    public void setCapacityAdults(int capacityAdults) { this.capacityAdults = capacityAdults; }

    public int getCapacityChildren() { return capacityChildren; }
    public void setCapacityChildren(int capacityChildren) { this.capacityChildren = capacityChildren; }

    public int getTotalRooms() { return totalRooms; }
    public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getRoomCategory() { return roomCategory; }
    public void setRoomCategory(String roomCategory) { this.roomCategory = roomCategory; }

    public boolean isHasAc() { return Boolean.TRUE.equals(hasAc); }
    public Boolean getHasAc() { return Boolean.TRUE.equals(hasAc); }
    public void setHasAc(Boolean hasAc) { this.hasAc = hasAc != null ? hasAc : true; }
}


