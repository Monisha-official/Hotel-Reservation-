package com.hotelease.controller;

import com.hotelease.dto.PaymentOrderDTO;
import com.hotelease.dto.PaymentVerificationDTO;
import com.hotelease.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<PaymentOrderDTO> createOrder(@RequestParam("bookingId") Long bookingId,
                                                      @RequestParam(value = "paymentMethod", defaultValue = "razorpay") String paymentMethod) {
        PaymentOrderDTO order = paymentService.createPaymentOrder(bookingId, paymentMethod);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(@RequestBody PaymentVerificationDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = paymentService.verifyPayment(dto);
            if (success) {
                response.put("success", true);
                response.put("message", "Payment verified and booking confirmed successfully!");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Payment signature verification failed.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Payment verification error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
