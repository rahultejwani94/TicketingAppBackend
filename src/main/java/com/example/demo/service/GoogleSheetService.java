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
    private static final String RANGE = "Sheet1!A:L";

    @Autowired
    private Sheets sheetsService;

    public void appendRow(List<Object> row) throws IOException {
        ValueRange body = new ValueRange().setValues(List.of(row));

        sheetsService.spreadsheets().values()
                .append(SPREADSHEET_ID, RANGE, body)
                .setValueInputOption("RAW")
                .execute();
    }
}

