package com.example.demo.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.Collections;

@Configuration
public class GoogleSheetsConfig {

    @Bean
    public Sheets sheetsService() throws Exception {

        // 🔥 Read from ENV variable instead of file
        String base64Json = System.getenv("GOOGLE_CREDENTIALS");

        if (base64Json == null || base64Json.isEmpty()) {
            throw new RuntimeException("GOOGLE_CREDENTIALS env variable not set");
        }

        byte[] decodedJson = Base64.getDecoder().decode(base64Json);

        InputStream in = new ByteArrayInputStream(decodedJson);

        GoogleCredentials credentials = GoogleCredentials
                .fromStream(in)
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/spreadsheets"));

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName("Admin App").build();
    }
}
