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

    private static final String SPREADSHEET_ID = "1gCedYeGOljKDsijAeu-kuLgnXg8YFJRANbMn0s2dAmk";
    private static final String RANGE = "Sheet1!A2:L";

    @Autowired
    private Sheets sheetsService;

    private static final Logger log = LoggerFactory.getLogger(TicketCacheService.class);

    @Cacheable("allTickets")
    public List<TicketDTO> getAllTickets() throws IOException {
        log.info("all tickets Cache working test");
        List<TicketDTO> result = new ArrayList<>();

        ValueRange response = sheetsService.spreadsheets().values()
                .get(SPREADSHEET_ID, RANGE)
                .execute();

        List<List<Object>> rows = response.getValues();

        if (rows == null) return result;

        for (int i = 0; i < rows.size(); i++) {
            List<Object> row = rows.get(i);

            if (row.size() < 12) continue;

            TicketDTO ticket = new TicketDTO(
                    row.get(7).toString(), // bookingId
                    row.get(5).toString(), // uuid
                    row.get(1).toString(), // name
                    row.get(4).toString(), // utr
                    i + 2,  // rowIndex
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
    public TicketResponse markCheckedIn(int rowIndex, String name) {

        try {
            String updateRange = "Sheet1!G" + rowIndex;

            ValueRange body = new ValueRange()
                    .setValues(List.of(List.of("Checked In")));

            sheetsService.spreadsheets().values()
                    .update(SPREADSHEET_ID, updateRange, body)
                    .setValueInputOption("RAW")
                    .execute();

            log.info("Ticket checked-in, cache cleared");

            return valid(name);

        } catch (Exception e) {
            e.printStackTrace();
            return invalid();
        }
    }

    @CacheEvict(value = {"pendingTickets", "allTickets"}, allEntries = true)
    public void approveTicket(String rowIndex) throws IOException {
        log.info("ticket approved");
        String updateRange = "Sheet1!G" + rowIndex;

        ValueRange body = new ValueRange()
                .setValues(List.of(List.of("Valid")));

        sheetsService.spreadsheets().values()
                .update(SPREADSHEET_ID, updateRange, body)
                .setValueInputOption("RAW")
                .execute();
    }

    @CacheEvict(value = {"pendingTickets", "allTickets"}, allEntries = true)
    public void rejectTicket(String rowIndex) throws IOException {

        String updateRange = "Sheet1!G" + rowIndex; // status column

        ValueRange body = new ValueRange()
                .setValues(List.of(List.of("Rejected")));

        sheetsService.spreadsheets().values()
                .update(SPREADSHEET_ID, updateRange, body)
                .setValueInputOption("RAW")
                .execute();

        log.info("Ticket Rejected, cache cleared");
    }

}
