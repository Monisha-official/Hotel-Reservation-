package com.hotelease.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hotels")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String country = "India";

    @Column(nullable = false, length = 100)
    private String state = "Delhi";

    @Column(length = 100)
    private String district = "Central Delhi";

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 255)
    private String landmark;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(length = 50)
    private String category = "MID_RANGE"; // BUDGET_LODGE, GUESTHOUSE, ECONOMY, MID_RANGE, HERITAGE, LUXURY

    private Boolean hasWifi = true;
    private Boolean hasParking = false;
    private Boolean hasBreakfast = false;
    private Boolean hasAc = true;

    private Double latitude;

    private Double longitude;

    @Column(name = "google_maps_url", length = 500)
    private String googleMapsUrl;

    private double rating = 4.5;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Hotel() {}

    public Hotel(String name, String city, String address, double rating, String imageUrl, String description) {
        this.name = name;
        this.city = city;
        this.address = address;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    public Hotel(String name, String country, String state, String city, String landmark, String address, double rating, String imageUrl, String description) {
        this.name = name;
        this.country = country;
        this.state = state;
        this.city = city;
        this.landmark = landmark;
        this.address = address;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    public Hotel(String name, String country, String state, String city, String landmark, String address, Double latitude, Double longitude, String googleMapsUrl, double rating, String imageUrl, String description) {
        this.name = name;
        this.country = country;
        this.state = state;
        this.city = city;
        this.landmark = landmark;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.googleMapsUrl = googleMapsUrl;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    public Hotel(String name, String country, String state, String district, String city, String landmark, String address, String category, boolean hasWifi, boolean hasParking, boolean hasBreakfast, boolean hasAc, Double latitude, Double longitude, String googleMapsUrl, double rating, String imageUrl, String description) {
        this.name = name;
        this.country = country;
        this.state = state;
        this.district = district;
        this.city = city;
        this.landmark = landmark;
        this.address = address;
        this.category = category;
        this.hasWifi = hasWifi;
        this.hasParking = hasParking;
        this.hasBreakfast = hasBreakfast;
        this.hasAc = hasAc;
        this.latitude = latitude;
        this.longitude = longitude;
        this.googleMapsUrl = googleMapsUrl;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getLandmark() { return landmark; }
    public void setLandmark(String landmark) { this.landmark = landmark; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isHasWifi() { return Boolean.TRUE.equals(hasWifi); }
    public Boolean getHasWifi() { return Boolean.TRUE.equals(hasWifi); }
    public void setHasWifi(Boolean hasWifi) { this.hasWifi = hasWifi != null ? hasWifi : true; }

    public boolean isHasParking() { return Boolean.TRUE.equals(hasParking); }
    public Boolean getHasParking() { return Boolean.TRUE.equals(hasParking); }
    public void setHasParking(Boolean hasParking) { this.hasParking = hasParking != null ? hasParking : false; }

    public boolean isHasBreakfast() { return Boolean.TRUE.equals(hasBreakfast); }
    public Boolean getHasBreakfast() { return Boolean.TRUE.equals(hasBreakfast); }
    public void setHasBreakfast(Boolean hasBreakfast) { this.hasBreakfast = hasBreakfast != null ? hasBreakfast : false; }

    public boolean isHasAc() { return Boolean.TRUE.equals(hasAc); }
    public Boolean getHasAc() { return Boolean.TRUE.equals(hasAc); }
    public void setHasAc(Boolean hasAc) { this.hasAc = hasAc != null ? hasAc : true; }



    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getGoogleMapsUrl() { return googleMapsUrl; }
    public void setGoogleMapsUrl(String googleMapsUrl) { this.googleMapsUrl = googleMapsUrl; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
