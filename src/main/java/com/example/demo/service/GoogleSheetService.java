package com.example.demo.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class GoogleSheetService {

    private static final String SPREADSHEET_ID = "1gCedYeGOljKDsijAeu-kuLgnXg8YFJRANbMn0s2dAmk";
    private static final String RANGE = "Sheet1!A:M";
    private static final String STATUS_COLUMN = "G";

    @Autowired
    private Sheets sheetsService;

    public void appendRow(List<Object> row) throws IOException {
        ValueRange body = new ValueRange().setValues(List.of(row));

        sheetsService.spreadsheets().values()
                .append(SPREADSHEET_ID, RANGE, body)
                .setValueInputOption("RAW")
                .execute();
    }

    public List<List<Object>> getRows() throws IOException {
        ValueRange response = sheetsService.spreadsheets().values()
                .get(SPREADSHEET_ID, RANGE)
                .execute();
        return  response.getValues();
    }

    public void updateStatusByUuid(String status, String uuid) throws IOException {
        List<List<Object>> rows = getRows();

        for (int i = 0; i < rows.size(); i++) {

            List<Object> row = rows.get(i);

            if (row.size() < 6) continue;

            String rowUuid = row.get(5).toString();
            System.out.println("Checking row " + i + " uuid=" + rowUuid);
            if (rowUuid.equals(uuid)) {

                String range = "Sheet1!" + STATUS_COLUMN + (i + 1); // still need sheet row internally
                System.out.println("MATCH FOUND at row: " + (i + 1));
                ValueRange body = new ValueRange()
                        .setValues(List.of(List.of(status)));

                sheetsService.spreadsheets().values()
                        .update(SPREADSHEET_ID, range, body)
                        .setValueInputOption("RAW")
                        .execute();

                return;
            }
        }
    }

    public void batchUpdateRows(List<List<Object>> rows) throws Exception {

        ValueRange body = new ValueRange()
                .setValues(rows);

        sheetsService.spreadsheets().values()
                .append(
                        SPREADSHEET_ID,
                        RANGE,
                        body
                )
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .execute();
    }

    public String getPdfUrlByBookingId(String bookingId) throws Exception {

        ValueRange response = sheetsService.spreadsheets().values()
                .get(
                        SPREADSHEET_ID,
                        "Sheet1!A2:M"
                )
                .execute();

        List<List<Object>> values = response.getValues();

        if (values == null) return null;

        for (List<Object> row : values) {

            // bookingId column index
            String rowBookingId = row.get(7).toString();

            if (bookingId.equals(rowBookingId)) {

                // pdf url column index
                return row.get(12).toString();
            }
        }

        return null;
    }
}

