package com.hotelease.service;

import com.hotelease.dto.ServiceRequestDTO;
import com.hotelease.model.Booking;
import com.hotelease.model.ServiceRequest;
import com.hotelease.model.User;
import com.hotelease.repository.BookingRepository;
import com.hotelease.repository.ServiceRequestRepository;
import com.hotelease.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServiceRequestPrioritizationEngine {

    private final ServiceRequestRepository serviceRequestRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public ServiceRequestPrioritizationEngine(ServiceRequestRepository serviceRequestRepository,
                                                BookingRepository bookingRepository,
                                                UserRepository userRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    /**
     * Determines priority dynamically using intelligent keyword rule classifier.
     */
    public String classifyPriority(String description) {
        if (description == null) return "LOW";
        String desc = description.toLowerCase();

        if (desc.contains("fire") || desc.contains("flood") || desc.contains("major leak") ||
            desc.contains("power cut") || desc.contains("blackout") || desc.contains("short circuit") ||
            desc.contains("locked out") || desc.contains("emergency") || desc.contains("gas leak")) {
            return "URGENT";
        }

        if (desc.contains("ac") || desc.contains("air condition") || desc.contains("cooling") ||
            desc.contains("hot water") || desc.contains("toilet") || desc.contains("shower broken") ||
            desc.contains("flush") || desc.contains("keycard") || desc.contains("smell") || desc.contains("leak")) {
            return "HIGH";
        }

        if (desc.contains("wifi") || desc.contains("internet") || desc.contains("tv") ||
            desc.contains("housekeeping") || desc.contains("delayed") || desc.contains("clean room") ||
            desc.contains("bedsheet") || desc.contains("dust")) {
            return "MEDIUM";
        }

        return "LOW";
    }

    public String classifyCategory(String description) {
        if (description == null) return "FRONT_DESK";
        String desc = description.toLowerCase();

        if (desc.contains("ac") || desc.contains("leak") || desc.contains("power") ||
            desc.contains("water") || desc.contains("shower") || desc.contains("tv") || desc.contains("electricity")) {
            return "MAINTENANCE";
        }
        if (desc.contains("towel") || desc.contains("clean") || desc.contains("bedsheet") ||
            desc.contains("housekeeping") || desc.contains("pillow") || desc.contains("soap")) {
            return "HOUSEKEEPING";
        }
        if (desc.contains("food") || desc.contains("breakfast") || desc.contains("tea") ||
            desc.contains("coffee") || desc.contains("drink") || desc.contains("water bottle")) {
            return "ROOM_SERVICE";
        }
        if (desc.contains("lock") || desc.contains("key") || desc.contains("stolen") || desc.contains("safe")) {
            return "SECURITY";
        }
        return "FRONT_DESK";
    }

    @Transactional
    public ServiceRequest createServiceRequest(ServiceRequestDTO dto) {
        ServiceRequest request = new ServiceRequest();

        Booking booking = null;
        if (dto.getBookingId() != null) {
            booking = bookingRepository.findById(dto.getBookingId()).orElse(null);
        }

        request.setBooking(booking);
        request.setRoomNumber(dto.getRoomNumber() != null ? dto.getRoomNumber() : "101");
        request.setIssueDescription(dto.getIssueDescription());

        String category = (dto.getCategory() != null && !dto.getCategory().trim().isEmpty())
                ? dto.getCategory().toUpperCase()
                : classifyCategory(dto.getIssueDescription());

        String priority = (dto.getPriority() != null && !dto.getPriority().trim().isEmpty())
                ? dto.getPriority().toUpperCase()
                : classifyPriority(dto.getIssueDescription());

        request.setCategory(category);
        request.setPriority(priority);
        request.setStatus("PENDING");

        return serviceRequestRepository.save(request);
    }

    public List<ServiceRequest> getAllRequests() {
        return serviceRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public ServiceRequest updateRequestStatus(Long requestId, String status, Long staffId, String resolutionNotes) {
        ServiceRequest req = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Service request not found."));

        if (status != null && !status.trim().isEmpty()) {
            req.setStatus(status.toUpperCase());
        }

        if (staffId != null) {
            User staff = userRepository.findById(staffId).orElse(null);
            req.setAssignedStaff(staff);
        }

        if (resolutionNotes != null) {
            req.setResolutionNotes(resolutionNotes);
        }

        if ("RESOLVED".equalsIgnoreCase(status)) {
            req.setResolvedAt(LocalDateTime.now());
        }

        return serviceRequestRepository.save(req);
    }
}
