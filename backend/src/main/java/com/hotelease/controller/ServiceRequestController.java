package com.hotelease.controller;

import com.hotelease.dto.ServiceRequestDTO;
import com.hotelease.model.ServiceRequest;
import com.hotelease.service.ServiceRequestPrioritizationEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-requests")
@CrossOrigin(origins = "*")
public class ServiceRequestController {

    private final ServiceRequestPrioritizationEngine prioritizationEngine;

    public ServiceRequestController(ServiceRequestPrioritizationEngine prioritizationEngine) {
        this.prioritizationEngine = prioritizationEngine;
    }

    @PostMapping
    public ResponseEntity<ServiceRequest> createRequest(@RequestBody ServiceRequestDTO dto) {
        ServiceRequest req = prioritizationEngine.createServiceRequest(dto);
        return ResponseEntity.ok(req);
    }

    @GetMapping
    public ResponseEntity<List<ServiceRequest>> getAllRequests() {
        return ResponseEntity.ok(prioritizationEngine.getAllRequests());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ServiceRequest> updateStatus(@PathVariable("id") Long id,
                                                        @RequestParam(value = "status", required = false) String status,
                                                        @RequestParam(value = "staffId", required = false) Long staffId,
                                                        @RequestParam(value = "notes", required = false) String notes) {
        ServiceRequest updated = prioritizationEngine.updateRequestStatus(id, status, staffId, notes);
        return ResponseEntity.ok(updated);
    }
}
