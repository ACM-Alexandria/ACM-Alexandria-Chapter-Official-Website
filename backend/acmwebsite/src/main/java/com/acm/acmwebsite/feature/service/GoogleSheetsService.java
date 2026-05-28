package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.exception.*;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GoogleSheetsService {

    private static final Logger log = LoggerFactory.getLogger(GoogleSheetsService.class);

    private final Sheets sheetsService;
    private final Drive driveService;

    @Value("${google.sheets.share-email:}")
    private String shareEmail;

    public GoogleSheetsService(ObjectProvider<Sheets> sheetsProvider, ObjectProvider<Drive> driveProvider) {
        this.sheetsService = sheetsProvider.getIfAvailable();
        this.driveService = driveProvider.getIfAvailable();
    }

    /**
     * Checks if the Google APIs are fully configured.
     * Throws an exception if not.
     */
    private void checkConfigured() {
        if (sheetsService == null || driveService == null) {
            throw new GoogleSheetsCredentialsException(
                    "Google Sheets integration is not configured. Service Account key is missing.");
        }
    }

    /**
     * Extracts Spreadsheet ID from a standard Google Sheets URL.
     */
    public String extractSpreadsheetId(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        Pattern pattern = Pattern.compile("/d/([a-zA-Z0-9-_]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Creates a new Google Sheet and shares it with the configured email as a
     * Writer.
     */
    public String createSpreadsheet(String title) {
        return createSpreadsheet(title, null);
    }

    /**
     * Creates a new Google Sheet inside a specific folder and shares it with the
     * configured email as a Writer.
     */
    public String createSpreadsheet(String title, String folderId) {
        checkConfigured();

        if (shareEmail == null || shareEmail.trim().isEmpty()) {
            throw new GoogleSheetsCredentialsException(
                    "Google Sheets share-email is not configured. Cannot share file.");
        }

        try {
            if (folderId != null && !folderId.trim().isEmpty()) {
                log.info("Creating spreadsheet with title: {} inside folder: {}", title, folderId);
            } else {
                log.info("Creating spreadsheet with title: {} at root", title);
            }

            com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File()
                    .setName(title)
                    .setMimeType("application/vnd.google-apps.spreadsheet");

            if (folderId != null && !folderId.trim().isEmpty()) {
                fileMetadata.setParents(List.of(folderId.trim()));
            }

            com.google.api.services.drive.model.File createdFile = driveService.files().create(fileMetadata)
                    .setFields("id, webViewLink")
                    .execute();

            String spreadsheetId = createdFile.getId();
            String spreadsheetUrl = "https://docs.google.com/spreadsheets/d/" + spreadsheetId + "/edit";

            log.info("Spreadsheet created successfully. ID: {}. URL: {}", spreadsheetId, spreadsheetUrl);

            // Share the spreadsheet with editor access
            shareSpreadsheet(spreadsheetId, shareEmail);

            return spreadsheetUrl;
        } catch (GoogleJsonResponseException e) {
            handleGoogleException(e, "create spreadsheet");
        } catch (IOException e) {
            throw new GoogleSheetsException("Network error occurred while creating spreadsheet", e);
        }
        return null;
    }

    /**
     * Shares a spreadsheet with a specific user email as a writer.
     */
    private void shareSpreadsheet(String spreadsheetId, String email) throws IOException {
        log.info("Sharing spreadsheet {} with email: {} as writer.", spreadsheetId, email);
        try {
            Permission permission = new Permission()
                    .setType("user")
                    .setRole("writer")
                    .setEmailAddress(email);

            driveService.permissions().create(spreadsheetId, permission)
                    .setSendNotificationEmail(false)
                    .execute();
            log.info("Permissions updated successfully for spreadsheet {}", spreadsheetId);
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 403 || e.getStatusCode() == 400) {
                throw new GoogleSheetsSharingException("Failed to share sheet with email " + email
                        + ". Ensure it is a valid Google Account and domain sharing restrictions allow external sharing.",
                        e);
            }
            throw e;
        }
    }

    /**
     * Wipes all cell values in "Sheet1" of the spreadsheet.
     */
    public void clearSpreadsheet(String spreadsheetId) {
        checkConfigured();
        try {
            log.info("Clearing cell values in spreadsheet {}", spreadsheetId);
            sheetsService.spreadsheets().values()
                    .clear(spreadsheetId, "Sheet1!A:Z", new ClearValuesRequest())
                    .execute();
            log.info("Spreadsheet {} cleared successfully.", spreadsheetId);
        } catch (GoogleJsonResponseException e) {
            handleGoogleException(e, "clear spreadsheet");
        } catch (IOException e) {
            throw new GoogleSheetsException("Network error occurred while clearing spreadsheet", e);
        }
    }

    /**
     * Writes dynamic row data to a spreadsheet starting from Sheet1!A1.
     */
    public void writeSpreadsheetData(String spreadsheetId, List<List<Object>> data) {
        checkConfigured();
        try {
            log.info("Writing {} rows of data to spreadsheet {}", data.size(), spreadsheetId);
            ValueRange body = new ValueRange().setValues(data);

            sheetsService.spreadsheets().values()
                    .update(spreadsheetId, "Sheet1!A1", body)
                    .setValueInputOption("RAW")
                    .execute();
            log.info("Data written successfully to spreadsheet {}", spreadsheetId);
        } catch (GoogleJsonResponseException e) {
            handleGoogleException(e, "write spreadsheet data");
        } catch (IOException e) {
            throw new GoogleSheetsException("Network error occurred while writing spreadsheet data", e);
        }
    }

    /**
     * Translates Google API exception status codes into user-friendly custom
     * exceptions.
     */
    private void handleGoogleException(GoogleJsonResponseException e, String operation) {
        log.error("Google Sheets API error during {}: code={}, message={}", operation, e.getStatusCode(),
                e.getMessage());
        int code = e.getStatusCode();
        if (code == 429) {
            throw new GoogleSheetsQuotaException(
                    "Google Sheets API quota limit reached. Please wait a few minutes and try again.", e);
        } else if (code == 403) {
            throw new GoogleSheetsSharingException(
                    "Access denied by Google APIs. Please check Google Console project API configuration.", e);
        } else if (code == 404) {
            throw new GoogleSheetsNotFoundException(
                    "Spreadsheet not found on Google Drive. It may have been permanently deleted.", e);
        } else {
            throw new GoogleSheetsException(
                    "Google Sheets integration failed during: " + operation + " (" + e.getMessage() + ")", e);
        }
    }
}
