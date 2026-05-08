package com.example.demo.service;

import com.cloudinary.Cloudinary;
import com.example.demo.controller.TicketController;
import com.example.demo.model.BookingRequest;
import com.example.demo.model.NewTicket;
import com.example.demo.utilities.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import com.cloudinary.Cloudinary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
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

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    @CacheEvict(value = {"pendingTickets", "allTickets"}, allEntries = true)
    public Map<String, Object> createBooking(BookingRequest req) throws Exception {

        log.info("Creating booking for request: {}", req.getName());

        String bookingId = UUIDUtil.generate();

        List<NewTicket> tickets = new ArrayList<>();
        List<byte[]> qrImages = new ArrayList<>();
        List<List<Object>> rows = new ArrayList<>();

        // -----------------------------
        // CREATE TICKET DATA
        // -----------------------------
        for (int i = 1; i <= req.getTicketCount(); i++) {

            String uuid = UUIDUtil.generate();

            byte[] ticketImage = qrService.generateTicketImage(uuid);

            qrImages.add(ticketImage);

            tickets.add(new NewTicket(uuid, i, bookingId));

            List<Object> row;

            if (req.getPaymentType().equalsIgnoreCase("FREE")) {

                row = new ArrayList<>(List.of(
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
                ));

            } else {

                row = new ArrayList<>(List.of(
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
                ));
            }

            rows.add(row);
        }

        // -----------------------------
        // GENERATE PDF
        // -----------------------------
        byte[] pdfBytes = pdfService.generateTicketPdf(
                bookingId,
                req.getName(),
                qrImages
        );

        // -----------------------------
        // CREATE TEMP FILE
        // -----------------------------
        String pdfFileName = "The_NoteBook_Concert_Ticket_" + bookingId + ".pdf";
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
        // ADD PDF URL INTO EACH ROW
        // -----------------------------
        for (List<Object> row : rows) {
            row.add(pdfUrl);
        }

        // -----------------------------
        // SAVE TO GOOGLE SHEETS
        // -----------------------------
        googleSheetService.batchUpdateRows(rows);

        // -----------------------------
        // RESPONSE
        // -----------------------------
        Map<String, Object> response = new HashMap<>();

        response.put("bookingId", bookingId);
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
}

