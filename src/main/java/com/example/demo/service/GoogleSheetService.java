package com.example.demo.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.time.format.DateTimeFormatter;

@Service
public class GoogleSheetService {

    private static final String SPREADSHEET_ID = "1gCedYeGOljKDsijAeu-kuLgnXg8YFJRANbMn0s2dAmk";
    private static final String SHEET_NAME = "Sheet1";
    private static final String RANGE = "Sheet1!A:N";
    private static final String STATUS_COLUMN = "G";

    @Autowired
    private Sheets sheetsService;

    DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm:ss");

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
        return response.getValues();
    }

    public void updateStatusByBookingId(
            String status,
            String bookingId
    ) throws Exception {

        List<List<Object>> rows = getRows();

        for (int i = 1; i < rows.size(); i++) {

            List<Object> row = rows.get(i);

            if (row.size() < 8) {
                continue;
            }

            String currentStatus = row.get(6).toString();

            if (!currentStatus.equalsIgnoreCase("Pending")) {
                continue;
            }

            String existingBookingId =
                    row.get(7).toString();

            if (bookingId.equals(existingBookingId)) {

                String range =
                        SHEET_NAME + "!" + STATUS_COLUMN + (i + 1);

                ValueRange body =
                        new ValueRange()
                                .setValues(List.of(List.of(status)));

                sheetsService.spreadsheets()
                        .values()
                        .update(
                                SPREADSHEET_ID,
                                range,
                                body
                        )
                        .setValueInputOption("RAW")
                        .execute();
            }
        }
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

    public int getActiveTicketCount(
            int reservationExpiryMinutes
    ) throws Exception {

        List<List<Object>> rows = getRows();

        int count = 0;

        for (int i = 1; i < rows.size(); i++) {

            List<Object> row = rows.get(i);

            if (row.size() < 7) {
                continue;
            }

            String status =
                    row.get(6).toString();

            if (status.equalsIgnoreCase("RESERVED")) {

                LocalDateTime expiresAt =
                        LocalDateTime.parse(row.get(13).toString(), formatter);

                if (expiresAt.isAfter(LocalDateTime.now())) {
                    count++;
                }

            } else if (status.equalsIgnoreCase("Pending") || status.equalsIgnoreCase("Valid") || status.equalsIgnoreCase("Checked In")) {
                count++;
            }
        }

        return count;
    }


    public void confirmReservation(
            String bookingId,
            List<String> uuids,
            String utr,
            String pdfUrl,
            String paymentType,
            Double totalAmount
    ) throws Exception {

        List<List<Object>> rows = getRows();

        int uuidIndex = 0;

        for (int i = 1; i < rows.size(); i++) {

            List<Object> row = rows.get(i);

            if (row.size() < 14) {
                continue;
            }

            String status =
                    row.get(6).toString();

            String existingBookingId =
                    row.get(7).toString();

            if (
                    status.equalsIgnoreCase("RESERVED")
                            &&
                            existingBookingId.equals(bookingId)
            ) {

                row.set(4, utr);

                row.set(5, uuids.get(uuidIndex));

                if (
                        "FREE".equalsIgnoreCase(paymentType)
                ) {
                    row.set(6, "Valid");
                } else {
                    row.set(6, "Pending");
                }

                row.set(10, totalAmount);
                row.set(12, pdfUrl);

                uuidIndex++;

                String range =
                        SHEET_NAME + "!A" + (i + 1) + ":N" + (i + 1);
                ;

                ValueRange body =
                        new ValueRange()
                                .setValues(List.of(row));

                sheetsService.spreadsheets()
                        .values()
                        .update(
                                SPREADSHEET_ID,
                                range,
                                body
                        )
                        .setValueInputOption("RAW")
                        .execute();
            }
        }
    }
    public void updateRow(
            int rowNumber,
            List<Object> rowData
    ) throws Exception {

        ValueRange body = new ValueRange()
                .setValues(List.of(rowData));

        sheetsService.spreadsheets().values()
                .update(
                        SPREADSHEET_ID,
                        SHEET_NAME + "!A" + rowNumber,
                        body
                )
                .setValueInputOption("RAW")
                .execute();
    }

    public void updateReservation(
            String bookingId,
            String name,
            String email,
            String phone
    ) throws Exception {

        List<List<Object>> values = getRows();

        for (int i = 1; i < values.size(); i++) {

            List<Object> row = values.get(i);

            if (row.size() < 8) {
                continue;
            }
            String existingBookingId = row.get(7).toString();

            if (bookingId.equals(existingBookingId)) {

                row.set(1, name);
                row.set(2, email);
                row.set(3, phone);

                updateRow(i + 1, row);
            }
        }
    }

    public boolean isAlreadyConfirmed(
            String bookingId
    ) throws Exception {

        List<List<Object>> rows = getRows();

        for (int i = 1; i < rows.size(); i++) {

            List<Object> row = rows.get(i);

            if (row.size() < 8) {
                continue;
            }

            String existingBookingId =
                    row.get(7).toString();

            String status =
                    row.get(6).toString();

            if (
                    bookingId.equals(existingBookingId)
                            &&
                            (
                                    status.equalsIgnoreCase("Pending")
                                            ||
                                            status.equalsIgnoreCase("Valid")
                            )
            ) {
                return true;
            }
        }

        return false;
    }

    public boolean isReservationExpired(String bookingId) throws Exception {

        List<List<Object>> rows = getRows();

        for (List<Object> row : rows) {

            if (row.size() < 14) continue;

            String existingBookingId = row.get(7).toString();

            if (bookingId.equals(existingBookingId)) {

                LocalDateTime expiresAt =
                        LocalDateTime.parse(row.get(13).toString(), formatter);

                return expiresAt.isBefore(LocalDateTime.now());
            }
        }

        return true;
    }
}

