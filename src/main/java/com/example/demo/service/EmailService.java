package com.example.demo.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendTicketEmail(String to, String name, String bookingId, int ticketCount, double totalAmount, List<byte[]> qrImages) throws Exception {

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        String body = "<p>Hi " + name + ",</p>" +
                "<p><b>Booking ID:</b> " + bookingId + "</p>" +
                "<p><b>Tickets:</b> " + ticketCount + "</p>" +
                "<p><b>Amount Paid:</b> ₹" + totalAmount + "</p>" +
                "<br/>" +
                "<p>Your ticket QR codes are attached to this email.</p>" +
                "<p>Please keep this email available for entry verification.</p>";

        helper.setTo(to);
        helper.setSubject("🎫 Your Concert Tickets");
        helper.setText(body, true);

        // 👉 Attach each QR
        int count = 1;
        for (byte[] qr : qrImages) {

            helper.addAttachment(
                    "ticket_" + count + ".png",
                    new ByteArrayResource(qr)
            );

            count++;
        }

        mailSender.send(message);
    }
}
