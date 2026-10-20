package com.hotelease.service;

import com.hotelease.dto.ChatRequestDTO;
import com.hotelease.dto.ChatResponseDTO;
import com.hotelease.dto.ServiceRequestDTO;
import com.hotelease.model.Booking;
import com.hotelease.model.Hotel;
import com.hotelease.model.RoomType;
import com.hotelease.model.ServiceRequest;
import com.hotelease.repository.BookingRepository;
import com.hotelease.repository.HotelRepository;
import com.hotelease.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class ChatbotEngineService {

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final ServiceRequestPrioritizationEngine prioritizationEngine;

    public ChatbotEngineService(BookingRepository bookingRepository,
                                HotelRepository hotelRepository,
                                RoomTypeRepository roomTypeRepository,
                                ServiceRequestPrioritizationEngine prioritizationEngine) {
        this.bookingRepository = bookingRepository;
        this.hotelRepository = hotelRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.prioritizationEngine = prioritizationEngine;
    }

    public ChatResponseDTO processChatMessage(ChatRequestDTO request) {
        String msg = request.getMessage() != null ? request.getMessage().toLowerCase().trim() : "";
        ChatResponseDTO response = new ChatResponseDTO();

        if (msg.isEmpty()) {
            response.setReply("Hello! How can I assist you with your stay at HotelEase today?");
            response.setIntent("GREETING");
            return response;
        }

        // Intent 1: Booking Status Inquiries
        if (msg.contains("status") || msg.contains("booking details") || msg.contains("my booking") || msg.contains("he-")) {
            response.setIntent("BOOKING_STATUS");
            String bookingCode = extractBookingCode(msg);

            Booking booking = null;
            if (bookingCode != null) {
                booking = bookingRepository.findByBookingCode(bookingCode).orElse(null);
            } else if (request.getUserEmail() != null) {
                List<Booking> userBookings = bookingRepository.findByGuestEmailOrderByCreatedAtDesc(request.getUserEmail());
                if (!userBookings.isEmpty()) booking = userBookings.get(0);
            }

            if (booking != null) {
                String reply = String.format("📋 **Booking Status for Code: %s**\n" +
                                             "• Guest: %s\n" +
                                             "• Hotel: %s (%s)\n" +
                                             "• Room Type: %s (%d room(s))\n" +
                                             "• Check-in: %s | Check-out: %s\n" +
                                             "• Total Amount: ₹%.2f\n" +
                                             "• Booking Status: %s",
                        booking.getBookingCode(), booking.getGuestName(), booking.getHotel().getName(),
                        booking.getHotel().getCity(), booking.getRoomType().getName(), booking.getNumRooms(),
                        booking.getCheckinDate(), booking.getCheckoutDate(), booking.getTotalCost(), booking.getStatus());
                response.setReply(reply);
            } else {
                response.setReply("Please provide your Booking Code (e.g. HE-2026-1001) or email address so I can fetch your booking status.");
            }
            return response;
        }

        // Intent 2: Room Search / Real Availability Inquiry
        if (msg.contains("available") || msg.contains("vacancy") || msg.contains("room rate") || msg.contains("price") || msg.contains("deluxe") || msg.contains("suite") || msg.contains("standard")) {
            response.setIntent("ROOM_SEARCH");
            List<Hotel> hotels = hotelRepository.findAll();

            if (hotels.isEmpty()) {
                response.setReply("We are currently updating our room catalog. Please try again shortly.");
                return response;
            }

            Hotel selectedHotel = hotels.get(0); // Default primary hotel
            List<RoomType> roomTypes = roomTypeRepository.findByHotelId(selectedHotel.getId());

            StringBuilder sb = new StringBuilder();
            sb.append("🏨 **Real-time Room Availability & Pricing at HotelEase**:\n\n");

            LocalDate checkin = LocalDate.now().plusDays(1);
            LocalDate checkout = checkin.plusDays(1);

            for (RoomType rt : roomTypes) {
                int booked = bookingRepository.countBookedRoomsInDateRange(rt.getId(), checkin, checkout);
                int available = Math.max(0, rt.getTotalRooms() - booked);

                sb.append(String.format("• **%s**: ₹%.0f/night | Max %d Adults | Available: %d rooms\n",
                        rt.getName(), rt.getBasePrice(), rt.getCapacityAdults(), available));
            }

            sb.append("\nWould you like to proceed to the Book page to select your dates?");
            response.setReply(sb.toString());
            return response;
        }

        // Intent 3: Cancellation Assistance
        if (msg.contains("cancel") || msg.contains("cancellation")) {
            response.setIntent("CANCELLATION");
            String bookingCode = extractBookingCode(msg);

            if (bookingCode != null) {
                Optional<Booking> opt = bookingRepository.findByBookingCode(bookingCode);
                if (opt.isPresent()) {
                    Booking b = opt.get();
                    b.setStatus("CANCELLED");
                    bookingRepository.save(b);
                    response.setReply("✅ Booking **" + bookingCode + "** has been successfully cancelled. Availability has been updated in our database.");
                } else {
                    response.setReply("Could not find booking with code: " + bookingCode);
                }
            } else {
                response.setReply("ℹ️ **Cancellation Policy**:\n" +
                                 "• Flexible rooms can be cancelled free of charge before check-in.\n" +
                                 "• To cancel directly, reply with: *Cancel HE-XXXXXX*");
            }
            return response;
        }

        // Intent 4: Guest Complaints & Service Requests (AC, Towels, WiFi, Water, Housekeeping, Maintenance)
        if (msg.contains("ac") || msg.contains("wifi") || msg.contains("towel") || msg.contains("water") ||
            msg.contains("leak") || msg.contains("clean") || msg.contains("shower") || msg.contains("food") ||
            msg.contains("problem") || msg.contains("not working") || msg.contains("repair")) {

            response.setIntent("SERVICE_REQUEST");

            String roomNo = request.getRoomNumber() != null ? request.getRoomNumber() : "104";

            ServiceRequestDTO dto = new ServiceRequestDTO();
            dto.setRoomNumber(roomNo);
            dto.setIssueDescription(request.getMessage());
            dto.setGuestEmail(request.getUserEmail());

            ServiceRequest req = prioritizationEngine.createServiceRequest(dto);

            String priorityTag = req.getPriority().equalsIgnoreCase("URGENT") ? "🚨 URGENT" :
                                 req.getPriority().equalsIgnoreCase("HIGH") ? "⚠️ HIGH" : "ℹ️ NORMAL";

            response.setReply(String.format("🛠️ **Guest Service Request Created** [Ticket #%d]\n" +
                                            "• Room: %s\n" +
                                            "• Category: %s\n" +
                                            "• Priority Level: %s\n" +
                                            "• Status: PENDING (Staff Dispatched)\n\n" +
                                            "Our duty manager has received your request and assigned a technician.",
                    req.getId(), req.getRoomNumber(), req.getCategory(), priorityTag));
            return response;
        }

        // Intent 5: Hotel Policies & Information FAQs
        if (msg.contains("check-in") || msg.contains("checkin") || msg.contains("checkout") || msg.contains("time")) {
            response.setIntent("POLICY_INFO");
            response.setReply("⏰ **Check-In & Check-Out Times**:\n• Check-in Time: 2:00 PM onwards\n• Check-out Time: 11:00 AM\n• Early check-in or late check-out is subject to room availability.");
            return response;
        }

        if (msg.contains("breakfast") || msg.contains("food") || msg.contains("restaurant") || msg.contains("menu")) {
            response.setIntent("AMENITY_INFO");
            response.setReply("🍽️ **Dining & Breakfast**:\n• Buffet Breakfast served daily from 7:00 AM to 10:30 AM.\n• Pure Vegetarian & International cuisine available.\n• Room Service available 24/7.");
            return response;
        }

        if (msg.contains("airport") || msg.contains("pickup") || msg.contains("cab") || msg.contains("taxi")) {
            response.setIntent("AMENITY_INFO");
            response.setReply("🚗 **Airport Transportation**:\n• Private airport pickup service available (+₹1,500).\n• You can select airport pickup during online booking or request it at front desk.");
            return response;
        }

        if (msg.contains("hello") || msg.contains("hi") || msg.contains("hey")) {
            response.setIntent("GREETING");
            response.setReply("Hello! I am your **HotelEase Virtual Guest Assistant** 🤖.\nHow can I help you today? You can ask about:\n1. Room availability & rates\n2. Booking status or cancellation\n3. Service complaints (AC, WiFi, Housekeeping)\n4. Check-in times & hotel policies");
            return response;
        }

        // General fallback with helpful menu
        response.setIntent("UNKNOWN");
        response.setReply("Thank you for contacting HotelEase Support! I am here to help you with:\n" +
                          "• *Room availability and rates*\n" +
                          "• *Booking status (e.g. status of HE-2026-1001)*\n" +
                          "• *Room issues (e.g. AC not cooling in room 104)*\n" +
                          "• *Check-in/out times & amenities*");
        return response;
    }

    private String extractBookingCode(String text) {
        if (text == null) return null;
        text = text.toUpperCase();
        int idx = text.indexOf("HE-");
        if (idx != -1) {
            String sub = text.substring(idx);
            String[] parts = sub.split("\\s+");
            return parts[0].replaceAll("[^A-Z0-9-]", "");
        }
        return null;
    }
}
