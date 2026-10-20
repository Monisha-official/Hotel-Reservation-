package com.hotelease.controller;

import com.hotelease.model.Hotel;
import com.hotelease.model.RoomType;
import com.hotelease.repository.BookingRepository;
import com.hotelease.repository.HotelRepository;
import com.hotelease.repository.RoomTypeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/hotels")
@CrossOrigin(origins = "*")
public class HotelController {

    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final BookingRepository bookingRepository;

    public HotelController(HotelRepository hotelRepository,
                           RoomTypeRepository roomTypeRepository,
                           BookingRepository bookingRepository) {
        this.hotelRepository = hotelRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping
    public ResponseEntity<List<Hotel>> getAllHotels(
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "district", required = false) String district,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "landmark", required = false) String landmark,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "query", required = false) String query) {
        
        if ((country != null && !country.trim().isEmpty()) ||
            (state != null && !state.trim().isEmpty()) ||
            (district != null && !district.trim().isEmpty()) ||
            (city != null && !city.trim().isEmpty()) ||
            (landmark != null && !landmark.trim().isEmpty()) ||
            (category != null && !category.trim().isEmpty()) ||
            (query != null && !query.trim().isEmpty())) {
            return ResponseEntity.ok(hotelRepository.searchHotels(country, state, district, city, landmark, category, query));
        }
        return ResponseEntity.ok(hotelRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hotel> getHotelById(@PathVariable("id") Long id) {
        return hotelRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/room-types")
    public ResponseEntity<List<RoomType>> getRoomTypesByHotel(@PathVariable("id") Long id) {
        return ResponseEntity.ok(roomTypeRepository.findByHotelId(id));
    }

    /**
     * Search available hotels with real-time room availability, GPS distance radius calculations,
     * budget accommodation filters (under ₹1,000/night), amenity filters, and price sorting.
     */
    @GetMapping("/search-availability")
    public ResponseEntity<List<Map<String, Object>>> searchHotelsWithAvailability(
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "district", required = false) String district,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "landmark", required = false) String landmark,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "roomType", required = false) String roomTypeStr,
            @RequestParam(value = "checkin", required = false) String checkinStr,
            @RequestParam(value = "checkout", required = false) String checkoutStr,
            @RequestParam(value = "destLat", required = false) Double destLat,
            @RequestParam(value = "destLng", required = false) Double destLng,
            @RequestParam(value = "radiusKm", required = false) Double radiusKm,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "hasAc", required = false) Boolean hasAc,
            @RequestParam(value = "hasWifi", required = false) Boolean hasWifi,
            @RequestParam(value = "hasParking", required = false) Boolean hasParking,
            @RequestParam(value = "hasBreakfast", required = false) Boolean hasBreakfast,
            @RequestParam(value = "sortBy", defaultValue = "price_asc") String sortBy) {

        List<Hotel> candidateHotels;
        boolean hasLocationFilter = (country != null && !country.trim().isEmpty()) ||
                                    (state != null && !state.trim().isEmpty()) ||
                                    (district != null && !district.trim().isEmpty()) ||
                                    (city != null && !city.trim().isEmpty()) ||
                                    (landmark != null && !landmark.trim().isEmpty()) ||
                                    (category != null && !category.trim().isEmpty()) ||
                                    (query != null && !query.trim().isEmpty());

        // When coordinates and radius are passed without city/landmark filter, search nationwide/worldwide
        if (hasLocationFilter) {
            candidateHotels = hotelRepository.searchHotels(country, state, district, city, landmark, category, query);
        } else {
            candidateHotels = hotelRepository.findAll();
        }


        LocalDate checkin = (checkinStr != null && !checkinStr.trim().isEmpty())
                ? LocalDate.parse(checkinStr)
                : LocalDate.now().plusDays(1);

        LocalDate checkout = (checkoutStr != null && !checkoutStr.trim().isEmpty())
                ? LocalDate.parse(checkoutStr)
                : checkin.plusDays(1);

        long nights = Math.max(1, ChronoUnit.DAYS.between(checkin, checkout));

        List<Map<String, Object>> result = new ArrayList<>();

        for (Hotel hotel : candidateHotels) {
            // Amenity Filters on Hotel level
            if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase(hotel.getCategory())) {
                continue;
            }
            if (hasWifi != null && hasWifi && !Boolean.TRUE.equals(hotel.getHasWifi())) {
                continue;
            }
            if (hasParking != null && hasParking && !Boolean.TRUE.equals(hotel.getHasParking())) {
                continue;
            }
            if (hasBreakfast != null && hasBreakfast && !Boolean.TRUE.equals(hotel.getHasBreakfast())) {
                continue;
            }

            // Haversine GPS Distance Calculation
            Double distanceKm = null;
            if (destLat != null && destLng != null && hotel.getLatitude() != null && hotel.getLongitude() != null) {
                distanceKm = calculateHaversineDistanceKm(destLat, destLng, hotel.getLatitude(), hotel.getLongitude());
                // Radius Filtering
                if (radiusKm != null && radiusKm > 0 && distanceKm > radiusKm) {
                    continue; // Skip hotels beyond selected radius
                }
            }

            List<RoomType> rts = roomTypeRepository.findByHotelId(hotel.getId());
            List<Map<String, Object>> roomTypeDetails = new ArrayList<>();
            Double minHotelRoomPrice = null;

            for (RoomType rt : rts) {
                // Room Type string filter
                if (roomTypeStr != null && !roomTypeStr.trim().isEmpty() &&
                    !rt.getName().equalsIgnoreCase(roomTypeStr) &&
                    !rt.getName().toLowerCase().contains(roomTypeStr.toLowerCase())) {
                    continue;
                }

                // AC Filter
                if (hasAc != null && !hasAc.equals(rt.getHasAc())) {
                    continue;
                }

                // Min / Max Price Filter
                if (minPrice != null && rt.getBasePrice() < minPrice) {
                    continue;
                }
                if (maxPrice != null && rt.getBasePrice() > maxPrice) {
                    continue;
                }

                int booked = bookingRepository.countBookedRoomsInDateRange(rt.getId(), checkin, checkout);
                int available = Math.max(0, rt.getTotalRooms() - booked);

                double totalStayCost = rt.getBasePrice() * nights;
                double gst = totalStayCost * 0.18;
                double grandTotal = totalStayCost + gst;

                Map<String, Object> rtMap = new HashMap<>();
                rtMap.put("id", rt.getId());
                rtMap.put("name", rt.getName());
                rtMap.put("roomCategory", rt.getRoomCategory());
                rtMap.put("hasAc", rt.getHasAc());
                rtMap.put("price", rt.getBasePrice());
                rtMap.put("nights", nights);
                rtMap.put("totalStayCost", totalStayCost);
                rtMap.put("taxAmount", gst);
                rtMap.put("grandTotal", grandTotal);
                rtMap.put("capacityAdults", rt.getCapacityAdults());
                rtMap.put("capacityChildren", rt.getCapacityChildren());
                rtMap.put("totalRooms", rt.getTotalRooms());
                rtMap.put("availableRooms", available);
                rtMap.put("isAvailable", available > 0);
                roomTypeDetails.add(rtMap);

                if (minHotelRoomPrice == null || rt.getBasePrice() < minHotelRoomPrice) {
                    minHotelRoomPrice = rt.getBasePrice();
                }
            }

            // Only include hotel if it has matching rooms
            if (roomTypeDetails.isEmpty()) {
                continue;
            }

            Map<String, Object> hotelMap = new HashMap<>();
            hotelMap.put("id", hotel.getId());
            hotelMap.put("name", hotel.getName());
            hotelMap.put("country", hotel.getCountry());
            hotelMap.put("state", hotel.getState());
            hotelMap.put("district", hotel.getDistrict());
            hotelMap.put("city", hotel.getCity());
            hotelMap.put("landmark", hotel.getLandmark());
            hotelMap.put("address", hotel.getAddress());
            hotelMap.put("category", hotel.getCategory());
            hotelMap.put("hasWifi", hotel.getHasWifi());
            hotelMap.put("hasParking", hotel.getHasParking());
            hotelMap.put("hasBreakfast", hotel.getHasBreakfast());
            hotelMap.put("hasAc", hotel.getHasAc());
            hotelMap.put("latitude", hotel.getLatitude());
            hotelMap.put("longitude", hotel.getLongitude());
            hotelMap.put("googleMapsUrl", hotel.getGoogleMapsUrl());
            hotelMap.put("distanceKm", distanceKm != null ? Math.round(distanceKm * 100.0) / 100.0 : null);
            hotelMap.put("minPrice", minHotelRoomPrice);
            hotelMap.put("rating", hotel.getRating());
            hotelMap.put("imageUrl", hotel.getImageUrl());
            hotelMap.put("description", hotel.getDescription());
            hotelMap.put("roomTypes", roomTypeDetails);

            result.add(hotelMap);
        }

        // Sorting
        if ("price_asc".equalsIgnoreCase(sortBy)) {
            result.sort(Comparator.comparingDouble(h -> (Double) h.getOrDefault("minPrice", Double.MAX_VALUE)));
        } else if ("price_desc".equalsIgnoreCase(sortBy)) {
            result.sort((h1, h2) -> Double.compare((Double) h2.getOrDefault("minPrice", 0.0), (Double) h1.getOrDefault("minPrice", 0.0)));
        } else if ("rating_desc".equalsIgnoreCase(sortBy)) {
            result.sort((h1, h2) -> Double.compare((Double) h2.getOrDefault("rating", 0.0), (Double) h1.getOrDefault("rating", 0.0)));
        } else if ("distance_asc".equalsIgnoreCase(sortBy)) {
            result.sort(Comparator.comparingDouble(h -> (Double) h.getOrDefault("distanceKm", Double.MAX_VALUE)));
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Standard Haversine distance formula: computes great-circle distance between two GPS points in kilometers.
     */
    private double calculateHaversineDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Earth's radius in kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2.0) * Math.sin(dLon / 2.0);
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return R * c;
    }
}

