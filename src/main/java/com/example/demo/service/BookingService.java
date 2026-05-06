package com.example.demo.service;

import com.example.demo.controller.TicketController;
import com.example.demo.model.BookingRequest;
import com.example.demo.model.NewTicket;
import com.example.demo.utilities.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookingService {

    @Autowired
    private GoogleSheetService googleSheetService;

    @Autowired
    private QRService qrService;

    @Autowired
    private PdfService pdfService;

    private final Map<String, byte[]> pdfStore = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    @CacheEvict(value = {"pendingTickets", "allTickets"}, allEntries = true)
    public Map<String, Object> createBooking(BookingRequest req) throws Exception {
        log.info("Creating booking for request: {}", req.getName());

        String bookingId = UUIDUtil.generate();


        List<NewTicket> tickets = new ArrayList<>();

        List<byte[]> qrImages = new ArrayList<>();

        for (int i = 1; i <= req.getTicketCount(); i++) {

            String uuid = UUIDUtil.generate();

            byte[] ticketImage = qrService.generateTicketImage(uuid);
            qrImages.add(ticketImage);

            tickets.add(new NewTicket(uuid, i, bookingId));
            List<Object> row = null;
            if(req.getPaymentType().equalsIgnoreCase("FREE")){
                row = List.of(
                        LocalDateTime.now().toString(),
                        req.getName(),
                        req.getEmail(),
                        req.getPhone(),
                        req.getUtr(),
                        uuid,
                        "Valid",
                        bookingId,
                        i,
                        req.getTicketCount(),
                        0.0,
                        req.getPaymentType()
                );
            }
            else{
                row = List.of(
                    LocalDateTime.now().toString(),
                    req.getName(),
                    req.getEmail(),
                    req.getPhone(),
                    req.getUtr(),
                    uuid,
                    "Pending",
                    bookingId,
                    i,
                    req.getTicketCount(),
                    req.getTotalAmount(),
                    req.getPaymentType()
                );
            }

            googleSheetService.appendRow(row);
        }

        byte[] pdfBytes = pdfService.generateTicketPdf(
                bookingId,
                req.getName(),
                qrImages
        );

        // store it
        pdfStore.put(bookingId, pdfBytes);

        Map<String, Object> response = new HashMap<>();
        response.put("bookingId", bookingId);
        response.put("tickets", tickets);
        response.put("status", "Pending");

        return response;
    }

    public byte[] getPdf(String bookingId) {
        return pdfStore.get(bookingId);
    }
}

