package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewTicket {
    private String uuid;
    private int ticketNumber;
    private String bookingId;
}
