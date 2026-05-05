package com.example.demo.model;

import lombok.Data;

@Data
public class BookingRequest {
    private String name;
    private String email;
    private String phone;
    private String utr;
    private int ticketCount;
    private double totalAmount;
    private String paymentType;
}

