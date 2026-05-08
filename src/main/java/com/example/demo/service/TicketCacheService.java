package com.example.demo.service;

import com.example.demo.model.TicketDTO;
import com.example.demo.model.TicketResponse;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.demo.utilities.TicketResponseStatusUtility.invalid;
import static com.example.demo.utilities.TicketResponseStatusUtility.valid;

@Service
public class TicketCacheService {

    @Autowired
    private GoogleSheetService googleSheetService;

    private static final Logger log = LoggerFactory.getLogger(TicketCacheService.class);

    @Cacheable("allTickets")
    public List<TicketDTO> getAllTickets() throws IOException {
        log.info("all tickets Cache working test");
        List<TicketDTO> result = new ArrayList<>();

        List<List<Object>> rows = googleSheetService.getRows();

        if (rows == null) return result;

        for (int i = 1; i < rows.size(); i++) {
            List<Object> row = rows.get(i);

            if (row.size() < 13) continue;

            TicketDTO ticket = new TicketDTO(
                    row.get(7).toString(), // bookingId
                    row.get(5).toString(), // uuid
                    row.get(1).toString(), // name
                    row.get(4).toString(), // utr
                    0,  // rowIndex
                    row.get(6).toString(),   // status
                    row.get(8).toString(), // ticketNumber
                    row.get(9).toString(),   // ticketCount
                    row.get(10).toString(),  // totalAmount
                    row.get(11).toString()
            );

            result.add(ticket);
        }

        return result;
    }

    @CacheEvict(value = {"pendingTickets", "allTickets"}, allEntries = true)
    public TicketResponse markCheckedIn(String uuid, String name) {

        try {
            googleSheetService.updateStatusByUuid("Checked In", uuid);
            log.info("Ticket checked-in, cache cleared");
            return valid(name);
        } catch (Exception e) {
            e.printStackTrace();
            return invalid();
        }
    }

    @CacheEvict(value = {"pendingTickets", "allTickets"}, allEntries = true)
    public void approveTicket(String uuid) throws IOException {
        googleSheetService.updateStatusByUuid("Valid", uuid);
        log.info("ticket approved");
    }

    @CacheEvict(value = {"pendingTickets", "allTickets"}, allEntries = true)
    public void rejectTicket(String uuid) throws IOException {
        googleSheetService.updateStatusByUuid("Rejected", uuid);
        log.info("Ticket Rejected, cache cleared");
    }

}
