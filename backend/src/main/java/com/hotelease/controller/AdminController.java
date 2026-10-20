package com.hotelease.controller;

import com.hotelease.dto.AdminDashboardDTO;
import com.hotelease.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Retrieves management dashboard KPI metrics.
     * Enforces strict server-side authorization check (Role must be ADMIN or STAFF).
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        
        if (roleHeader == null || (!roleHeader.equalsIgnoreCase("ADMIN") && !roleHeader.equalsIgnoreCase("STAFF"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "success", false,
                            "error", "FORBIDDEN_ACCESS",
                            "message", "Access Denied: Server-side verification requires ADMIN or STAFF credentials."
                    ));
        }

        AdminDashboardDTO metrics = adminService.getDashboardMetrics();
        return ResponseEntity.ok(metrics);
    }
}

