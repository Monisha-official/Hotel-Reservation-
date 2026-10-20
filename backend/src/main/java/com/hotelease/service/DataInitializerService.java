package com.hotelease.service;

import com.hotelease.model.*;
import com.hotelease.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DataInitializerService {

    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final ServiceRequestRepository serviceRequestRepository;

    public DataInitializerService(HotelRepository hotelRepository,
                                  RoomTypeRepository roomTypeRepository,
                                  RoomRepository roomRepository,
                                  UserRepository userRepository,
                                  BookingRepository bookingRepository,
                                  PaymentRepository paymentRepository,
                                  ServiceRequestRepository serviceRequestRepository) {
        this.hotelRepository = hotelRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    @PostConstruct
    @Transactional
    public void initializeSeedData() {
        // Ensure default system accounts are guaranteed to exist
        if (userRepository.findByEmail("admin@hotelease.com").isEmpty()) {
            userRepository.save(new User("System Admin", "admin@hotelease.com", "admin123", "+91 9876543210", "ADMIN"));
        }
        if (userRepository.findByEmail("rahul@example.com").isEmpty()) {
            userRepository.save(new User("Rahul Verma", "rahul@example.com", "user123", "+91 9876543213", "CUSTOMER"));
        }
        if (userRepository.findByEmail("rajesh@hotelease.com").isEmpty()) {
            userRepository.save(new User("Rajesh Kumar (Technician)", "rajesh@hotelease.com", "staff123", "+91 9876543211", "STAFF"));
        }
        if (userRepository.findByEmail("priya@hotelease.com").isEmpty()) {
            userRepository.save(new User("Priya Sharma (Housekeeping)", "priya@hotelease.com", "staff123", "+91 9876543212", "STAFF"));
        }


        if (hotelRepository.count() < 12) {
            // Clear legacy seed data if present in foreign key dependency order
            paymentRepository.deleteAll();
            serviceRequestRepository.deleteAll();
            bookingRepository.deleteAll();
            roomRepository.deleteAll();
            roomTypeRepository.deleteAll();
            hotelRepository.deleteAll();


            // Seed sample hotels across multiple countries, states, districts, cities & landmarks
            // 1. Delhi Mid-range
            Hotel h1 = new Hotel("Hotel Ashoka Residency", "India", "Delhi", "Central Delhi", "New Delhi", "Connaught Place / IGI Airport", "Connaught Place, New Delhi", "MID_RANGE", true, true, true, true, 28.6315, 77.2167, "https://www.google.com/maps/search/?api=1&query=Connaught+Place+New+Delhi", 4.3, "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=500", "Mid-range hotel in the heart of Delhi with clean rooms, conference facilities, and friendly staff.");

            // 2. Delhi Budget Lodge
            Hotel h2 = new Hotel("Paharganj Tourist Lodge & Guesthouse", "India", "Delhi", "Central Delhi", "New Delhi", "New Delhi Railway Station", "Main Bazaar, Paharganj, New Delhi", "BUDGET_LODGE", true, false, false, false, 28.6425, 77.2150, "https://www.google.com/maps/search/?api=1&query=New+Delhi+Railway+Station", 4.1, "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=500", "Affordable traveler lodge adjacent to New Delhi Railway Station with economy rooms for students, backpackers, and budget workers.");

            // 3. Bangalore
            Hotel h3 = new Hotel("The Grand Maharaja", "India", "Karnataka", "Bangalore Urban", "Bangalore", "MG Road / Commercial Street", "MG Road, Bangalore", "MID_RANGE", true, true, true, true, 12.9756, 77.6066, "https://www.google.com/maps/search/?api=1&query=MG+Road+Bangalore", 4.6, "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=500", "Modern hotel with excellent amenities in Bangalore's commercial hub.");

            // 4. Mumbai
            Hotel h4 = new Hotel("Taj Heritage Palace", "India", "Maharashtra", "Mumbai City", "Mumbai", "Marine Drive / Gateway of India", "Marine Drive, Mumbai", "HERITAGE", true, true, true, true, 18.9440, 72.8238, "https://www.google.com/maps/search/?api=1&query=Marine+Drive+Mumbai", 4.8, "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=500", "Luxury hotel with stunning sea views and world-class hospitality.");

            // 5. Jaipur
            Hotel h5 = new Hotel("Royal Rajputana Resort", "India", "Rajasthan", "Jaipur", "Jaipur", "City Palace Area / Hawa Mahal", "City Palace Area, Jaipur", "HERITAGE", true, true, true, true, 26.9258, 75.8237, "https://www.google.com/maps/search/?api=1&query=City+Palace+Jaipur", 4.9, "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=500", "Experience royal Rajasthani hospitality in this magnificent heritage property.");

            // 6. Goa
            Hotel h6 = new Hotel("Goa Beach Paradise", "India", "Goa", "North Goa", "Goa", "Calangute Beach / Baga", "Calangute Beach, Goa", "MID_RANGE", true, true, true, true, 15.5494, 73.7535, "https://www.google.com/maps/search/?api=1&query=Calangute+Beach+Goa", 4.7, "https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=500", "Beachfront resort offering water sports and stunning sunset views.");
            
            // 7. Thanjavur Budget Lodge 1 (Under ₹1,000 - 0.4 km from Big Temple)
            Hotel h7 = new Hotel("Brihadeeswara Pilgrim Lodge & Guesthouse", "India", "Tamil Nadu", "Thanjavur", "Thanjavur", "Brihadisvara Temple (Big Temple) / Thanjavur Bus Stand", "West Main St, 0.4 km from Big Temple, Thanjavur", "BUDGET_LODGE", true, false, false, false, 10.7835, 79.1325, "https://www.google.com/maps/search/?api=1&query=Brihadisvara+Temple+Thanjavur", 4.4, "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=500", "Clean, highly affordable pilgrim lodge and guesthouse situated just 400m from Brihadisvara Temple (Big Temple). Inexpensive non-AC and AC rooms for families, students, and pilgrims.");

            // 8. Thanjavur Budget Lodge 2 (Near Railway Station)
            Hotel h8 = new Hotel("Meenakshi Bhavan Residency & Budget Lodge", "India", "Tamil Nadu", "Thanjavur", "Thanjavur", "Thanjavur Railway Station / Old Bus Stand", "Railway Station Road, Thanjavur", "BUDGET_LODGE", true, true, false, false, 10.7865, 79.1375, "https://www.google.com/maps/search/?api=1&query=Thanjavur+Railway+Station", 4.2, "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=500", "Budget accommodation located near Thanjavur Junction Railway Station with basic comfortable lodging.");

            // 9. Thanjavur Heritage Hotel
            Hotel h9 = new Hotel("Hotel Chola Grand", "India", "Tamil Nadu", "Thanjavur", "Thanjavur", "Brihadisvara Temple (Big Temple) / Railway Station", "Near Big Temple, South Main St, Thanjavur", "HERITAGE", true, true, true, true, 10.7828, 79.1318, "https://www.google.com/maps/search/?api=1&query=Brihadisvara+Temple+Thanjavur", 4.7, "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=500", "Premium heritage stay minutes from UNESCO World Heritage Brihadisvara Temple and Thanjavur Railway Station.");

            // 10. Thanjavur Cultural Heritage Hotel
            Hotel h10 = new Hotel("Sangu Chakra Heritage Hotel", "India", "Tamil Nadu", "Thanjavur", "Thanjavur", "Saraswathi Mahal Library / Bus Stand", "East Main St, Thanjavur", "HERITAGE", true, true, true, true, 10.7915, 79.1384, "https://www.google.com/maps/search/?api=1&query=Saraswathi+Mahal+Library+Thanjavur", 4.5, "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=500", "Traditional Tamil cultural hotel located near the historic Saraswathi Mahal Library and Central Bus Stand.");

            // 11. Chennai Budget Lodge (Under ₹1,000)
            Hotel h11 = new Hotel("Marina Youth Hostel & Budget Lodge", "India", "Tamil Nadu", "Chennai", "Chennai", "Marina Beach / Chennai Central", "Triplicane High Rd, Near Marina Beach, Chennai", "BUDGET_LODGE", true, false, false, false, 13.0475, 80.2825, "https://www.google.com/maps/search/?api=1&query=Marina+Beach+Chennai", 4.3, "https://images.unsplash.com/photo-1555854877-bab0e564b8d5?w=500", "Inexpensive youth hostel and budget lodge near Marina Beach. Features backpacker dorm beds and budget private rooms.");

            // 12. Chennai Luxury Palace
            Hotel h12 = new Hotel("The Leela Palace Chennai", "India", "Tamil Nadu", "Chennai", "Chennai", "Marina Beach / Santhome Cathedral", "MRC Nagar, Raja Annamalaipuram, Chennai", "LUXURY", true, true, true, true, 13.0267, 80.2745, "https://www.google.com/maps/search/?api=1&query=The+Leela+Palace+Chennai", 4.9, "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=500", "Sea-facing luxury palace hotel offering breathtaking panoramic views of Marina Beach.");

            // 13. San Francisco
            Hotel h13 = new Hotel("The Grand Hyatt San Francisco", "USA", "California", "San Francisco County", "San Francisco", "Union Square / Fisherman's Wharf", "345 Stockton St, San Francisco, CA", "LUXURY", true, true, true, true, 37.7879, -122.4075, "https://www.google.com/maps/search/?api=1&query=Union+Square+San+Francisco", 4.8, "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=500", "Iconic hotel steps away from Union Square shopping, cable cars, and Fisherman's Wharf.");

            // 14. Paris
            Hotel h14 = new Hotel("Le Grand Hotel Paris", "France", "Île-de-France", "Paris Department", "Paris", "Eiffel Tower / Champs-Élysées", "2 Rue Scribe, Paris", "LUXURY", true, true, true, true, 48.8708, 2.3312, "https://www.google.com/maps/search/?api=1&query=Eiffel+Tower+Paris", 4.9, "https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=500", "Classic Parisian luxury hotel overlooking Opera Garnier and near the Eiffel Tower.");

            // 15. Tokyo
            Hotel h15 = new Hotel("Tokyo Tower Palace Hotel", "Japan", "Kantō", "Chiyoda Ward", "Tokyo", "Shinjuku / Tokyo Station", "1-1-1 Marunouchi, Chiyoda-ku, Tokyo", "LUXURY", true, true, true, true, 35.6852, 139.7528, "https://www.google.com/maps/search/?api=1&query=Tokyo+Station+Japan", 4.9, "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=500", "Serene luxury sanctuary adjacent to the Imperial Palace gardens and Tokyo Station.");

            List<Hotel> allHotels = List.of(h1, h2, h3, h4, h5, h6, h7, h8, h9, h10, h11, h12, h13, h14, h15);
            hotelRepository.saveAll(allHotels);

            // Seed Room Types tailored to budget and category
            // Brihadeeswara Pilgrim Lodge (Thanjavur) -> Affordable budget rooms
            RoomType rtPilgrim1 = new RoomType(h7, "Economy Non-AC Room", "NON_AC_BUDGET", false, 650, 2, 1, 8, "Clean budget non-AC room with attached bathroom, fan, and free clean RO drinking water. Perfect for pilgrims and students.", "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=500");
            RoomType rtPilgrim2 = new RoomType(h7, "Standard AC Pilgrim Room", "STANDARD_AC", true, 950, 3, 1, 6, "Air-conditioned budget room with double bed, extra mattress, and hot water geyser.", "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=500");
            RoomType rtPilgrim3 = new RoomType(h7, "Family Pilgrim Suite (4-5 Beds)", "FAMILY_SUITE", true, 1400, 5, 2, 4, "Spacious family room with 4 single beds or 2 double beds, ideal for temple pilgrimage groups.", "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=500");
            roomTypeRepository.saveAll(List.of(rtPilgrim1, rtPilgrim2, rtPilgrim3));

            // Meenakshi Bhavan (Thanjavur)
            RoomType rtMeenakshi1 = new RoomType(h8, "Budget Saver Room (Non-AC)", "NON_AC_BUDGET", false, 750, 2, 1, 8, "Affordable non-AC room near Thanjavur Railway Station with cable TV and WiFi.", "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=500");
            RoomType rtMeenakshi2 = new RoomType(h8, "Budget Deluxe AC Room", "STANDARD_AC", true, 1100, 3, 1, 5, "Air-conditioned comfortable room near bus stand.", "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=500");
            roomTypeRepository.saveAll(List.of(rtMeenakshi1, rtMeenakshi2));

            // Marina Youth Hostel (Chennai)
            RoomType rtMarina1 = new RoomType(h11, "Backpacker Dormitory Bed", "DORMITORY", false, 550, 1, 0, 12, "Single bunk bed in clean air-cooled dormitory with secure locker and shared bathroom.", "https://images.unsplash.com/photo-1555854877-bab0e564b8d5?w=500");
            RoomType rtMarina2 = new RoomType(h11, "Economy Non-AC Private Room", "NON_AC_BUDGET", false, 850, 2, 1, 6, "Private non-AC double room with fan and desk.", "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=500");
            RoomType rtMarina3 = new RoomType(h11, "Standard AC Private Room", "STANDARD_AC", true, 1200, 2, 1, 5, "Private AC room near Marina Beach.", "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=500");
            roomTypeRepository.saveAll(List.of(rtMarina1, rtMarina2, rtMarina3));

            // Paharganj Lodge (Delhi)
            RoomType rtDelhiLodge1 = new RoomType(h2, "Economy Non-AC Room", "NON_AC_BUDGET", false, 750, 2, 1, 8, "Budget accommodation near New Delhi Railway Station.", "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=500");
            RoomType rtDelhiLodge2 = new RoomType(h2, "Standard AC Budget Room", "STANDARD_AC", true, 990, 2, 1, 6, "Affordable air-conditioned room with high-speed WiFi.", "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=500");
            roomTypeRepository.saveAll(List.of(rtDelhiLodge1, rtDelhiLodge2));

            // Seed Room Types for remaining properties (Standard, Deluxe, Suites)
            for (Hotel hotel : List.of(h1, h3, h4, h5, h6, h9, h10, h12, h13, h14, h15)) {
                RoomType rtStandard = new RoomType(hotel, "Standard Room", "STANDARD", true, 2500, 2, 1, 10, "Cozy standard room with free high-speed WiFi and air conditioning.", "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=500");
                RoomType rtDeluxe = new RoomType(hotel, "Deluxe Room", "DELUXE", true, 4000, 2, 1, 8, "Spacious deluxe room with premium bedding and complimentary breakfast.", "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=500");
                RoomType rtSuite = new RoomType(hotel, "Executive Suite", "SUITE", true, 6500, 3, 2, 5, "Luxury suite with panoramic views, living area, and butler service.", "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=500");
                RoomType rtFamily = new RoomType(hotel, "Family Room", "FAMILY_ROOM", true, 5000, 4, 2, 6, "Interconnected double rooms ideal for families.", "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=500");
                RoomType rtPresidential = new RoomType(hotel, "Presidential Suite", "SUITE", true, 12000, 4, 2, 2, "Ultra-luxurious suite with private pool and Jacuzzi.", "https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=500");

                roomTypeRepository.saveAll(List.of(rtStandard, rtDeluxe, rtSuite, rtFamily, rtPresidential));
            }

            // Seed physical rooms
            for (RoomType rt : roomTypeRepository.findAll()) {
                for (int i = 1; i <= Math.min(3, rt.getTotalRooms()); i++) {
                    roomRepository.save(new Room(rt, rt.getId() + "0" + i, 1, "AVAILABLE"));
                }
            }

            // Seed a sample booking and sample service request for demonstration
            Hotel demoHotel = h1;
            RoomType demoRoomType = roomTypeRepository.findByHotelId(demoHotel.getId()).get(0);
            
            Booking demoBooking = new Booking();
            demoBooking.setBookingCode("HE-2026-1001");
            demoBooking.setHotel(demoHotel);
            demoBooking.setRoomType(demoRoomType);
            demoBooking.setGuestName("Rahul Verma");
            demoBooking.setGuestEmail("rahul@example.com");
            demoBooking.setGuestPhone("+91 9876543213");
            demoBooking.setCheckinDate(LocalDate.now());
            demoBooking.setCheckoutDate(LocalDate.now().plusDays(3));
            demoBooking.setNumRooms(1);
            demoBooking.setNumGuests(2);
            demoBooking.setRoomCost(7500);
            demoBooking.setServicesCost(500);
            demoBooking.setTaxAmount(1440);
            demoBooking.setTotalCost(9440);
            demoBooking.setStatus("CONFIRMED");
            demoBooking.setQrCodeToken("QR-HE-2026-1001");
            bookingRepository.save(demoBooking);

            // Sample Service Request
            ServiceRequest sr1 = new ServiceRequest();
            sr1.setBooking(demoBooking);
            sr1.setRoomNumber("104");
            sr1.setIssueDescription("AC is not cooling properly in Room 104");
            sr1.setCategory("MAINTENANCE");
            sr1.setPriority("HIGH");
            sr1.setStatus("PENDING");
            serviceRequestRepository.save(sr1);
        }
    }
}
