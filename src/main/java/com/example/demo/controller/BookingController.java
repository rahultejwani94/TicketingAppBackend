package com.example.demo.controller;

import com.example.demo.model.BookingRequest;
import com.example.demo.service.BookingService;
import com.example.demo.service.PdfService;
import com.example.demo.utilities.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @GetMapping("/download/{bookingId}")
    public ResponseEntity<byte[]> downloadTicket(
            @PathVariable String bookingId
    ) {

        try {

            byte[] pdfBytes = bookingService.downloadPdfByBookingId(bookingId);

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=The_Notebook_Concert_Ticket_" + bookingId + ".pdf"
                    )
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception ex) {

            if ("PDF not found".equals(ex.getMessage())) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "PDF not found"
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Something went wrong"
            );
        }
    }

}
