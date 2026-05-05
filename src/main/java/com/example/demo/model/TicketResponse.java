package com.example.demo.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketResponse {
    private String status;
// VALID
// INVALID
// ALREADY_SCANNED
// NOT_APPROVED

    private String name;
}

