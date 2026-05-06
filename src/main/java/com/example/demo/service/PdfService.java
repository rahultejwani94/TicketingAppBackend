package com.example.demo.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

@Service
public class PdfService {

    private static final Logger log = LoggerFactory.getLogger(PdfService.class);

    public byte[] generateTicketPdf(String bookingId,
                                    String name,
                                    List<byte[]> qrImages) throws Exception {

        log.info("Generating ticket for : {}", name);

        Document document = new Document(PageSize.A4, 20, 20, 20, 20);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, out);
        document.open();

        Font value = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font idFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        InputStream logoStream = getClass()
                .getClassLoader()
                .getResourceAsStream("static/logo.png");
        if (logoStream == null) {
            throw new RuntimeException("Logo not found in resources");
        }

        byte[] logoBytes = logoStream.readAllBytes();

        Image logo = Image.getInstance(logoBytes);
        logo.scaleToFit(80, 80);   // reduce size

        PdfPTable header = getPdfPTable(logo, titleFont);

        document.add(header);
        LineSeparator line = new LineSeparator();
        document.add(new Chunk(line));

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(10);

        infoTable.addCell(getCell("Booking ID:", true));
        infoTable.addCell(getCell(bookingId, false));

        infoTable.addCell(getCell("Name:", true));
        infoTable.addCell(getCell(name, false));

        document.add(infoTable);

        infoTable.setSpacingAfter(10);

        int count = 1;

        for (byte[] qr : qrImages) {

            // =========================
            // TICKET BOX (IMPORTANT)
            // =========================
            PdfPTable ticketBox = new PdfPTable(1);
            ticketBox.setWidthPercentage(100);
            ticketBox.setSpacingBefore(10);
            ticketBox.setSpacingAfter(10);

            PdfPCell cell = new PdfPCell();
            cell.setBorder(Rectangle.BOX);
            cell.setBackgroundColor(new BaseColor(245, 245, 245));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(15);
            cell.setBorderWidth(1.5f);
            cell.setBorderColor(new BaseColor(200, 200, 200));

            // Ticket Title
            Paragraph ticketTitle = new Paragraph(
                    "🎟 Ticket " + count,
                    titleFont
            );

            // Optional: Ticket ID (you can replace with real uuid if you have)
            Paragraph ticketId = new Paragraph(
                    "Booking ID: " + bookingId,
                    idFont
            );

            // QR Image
            Image img = Image.getInstance(qr);
            img.scaleToFit(150, 150);
            img.setAlignment(Element.ALIGN_CENTER);

            Paragraph spacer = new Paragraph(" ");

            PdfPTable innerTable = new PdfPTable(2);
            innerTable.setWidthPercentage(100);
            innerTable.setWidths(new float[]{3, 1});

// LEFT SIDE (TEXT)
            PdfPCell left = new PdfPCell();
            left.setBorder(Rectangle.NO_BORDER);

            left.addElement(ticketTitle);
            left.addElement(ticketId);

// RIGHT SIDE (QR)
            PdfPCell right = new PdfPCell(img);
            right.setBorder(Rectangle.NO_BORDER);
            right.setHorizontalAlignment(Element.ALIGN_RIGHT);
            right.setVerticalAlignment(Element.ALIGN_MIDDLE);

// ADD BOTH
            innerTable.addCell(left);
            innerTable.addCell(right);

// ADD TO MAIN CELL
            cell.addElement(innerTable);

            // Add cell to table
            ticketBox.addCell(cell);

            // Add ticket box to document
            document.add(ticketBox);

            count++;
        }

        Paragraph footer = new Paragraph(
                "Please show this ticket at entry. Do not share QR code.",
                value
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(15);

        document.add(footer);

        document.close();
        return out.toByteArray();
    }

    private PdfPCell getCell(String text, boolean isBold) {
        Font font = isBold
                ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)
                : FontFactory.getFont(FontFactory.HELVETICA, 12);

        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        return cell;
    }

    private static PdfPTable getPdfPTable(Image logo, Font titleFont) throws DocumentException {
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setSpacingAfter(10);
        header.setWidths(new float[]{1, 3}); // more space to title

// Left: Logo
        PdfPCell logoCell = new PdfPCell(logo);
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);

// Right: Title
        Paragraph title = new Paragraph("EVENT TICKET", titleFont);

        PdfPCell titleCell = new PdfPCell(title);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        titleCell.setPaddingTop(20); // vertically center feel
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

// Add cells
        header.addCell(logoCell);
        header.addCell(titleCell);
        return header;
    }
}
