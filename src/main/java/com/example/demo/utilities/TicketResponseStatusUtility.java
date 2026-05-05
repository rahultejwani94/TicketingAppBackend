package com.example.demo.utilities;

import com.example.demo.model.TicketResponse;

public class TicketResponseStatusUtility {
    public static TicketResponse valid(String name) {
        return TicketResponse.builder()
                .status("VALID")
                .name(name)
                .build();
    }

    public static TicketResponse alreadyScanned(String name) {
        return TicketResponse.builder()
                .status("ALREADY_SCANNED")
                .name(name)
                .build();
    }

    public static TicketResponse invalid() {
        return TicketResponse.builder()
                .status("INVALID")
                .name(null)
                .build();
    }

    public static TicketResponse rejected() {
        return TicketResponse.builder()
                .status("REJECTED")
                .name(null)
                .build();
    }

    public static TicketResponse notApproved(String name) {
        return TicketResponse.builder()
                .status("NOT_APPROVED")
                .name(name)
                .build();
    }
}

