package com.example.demo.controller;

import com.example.demo.model.BookingRequest;
import com.example.demo.service.BookingService;
import com.example.demo.utilities.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<?> create(@RequestHeader(value = "Authorization", required = false) String authHeader, @RequestBody BookingRequest request) throws Exception {
        if ("FREE".equals(request.getPaymentType())) {

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            String token = authHeader.substring(7);

            try {
                JwtUtil.validateToken(token);
            } catch (Exception e) {
                return ResponseEntity.status(401).body("Invalid token");
            }
        }
        return ResponseEntity.ok(bookingService.createBooking(request));
    }
}
