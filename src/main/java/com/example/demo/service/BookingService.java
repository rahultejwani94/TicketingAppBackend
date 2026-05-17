package com.example.demo.service;

import com.example.demo.exceptions.BookingConflictException;
import com.example.demo.exceptions.ReservationExpiredException;
import com.example.demo.model.BookingRequest;
import com.example.demo.model.NewTicket;
import com.example.demo.utilities.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URL;

@Service
public class BookingService {

    @Autowired
    private GoogleSheetService googleSheetService;

    @Autowired
    private QRService qrService;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Value("${ticket.max.limit}")
    private int maxTickets;

    @Value("${reservation.expiry.minutes}")
    private int reservationExpiryMinutes;

    private final Object reservationLock = new Object();

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    @CacheEvict(value = {"pendingTickets", "allTickets"}, allEntries = true)
    public Map<String, Object> reserveBooking(
            BookingRequest req
    ) throws Exception {
        synchronized (reservationLock) {
            log.info("Reserving booking for request: {}", req.getName());

            int activeTickets =
                    googleSheetService.getActiveTicketCount(
                            reservationExpiryMinutes
                    );

            log.info("Current active tickets: {}, Max allowed tickets: {}", activeTickets, maxTickets);

            if (
                    activeTickets + req.getTicketCount()
                            > maxTickets
            ) {

                throw new BookingConflictException("HOUSEFULL");
            }

            String reservationId = UUIDUtil.generate();

            List<List<Object>> rows = new ArrayList<>();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
            ZonedDateTime expiresAt = now.plusMinutes(reservationExpiryMinutes);

            String nowStr = now.format(formatter);
            String expiresAtStr = expiresAt.format(formatter);

            for (int i = 1; i <= req.getTicketCount(); i++) {

                rows.add(new ArrayList<>(List.of(
                        nowStr,
                        req.getName(),
                        req.getEmail(),
                        req.getPhone(),
                        "N/A",
                        "N/A",
                        "Reserved",
                        reservationId,
                        i,
                        req.getTicketCount(),
                        req.getTotalAmount(),
                        req.getPaymentType(),
                        "N/A",
                        expiresAtStr
                )));
            }

            googleSheetService.batchUpdateRows(rows);

            Map<String, Object> response =
                    new HashMap<>();

            response.put("success", true);
            response.put("reservationId", reservationId);
            response.put("expiresAt", expiresAt.toInstant().toString());
            return response;
        }
    }

    @CacheEvict(value = {"pendingTickets", "allTickets"}, allEntries = true)
    public Map<String, Object> confirmReservation(String reservationId, BookingRequest req) throws Exception {

        log.info("Creating booking for request: {} and reservation: {}", req.getName(), reservationId);

        if (
                googleSheetService.isReservationExpired(
                        reservationId
                )
        ) {
            throw new ReservationExpiredException("Reservation expired");
        }

        if (
                googleSheetService.isAlreadyConfirmed(
                        reservationId
                )
        ) {
            throw new RuntimeException(
                    "Booking already confirmed"
            );
        }

        List<NewTicket> tickets = new ArrayList<>();
        List<byte[]> qrImages = new ArrayList<>();
        List<String> uuids = new ArrayList<>();

        // -----------------------------
        // CREATE TICKET DATA
        // -----------------------------
        for (int i = 1; i <= req.getTicketCount(); i++) {

            String uuid = UUIDUtil.generate();

            uuids.add(uuid);

            byte[] ticketImage = qrService.generateTicketImage(uuid);

            qrImages.add(ticketImage);

            tickets.add(new NewTicket(uuid, i, reservationId));
        }

        // -----------------------------
        // GENERATE PDF
        // -----------------------------
        byte[] pdfBytes = pdfService.generateTicketPdf(
                reservationId,
                req.getName(),
                qrImages
        );

        // -----------------------------
        // CREATE TEMP FILE
        // -----------------------------
        String pdfFileName = "The_NoteBook_Concert_Ticket_" + reservationId + ".pdf";
        File tempFile = File.createTempFile(pdfFileName, ".pdf");

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(pdfBytes);
        }

        // -----------------------------
        // UPLOAD PDF
        // -----------------------------
        String pdfUrl = cloudinaryService.uploadPdf(tempFile, pdfFileName);

        // -----------------------------
        // DELETE TEMP FILE
        // -----------------------------
        tempFile.delete();

        // -----------------------------
        // SAVE TO GOOGLE SHEETS
        // -----------------------------
        googleSheetService.confirmReservation(
                reservationId,
                uuids,
                req.getUtr(),
                pdfUrl,
                req.getPaymentType(),
                req.getTotalAmount()
        );

        // -----------------------------
        // RESPONSE
        // -----------------------------
        Map<String, Object> response = new HashMap<>();

        response.put("bookingId", reservationId);
        response.put("tickets", tickets);
        response.put("pdfUrl", pdfUrl);

        if (req.getPaymentType().equalsIgnoreCase("FREE")) {
            response.put("status", "Valid");
        } else {
            response.put("status", "Pending");
        }

        return response;
    }

    public byte[] downloadPdfByBookingId(String bookingId) throws Exception {
        // 1. get pdf url from google sheet
        String pdfUrl = googleSheetService.getPdfUrlByBookingId(bookingId);

        if (pdfUrl == null || pdfUrl.isBlank()) {
            throw new RuntimeException("PDF not found");
        }

        // 2. download bytes from cloudinary url
        URL url = new URL(pdfUrl);

        try (InputStream in = url.openStream()) {
            return in.readAllBytes();
        }
    }

    @CacheEvict(value = {"pendingTickets", "allTickets"}, allEntries = true)
    public Map<String, Object> updateReservation(
            String reservationId,
            BookingRequest req
    ) throws Exception {

        googleSheetService.updateReservation(
                reservationId,
                req.getName(),
                req.getEmail(),
                req.getPhone()
        );

        Map<String, Object> response = new HashMap<>();

        response.put("success", true);
        response.put("reservationId", reservationId);

        return response;
    }
}

