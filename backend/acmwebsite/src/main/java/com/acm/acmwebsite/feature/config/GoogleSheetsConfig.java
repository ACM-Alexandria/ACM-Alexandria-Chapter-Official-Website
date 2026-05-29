package com.acm.acmwebsite.feature.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

@Configuration
public class GoogleSheetsConfig {

    private static final Logger log = LoggerFactory.getLogger(GoogleSheetsConfig.class);

    @Value("${google.sheets.client-id:}")
    private String clientId;

    @Value("${google.sheets.client-secret:}")
    private String clientSecret;

    @Value("${google.sheets.refresh-token:}")
    private String refreshToken;

    private GoogleCredentials credentials;
    private boolean credentialsLoaded = false;

    private synchronized GoogleCredentials getCredentials() {
        if (credentialsLoaded) {
            return credentials;
        }
        credentialsLoaded = true;
        try {
            // Load OAuth 2.0 User Credentials (Client ID, Client Secret, Refresh Token).
            String envClientId = System.getenv("GOOGLE_CLIENT_ID");
            String envClientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
            String envRefreshToken = System.getenv("GOOGLE_REFRESH_TOKEN");

            String effectiveClientId = (envClientId != null && !envClientId.trim().isEmpty()) ? envClientId : clientId;
            String effectiveClientSecret = (envClientSecret != null && !envClientSecret.trim().isEmpty()) ? envClientSecret : clientSecret;
            String effectiveRefreshToken = (envRefreshToken != null && !envRefreshToken.trim().isEmpty()) ? envRefreshToken : refreshToken;

            if (effectiveClientId != null && !effectiveClientId.trim().isEmpty() &&
                effectiveClientSecret != null && !effectiveClientSecret.trim().isEmpty() &&
                effectiveRefreshToken != null && !effectiveRefreshToken.trim().isEmpty()) {
                
                log.info("Loading Google credentials using OAuth 2.0 User Credentials (client-id: {})...", effectiveClientId);
                GoogleCredentials creds = UserCredentials.newBuilder()
                        .setClientId(effectiveClientId)
                        .setClientSecret(effectiveClientSecret)
                        .setRefreshToken(effectiveRefreshToken)
                        .build()
                        .createScoped(Arrays.asList(
                                "https://www.googleapis.com/auth/spreadsheets",
                                "https://www.googleapis.com/auth/drive"
                        ));
                credentials = creds;
                return credentials;
            }
        } catch (Exception e) {
            log.error("Failed to load Google credentials: {}. Exception: {}", e.getMessage(), e.toString());
        }

        log.warn("Google credentials not configured or failed to load. Google Sheets export features will be unavailable.");
        return null;
    }

    @Bean
    public Sheets googleSheets() {
        GoogleCredentials credentials = getCredentials();
        if (credentials == null) {
            return null;
        }
        try {
            return new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName("ACM-Alexandria-Website")
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            log.error("Failed to initialize Google Sheets service: {}", e.getMessage());
            return null;
        }
    }

    @Bean
    public Drive googleDrive() {
        GoogleCredentials credentials = getCredentials();
        if (credentials == null) {
            return null;
        }
        try {
            return new Drive.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName("ACM-Alexandria-Website")
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            log.error("Failed to initialize Google Drive service: {}", e.getMessage());
            return null;
        }
    }
}
