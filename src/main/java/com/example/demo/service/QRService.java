package com.example.demo.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class QRService {

    public byte[] generateTicketImage(String ticketId) throws Exception {

        // 1. Load the Logo (This is your base template)
        BufferedImage baseTicket = ImageIO.read(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("ticket_logo_cropped.jpeg"))
        );

        int width = baseTicket.getWidth();
        int height = baseTicket.getHeight();

        // 2. Generate QR Code
        int qrSize = 180; // Adjusted to fit nicely inside the picture frame
        QRCodeWriter writer = new QRCodeWriter();

        // Add hints for better quality/error correction
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix matrix = writer.encode(ticketId, BarcodeFormat.QR_CODE, qrSize, qrSize, hints);
        BufferedImage qrImage = new BufferedImage(qrSize, qrSize, BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < qrSize; x++) {
            for (int y = 0; y < qrSize; y++) {
                // We use ARGB to allow transparency if needed,
                // but here we stick to black and white for scannability
                qrImage.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0x00FFFFFF);
            }
        }

        // 3. Prepare Graphics on the base ticket
        Graphics2D g = baseTicket.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 4. Calculate Position for QR (Centering it in the top image area)
        // Based on your logo, the "frame" is roughly in the top half
        int xPos = (width - qrSize) / 2;
        int yPos = 46; // Manual offset to align with the microphone image center

        // 5. Draw Rounded White Background (The "Card" look from your sample)
        int padding = 15;
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Float(
                xPos - padding,
                yPos - padding,
                qrSize + (padding * 2),
                qrSize + (padding * 2),
                30, 30));

        // 6. Draw the QR Image
        g.drawImage(qrImage, xPos, yPos, null);

        // 7. (Optional) Overwrite/Draw the Ticket ID text at the bottom
        // if you want to replace "The Notebook" or add to it.
        // g.setColor(Color.BLACK);
        // g.setFont(new Font("SansSerif", Font.BOLD, 12));
        // g.drawString(ticketId, 40, height - 50);

        g.dispose();

        // 8. Convert to byte[]
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(baseTicket, "png", baos);

        return baos.toByteArray();
    }
}