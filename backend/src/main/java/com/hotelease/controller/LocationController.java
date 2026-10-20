package com.hotelease.controller;

import com.hotelease.model.Hotel;
import com.hotelease.repository.HotelRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "*")
public class LocationController {

    private final HotelRepository hotelRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public LocationController(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    /**
     * Returns dynamic cascading 5-tier location tree:
     * Country -> State -> District -> City -> Landmarks
     */
    @GetMapping("/hierarchy")
    public ResponseEntity<Map<String, Object>> getLocationHierarchy() {
        List<Hotel> hotels = hotelRepository.findAll();

        // Country -> State -> District -> City -> Set<Landmarks>
        Map<String, Map<String, Map<String, Map<String, Set<String>>>>> tree = new LinkedHashMap<>();

        for (Hotel h : hotels) {
            String country = h.getCountry() != null && !h.getCountry().trim().isEmpty() ? h.getCountry().trim() : "India";
            String state = h.getState() != null && !h.getState().trim().isEmpty() ? h.getState().trim() : "Tamil Nadu";
            String district = h.getDistrict() != null && !h.getDistrict().trim().isEmpty() ? h.getDistrict().trim() : state;
            String city = h.getCity() != null && !h.getCity().trim().isEmpty() ? h.getCity().trim() : "Default City";
            String landmark = h.getLandmark() != null ? h.getLandmark().trim() : "";

            tree.putIfAbsent(country, new LinkedHashMap<>());
            Map<String, Map<String, Map<String, Set<String>>>> stateMap = tree.get(country);

            stateMap.putIfAbsent(state, new LinkedHashMap<>());
            Map<String, Map<String, Set<String>>> districtMap = stateMap.get(state);

            districtMap.putIfAbsent(district, new LinkedHashMap<>());
            Map<String, Set<String>> cityMap = districtMap.get(district);

            cityMap.putIfAbsent(city, new LinkedHashSet<>());
            if (!landmark.isEmpty()) {
                cityMap.get(city).add(landmark);
            }
        }

        // Format into JSON-friendly response
        List<Map<String, Object>> countriesList = new ArrayList<>();

        for (Map.Entry<String, Map<String, Map<String, Map<String, Set<String>>>>> countryEntry : tree.entrySet()) {
            Map<String, Object> countryMap = new HashMap<>();
            countryMap.put("name", countryEntry.getKey());

            List<Map<String, Object>> statesList = new ArrayList<>();
            for (Map.Entry<String, Map<String, Map<String, Set<String>>>> stateEntry : countryEntry.getValue().entrySet()) {
                Map<String, Object> stateMap = new HashMap<>();
                stateMap.put("name", stateEntry.getKey());

                List<Map<String, Object>> districtsList = new ArrayList<>();
                for (Map.Entry<String, Map<String, Set<String>>> districtEntry : stateEntry.getValue().entrySet()) {
                    Map<String, Object> districtMap = new HashMap<>();
                    districtMap.put("name", districtEntry.getKey());

                    List<Map<String, Object>> citiesList = new ArrayList<>();
                    for (Map.Entry<String, Set<String>> cityEntry : districtEntry.getValue().entrySet()) {
                        Map<String, Object> cityMap = new HashMap<>();
                        cityMap.put("name", cityEntry.getKey());
                        cityMap.put("landmarks", new ArrayList<>(cityEntry.getValue()));
                        citiesList.add(cityMap);
                    }
                    districtMap.put("cities", citiesList);
                    districtsList.add(districtMap);
                }
                stateMap.put("districts", districtsList);
                statesList.add(stateMap);
            }
            countryMap.put("states", statesList);
            countriesList.add(countryMap);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("countries", countriesList);
        return ResponseEntity.ok(response);
    }

    /**
     * Provides live autocomplete search suggestions across Countries, States, Districts, Cities, Landmarks & Hotels.
     * Includes coordinates (lat, lng) for immediate map navigation.
     */
    @GetMapping("/suggestions")
    public ResponseEntity<List<Map<String, Object>>> getSuggestions(@RequestParam(value = "q", defaultValue = "") String query) {
        if (query == null || query.trim().length() < 1) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        String q = query.trim().toLowerCase();
        List<Hotel> hotels = hotelRepository.findAll();
        List<Map<String, Object>> suggestions = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (Hotel h : hotels) {
            // Check Landmark match (e.g. Brihadisvara Temple, Eiffel Tower)
            if (h.getLandmark() != null && h.getLandmark().toLowerCase().contains(q)) {
                String key = "LM:" + h.getLandmark() + ":" + h.getCity();
                if (!seen.contains(key)) {
                    seen.add(key);
                    Map<String, Object> item = new HashMap<>();
                    item.put("type", "landmark");
                    item.put("value", h.getLandmark());
                    item.put("label", "🏛️ " + h.getLandmark() + " (Landmark in " + h.getCity() + ", " + h.getDistrict() + ", " + h.getCountry() + ")");
                    item.put("district", h.getDistrict());
                    item.put("city", h.getCity());
                    item.put("state", h.getState());
                    item.put("country", h.getCountry());
                    item.put("latitude", h.getLatitude());
                    item.put("longitude", h.getLongitude());
                    suggestions.add(item);
                }
            }

            // Check City match (e.g. Thanjavur, Chennai, Paris)
            if (h.getCity() != null && h.getCity().toLowerCase().contains(q)) {
                String key = "CITY:" + h.getCity() + ":" + h.getState();
                if (!seen.contains(key)) {
                    seen.add(key);
                    Map<String, Object> item = new HashMap<>();
                    item.put("type", "city");
                    item.put("value", h.getCity());
                    item.put("label", "🌆 " + h.getCity() + " (City in " + h.getDistrict() + ", " + h.getState() + ", " + h.getCountry() + ")");
                    item.put("district", h.getDistrict());
                    item.put("city", h.getCity());
                    item.put("state", h.getState());
                    item.put("country", h.getCountry());
                    item.put("latitude", h.getLatitude());
                    item.put("longitude", h.getLongitude());
                    suggestions.add(item);
                }
            }

            // Check District match (e.g. Thanjavur District, Central Delhi)
            if (h.getDistrict() != null && h.getDistrict().toLowerCase().contains(q)) {
                String key = "DISTRICT:" + h.getDistrict();
                if (!seen.contains(key)) {
                    seen.add(key);
                    Map<String, Object> item = new HashMap<>();
                    item.put("type", "district");
                    item.put("value", h.getDistrict());
                    item.put("label", "🗺️ " + h.getDistrict() + " (District in " + h.getState() + ", " + h.getCountry() + ")");
                    item.put("district", h.getDistrict());
                    item.put("state", h.getState());
                    item.put("country", h.getCountry());
                    item.put("latitude", h.getLatitude());
                    item.put("longitude", h.getLongitude());
                    suggestions.add(item);
                }
            }

            // Check Hotel Name match
            if (h.getName() != null && h.getName().toLowerCase().contains(q)) {
                String key = "HOTEL:" + h.getName();
                if (!seen.contains(key)) {
                    seen.add(key);
                    Map<String, Object> item = new HashMap<>();
                    item.put("type", "hotel");
                    item.put("value", h.getName());
                    item.put("label", "🏨 " + h.getName() + " (" + h.getCity() + " - " + h.getCategory() + ")");
                    item.put("district", h.getDistrict());
                    item.put("city", h.getCity());
                    item.put("state", h.getState());
                    item.put("country", h.getCountry());
                    item.put("latitude", h.getLatitude());
                    item.put("longitude", h.getLongitude());
                    suggestions.add(item);
                }
            }

            // Check State match
            if (h.getState() != null && h.getState().toLowerCase().contains(q)) {
                String key = "STATE:" + h.getState();
                if (!seen.contains(key)) {
                    seen.add(key);
                    Map<String, Object> item = new HashMap<>();
                    item.put("type", "state");
                    item.put("value", h.getState());
                    item.put("label", "📍 " + h.getState() + " (State/Province in " + h.getCountry() + ")");
                    item.put("state", h.getState());
                    item.put("country", h.getCountry());
                    item.put("latitude", h.getLatitude());
                    item.put("longitude", h.getLongitude());
                    suggestions.add(item);
                }
            }
        }

        return ResponseEntity.ok(suggestions);
    }

    /**
     * Geocode an address/place name to coordinates using database landmarks or OpenStreetMap Nominatim.
     */
    @GetMapping("/geocode")
    public ResponseEntity<Map<String, Object>> geocode(@RequestParam("query") String query) {
        Map<String, Object> result = new HashMap<>();
        String q = query.trim().toLowerCase();

        // 1. Check local DB hotels/landmarks first for instant, reliable response
        List<Hotel> hotels = hotelRepository.findAll();
        for (Hotel h : hotels) {
            if ((h.getLandmark() != null && h.getLandmark().toLowerCase().contains(q)) ||
                (h.getCity() != null && h.getCity().toLowerCase().contains(q)) ||
                (h.getName() != null && h.getName().toLowerCase().contains(q))) {
                result.put("found", true);
                result.put("name", h.getLandmark() != null && !h.getLandmark().isEmpty() ? h.getLandmark() : h.getCity());
                result.put("city", h.getCity());
                result.put("district", h.getDistrict());
                result.put("state", h.getState());
                result.put("country", h.getCountry());
                result.put("latitude", h.getLatitude());
                result.put("longitude", h.getLongitude());
                return ResponseEntity.ok(result);
            }
        }

        // 2. Fallback to OpenStreetMap Nominatim API if query is outside local DB
        try {
            String url = "https://nominatim.openstreetmap.org/search?q=" +
                    URLEncoder.encode(query, StandardCharsets.UTF_8) + "&format=json&limit=1";
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "HotelEase-App/2.0");
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            ResponseEntity<List> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, List.class);

            if (response.getBody() != null && !response.getBody().isEmpty()) {
                Map<?, ?> first = (Map<?, ?>) response.getBody().get(0);
                result.put("found", true);
                result.put("name", first.get("display_name"));
                result.put("latitude", Double.parseDouble(first.get("lat").toString()));
                result.put("longitude", Double.parseDouble(first.get("lon").toString()));
                return ResponseEntity.ok(result);
            }
        } catch (Exception e) {
            // Ignore external geocoding errors gracefully
        }

        result.put("found", false);
        return ResponseEntity.ok(result);
    }
}

