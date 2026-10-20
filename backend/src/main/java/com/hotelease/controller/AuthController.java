package com.hotelease.controller;

import com.hotelease.model.User;
import com.hotelease.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Development endpoint returning standard pre-seeded development accounts
     * for seamless testing of both User and Admin portals.
     */
    @GetMapping("/dev-accounts")
    public ResponseEntity<List<Map<String, String>>> getDevAccounts() {
        List<Map<String, String>> list = new ArrayList<>();

        Map<String, String> adminAcc = new HashMap<>();
        adminAcc.put("role", "ADMIN");
        adminAcc.put("email", "admin@hotelease.com");
        adminAcc.put("password", "admin123");
        adminAcc.put("label", "👑 System Admin (Full Management Access)");
        list.add(adminAcc);

        Map<String, String> customerAcc = new HashMap<>();
        customerAcc.put("role", "CUSTOMER");
        customerAcc.put("email", "rahul@example.com");
        customerAcc.put("password", "user123");
        customerAcc.put("label", "👤 Registered Customer (Rahul Verma)");
        list.add(customerAcc);

        Map<String, String> staffAcc = new HashMap<>();
        staffAcc.put("role", "STAFF");
        staffAcc.put("email", "rajesh@hotelease.com");
        staffAcc.put("password", "staff123");
        staffAcc.put("label", "🛠️ Hotel Staff / Technician (Rajesh Kumar)");
        list.add(staffAcc);

        return ResponseEntity.ok(list);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        if (email == null || password == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Email and password are required."));
        }

        Map<String, Object> response = new HashMap<>();

        Optional<User> opt = userRepository.findByEmail(email.trim());
        if (opt.isPresent()) {
            User user = opt.get();
            // Match against stored hash or development defaults
            boolean passwordMatches = password.equals(user.getPasswordHash()) ||
                    ("admin@hotelease.com".equalsIgnoreCase(email) && "admin123".equals(password)) ||
                    ("rahul@example.com".equalsIgnoreCase(email) && "user123".equals(password)) ||
                    ("rajesh@hotelease.com".equalsIgnoreCase(email) && "staff123".equals(password));

            if (passwordMatches) {
                response.put("success", true);
                response.put("userId", user.getId());
                response.put("name", user.getName());
                response.put("email", user.getEmail());
                response.put("role", user.getRole());
                return ResponseEntity.ok(response);
            }
        }

        response.put("success", false);
        response.put("message", "Invalid email or password. Please verify credentials.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody User newUser) {
        Map<String, Object> response = new HashMap<>();

        if (userRepository.existsByEmail(newUser.getEmail())) {
            response.put("success", false);
            response.put("message", "Email is already registered.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        newUser.setPasswordHash(newUser.getPasswordHash() != null ? newUser.getPasswordHash() : "user123");
        User saved = userRepository.save(newUser);

        response.put("success", true);
        response.put("userId", saved.getId());
        response.put("role", saved.getRole());
        response.put("message", "Account registered successfully.");
        return ResponseEntity.ok(response);
    }
}

