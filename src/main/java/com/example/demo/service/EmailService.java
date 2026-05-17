package com.example.demo.service;

import com.example.demo.exceptions.EmailSendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${resend.api.key}")
    private String resendApiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.resend.com")
            .build();

    public void sendTicketEmail(
            String to,
            String name,
            String bookingId,
            int ticketCount,
            double totalAmount,
            byte[] pdfBytes
    ) throws Exception {

        String htmlBody = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                
                <div style="text-align: center; margin-bottom: 30px;">
                    <h1 style="color: #7c3aed; margin: 0;">🎵 The Notebook Concert</h1>
                    <p style="color: #666; margin-top: 5px;">Your tickets are confirmed!</p>
                </div>

                <div style="background: linear-gradient(135deg, #7c3aed, #db2777); 
                            padding: 20px; border-radius: 12px; text-align: center; margin-bottom: 25px;">
                    <h2 style="color: white; margin: 0;">🎫 Booking Confirmed!</h2>
                </div>

                <p style="font-size: 16px;">Hi <b>%s</b>,</p>
                <p>Thank you for booking! Your tickets are attached to this email as a PDF.</p>

                <table style="width:100%%; border-collapse: collapse; margin: 20px 0;">
                    <tr style="background: #f3f4f6;">
                        <td style="padding: 12px; border: 1px solid #e5e7eb; font-weight: bold;">Booking ID</td>
                        <td style="padding: 12px; border: 1px solid #e5e7eb; font-family: monospace; color: #7c3aed;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 12px; border: 1px solid #e5e7eb; font-weight: bold;">Tickets</td>
                        <td style="padding: 12px; border: 1px solid #e5e7eb;">%d</td>
                    </tr>
                    <tr style="background: #f3f4f6;">
                        <td style="padding: 12px; border: 1px solid #e5e7eb; font-weight: bold;">Amount Paid</td>
                        <td style="padding: 12px; border: 1px solid #e5e7eb; color: #16a34a; font-weight: bold;">₹%.0f</td>
                    </tr>
                </table>

                <div style="background: #fef9c3; border: 1px solid #fde047; 
                            padding: 15px; border-radius: 8px; margin: 20px 0;">
                    <p style="margin: 0; color: #854d0e;">
                        📎 <b>Your ticket PDF is attached.</b> Please download and keep it ready for entry.
                    </p>
                </div>

                <div style="background: #f3f4f6; padding: 15px; border-radius: 8px; margin: 20px 0;">
                    <p style="margin: 0; font-size: 13px; color: #666;">
                        💡 <b>Keep your Booking ID safe:</b> <span style="font-family: monospace; color: #7c3aed;">%s</span><br/>
                        You'll need it to download your ticket again if needed.
                    </p>
                </div>

                <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 25px 0;"/>
                <p style="color: #999; font-size: 12px; text-align: center;">
                    The Notebook Concert • thenotebookconcert.in
                </p>
            </div>
        """.formatted(name, bookingId, ticketCount, totalAmount, bookingId);

        // Attachment
        Map<String, String> attachment = new HashMap<>();
        attachment.put("filename", "The_NoteBook_Concert_Ticket_" + bookingId + ".pdf");
        attachment.put("content", Base64.getEncoder().encodeToString(pdfBytes));

        // Payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("from", "tickets@thenotebookconcert.in");
        payload.put("to", List.of(to));
        payload.put("subject", "🎫 Booking Confirmed - The Notebook Concert");
        payload.put("html", htmlBody);
        payload.put("attachments", List.of(attachment));

        webClient.post()
                .uri("/emails")
                .header("Authorization", "Bearer " + resendApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .onStatus(
                        status -> !status.is2xxSuccessful(),
                        response -> response.bodyToMono(String.class)
                                .map(error -> new EmailSendException("Failed to send email: " + error))
                )
                .bodyToMono(String.class)
                .doOnSuccess(res -> log.info("Email sent successfully to {}", to))
                .doOnError(err -> log.error("Email error for {}: {}", to, err.getMessage()))
                .block();
    }
}