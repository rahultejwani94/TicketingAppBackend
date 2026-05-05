package com.example.demo.controller;

import com.example.demo.model.TicketDTO;
import com.example.demo.model.TicketRequest;
import com.example.demo.model.TicketResponse;
import com.example.demo.service.TicketCacheService;
import com.example.demo.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
public class TicketController {

    private static final Logger log = LoggerFactory.getLogger(TicketController.class);

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketCacheService ticketCacheService;

    @PostMapping("/scan-ticket")
    public TicketResponse scanTicket(@RequestBody TicketRequest request) {
        log.info("Scan request received for UUID: {}", request.getUuid());
        long start = System.currentTimeMillis();
        TicketResponse ticketResponse = ticketService.scanTicket(request.getUuid());
        log.info("Execution time: {} ms", System.currentTimeMillis() - start);
        log.info("Scan result for {}: {}", request.getUuid(), ticketResponse.getStatus());
        return ticketResponse;
    }

    @GetMapping("/pending-tickets")
    public List<TicketDTO> getPendingTickets() throws IOException {
        log.info("Fetching pending tickets");
        long start = System.currentTimeMillis();
        List<TicketDTO> result = ticketService.getPendingTickets();
        log.info("Execution time: {} ms", System.currentTimeMillis() - start);
        log.info("Total pending tickets found: {}", result.size());
        return result;
    }

    @PostMapping("/approve-ticket")
    public String approveTicket(@RequestBody Map<String, String> request) throws IOException {
        log.info("Approving ticket at row: {}", request.get("rowIndex"));
        ticketService.approveTicket(request.get("rowIndex"));
        log.info("Ticket approved for row: {}", request.get("rowIndex"));
        return "SUCCESS";
    }

    @PostMapping("/reject-ticket")
    public String rejectTicket(@RequestBody Map<String, String> request) throws IOException {
        ticketCacheService.rejectTicket(request.get("rowIndex"));
        return "REJECTED";
    }

    @GetMapping("/all-tickets")
    public List<TicketDTO> getAllTickets() throws IOException {
        log.info("Fetching all tickets");
        long start = System.currentTimeMillis();
        List<TicketDTO> allTickets =  ticketCacheService.getAllTickets();
        log.info("Execution time: {} ms", System.currentTimeMillis() - start);
        log.info("Total tickets fetched: {}", allTickets.size());
        return allTickets;
    }
}

