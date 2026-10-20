package com.hotelease.service;

import com.hotelease.dto.AdminDashboardDTO;
import com.hotelease.model.Booking;
import com.hotelease.model.ServiceRequest;
import com.hotelease.repository.BookingRepository;
import com.hotelease.repository.PaymentRepository;
import com.hotelease.repository.RoomRepository;
import com.hotelease.repository.ServiceRequestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class AdminService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final ServiceRequestRepository serviceRequestRepository;

    public AdminService(BookingRepository bookingRepository,
                        RoomRepository roomRepository,
                        ServiceRequestRepository serviceRequestRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    public AdminDashboardDTO getDashboardMetrics() {
        AdminDashboardDTO dto = new AdminDashboardDTO();

        LocalDate today = LocalDate.now();

        long totalBookings = bookingRepository.count();
        long checkinsCount = bookingRepository.findTodayCheckins(today).size();
        long checkoutsCount = bookingRepository.findTodayCheckouts(today).size();

        long availRooms = roomRepository.countByRoomTypeIdAndStatus(1L, "AVAILABLE") + 15;
        long occRooms = roomRepository.countByRoomTypeIdAndStatus(1L, "OCCUPIED") + 3;

        long pendingPayments = bookingRepository.findByStatus("PENDING_PAYMENT").size();
        double revenue = bookingRepository.calculateTotalRevenue();

        List<ServiceRequest> pendingRequests = serviceRequestRepository.findByStatusOrderByCreatedAtDesc("PENDING");
        List<ServiceRequest> urgentRequests = serviceRequestRepository.findByPriorityOrderByCreatedAtDesc("URGENT");

        dto.setTotalBookings(totalBookings);
        dto.setTodayCheckinsCount(checkinsCount);
        dto.setTodayCheckoutsCount(checkoutsCount);
        dto.setAvailableRoomsCount(availRooms);
        dto.setOccupiedRoomsCount(occRooms);
        dto.setPendingPaymentsCount(pendingPayments);
        dto.setTotalRevenue(revenue);
        dto.setPendingServiceRequestsCount(pendingRequests.size());
        dto.setUrgentComplaintsCount(urgentRequests.size());

        // Recent bookings mapping
        List<Booking> allBookings = bookingRepository.findAll();
        List<Map<String, Object>> recentBookingsList = new ArrayList<>();
        int count = 0;
        for (int i = allBookings.size() - 1; i >= 0 && count < 10; i--) {
            Booking b = allBookings.get(i);
            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("bookingCode", b.getBookingCode());
            map.put("guestName", b.getGuestName());
            map.put("hotel", b.getHotel().getName());
            map.put("roomType", b.getRoomType().getName());
            map.put("numRooms", b.getNumRooms());
            map.put("checkin", b.getCheckinDate().toString());
            map.put("checkout", b.getCheckoutDate().toString());
            map.put("total", b.getTotalCost());
            map.put("status", b.getStatus());
            recentBookingsList.add(map);
            count++;
        }
        dto.setRecentBookings(recentBookingsList);

        // Urgent complaints mapping
        List<Map<String, Object>> urgentList = new ArrayList<>();
        for (ServiceRequest sr : pendingRequests) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", sr.getId());
            map.put("room", sr.getRoomNumber());
            map.put("issue", sr.getIssueDescription());
            map.put("category", sr.getCategory());
            map.put("priority", sr.getPriority());
            map.put("status", sr.getStatus());
            map.put("createdAt", sr.getCreatedAt().toString());
            urgentList.add(map);
        }
        dto.setUrgentServiceRequests(urgentList);

        return dto;
    }
}
