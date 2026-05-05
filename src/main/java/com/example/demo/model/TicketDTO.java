package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketDTO {
    private String bookingId;
    private String uuid;
    private String name;
    private String utr;
    private int rowIndex;
    private String status;
    private String ticketNumber;
    private String ticketCount;
    private String totalAmount;
    private String paymentType;
}
