package com.example.demo.controller;

import com.example.demo.model.BookingRequest;
import com.example.demo.service.BookingService;
import com.example.demo.service.PdfService;
import com.example.demo.utilities.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

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

    @GetMapping("/{bookingId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String bookingId) {

        log.info("Getting pdf for booking id: {}", bookingId);

        byte[] pdf = bookingService.getPdf(bookingId);

        if (pdf == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition",
                        "attachment; filename=ticket_" + bookingId + ".pdf")
                .body(pdf);
    }
}
