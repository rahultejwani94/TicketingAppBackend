package com.example.demo.service;

import com.example.demo.model.TicketDTO;
import com.example.demo.model.TicketResponse;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;

import static com.example.demo.utilities.TicketResponseStatusUtility.*;

@Service
public class TicketService {

    @Autowired
    private Sheets sheetsService;

    private static final String SPREADSHEET_ID = "1gCedYeGOljKDsijAeu-kuLgnXg8YFJRANbMn0s2dAmk";
    private static final String RANGE = "Sheet1!A2:L";

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    @Autowired
    private TicketCacheService ticketCacheService;

    public TicketResponse scanTicket(String uuid) {
        try {
            List<TicketDTO> tickets = ticketCacheService.getAllTickets();

            if (tickets == null) {
                return invalid();
            }

            Map<String, TicketDTO> map = tickets.stream()
                    .collect(Collectors.toMap(TicketDTO::getUuid, t -> t));

            TicketDTO ticket = map.get(uuid);

            if (ticket == null) return invalid();

            String status = ticket.getStatus();
            String name = ticket.getName();

            if ("Checked In".equalsIgnoreCase(status)) {
                return alreadyScanned(name);
            }

            if ("Pending".equalsIgnoreCase(status)) {
                return notApproved(name);
            }

            if ("Rejected".equalsIgnoreCase(status)) {
                return rejected();
            }

            if (!"Valid".equalsIgnoreCase(status)) {
                return invalid();
            }
            return ticketCacheService.markCheckedIn(ticket.getRowIndex(), name);
        } catch (Exception e) {
            e.printStackTrace();
            return invalid();
        }
    }

    @Cacheable("pendingTickets")
    public List<TicketDTO> getPendingTickets() throws IOException {
        log.info("pending tickets cache work test");
        List<TicketDTO> allTickets = ticketCacheService.getAllTickets(); // ✅ cached

        return allTickets.stream()
                .filter(ticket -> "Pending".equalsIgnoreCase(ticket.getStatus()))
                .collect(Collectors.toList());
    }

    public void approveTicket(String rowIndex) throws IOException {
        ticketCacheService.approveTicket(rowIndex);
    }
}
