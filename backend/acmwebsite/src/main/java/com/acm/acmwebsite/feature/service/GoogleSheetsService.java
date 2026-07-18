package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.exception.*;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
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

            return spreadsheetUrl;
        } catch (GoogleJsonResponseException e) {
            handleGoogleException(e, "create spreadsheet");
        } catch (IOException e) {
            handleIOException(e, "creating spreadsheet");
        }
        return null;
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
            handleIOException(e, "clearing spreadsheet");
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
            handleIOException(e, "writing spreadsheet data");
        }
    }

    /**
     * Handles network or IO errors, checking for expired/revoked credentials.
     */
    private void handleIOException(IOException e, String operation) {
        log.error("Network/IO error during {}: {}", operation, e.getMessage(), e);

        Throwable cause = e;
        while (cause != null) {
            String msg = cause.getMessage();
            if (msg != null && (msg.contains("invalid_grant") || msg.contains("Token has been expired or revoked"))) {
                throw new GoogleSheetsCredentialsException(
                        "Google Sheets integration failed: OAuth refresh token is invalid, expired, or revoked. Please configure a new refresh token.", e);
            }
            cause = cause.getCause();
        }

        throw new GoogleSheetsException("Network error occurred while " + operation, e);
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
            throw new GoogleSheetsAcessDenialException(
                    "Access denied by Google APIs. Please check Google Console project API configuration.", e);
        } else if (code == 404) {
            throw new GoogleSheetsNotFoundException(
                    "Spreadsheet not found on Google Drive. It may have been permanently deleted.", e);
        } else {
            throw new GoogleSheetsException(
                    "Google Sheets integration failed during: " + operation + " (" + e.getMessage() + ")", e);
        }
    }

    /**
     * Generic helper to compile registration data and synchronize it with Google Sheets.
     */
    public <T, Q> String syncRegistrationData(
            String title,
            String folderId,
            String currentSpreadsheetUrl,
            List<String> prefixHeaders,
            List<T> registrations,
            List<Q> questions,
            java.util.function.Function<T, com.acm.acmwebsite.User_Authentication.entity.User> userExtractor,
            java.util.function.Function<T, Long> idExtractor,
            java.util.function.Function<T, java.util.Map<Long, String>> answersExtractor,
            java.util.function.Function<Q, Long> questionIdExtractor,
            java.util.function.Function<Q, String> questionTextExtractor
    ) {
        List<List<Object>> rows = new java.util.ArrayList<>();

        // Add prefix headers (metadata)
        if (prefixHeaders != null) {
            for (int i = 0; i < prefixHeaders.size(); i += 2) {
                if (i + 1 < prefixHeaders.size()) {
                    rows.add(java.util.Arrays.asList(prefixHeaders.get(i), prefixHeaders.get(i + 1)));
                } else {
                    rows.add(java.util.Collections.singletonList(prefixHeaders.get(i)));
                }
            }
            rows.add(java.util.Collections.emptyList());
        }

        // Add Column Headers
        List<Object> headers = new java.util.ArrayList<>(java.util.Arrays.asList(
                "#", "Registration ID", "Name", "Email", "Phone Number", "Is Alex Eng Student", "Batch", "Department"
        ));
        for (Q question : questions) {
            headers.add(questionTextExtractor.apply(question));
        }
        rows.add(headers);

        // Add Registrants Data
        int seqNum = 1;
        for (T reg : registrations) {
            com.acm.acmwebsite.User_Authentication.entity.User user = userExtractor.apply(reg);
            if (user == null) continue;

            List<Object> row = new java.util.ArrayList<>(java.util.Arrays.asList(
                    seqNum++,
                    idExtractor.apply(reg),
                    user.getName() != null ? user.getName() : "",
                    user.getEmail() != null ? user.getEmail() : "",
                    user.getPhoneNumber() != null ? user.getPhoneNumber() : "",
                    user.getIsAlexEngStudent() != null && user.getIsAlexEngStudent() ? "Yes" : "No",
                    user.getBatch() != null ? user.getBatch() : "",
                    user.getDepartment() != null ? user.getDepartment().name() : ""
            ));

            java.util.Map<Long, String> answers = answersExtractor.apply(reg);
            for (Q question : questions) {
                Long qId = questionIdExtractor.apply(question);
                String answer = answers != null ? answers.getOrDefault(qId, "") : "";
                row.add(answer);
            }
            rows.add(row);
        }

        // Write to Spreadsheet
        String spreadsheetId = extractSpreadsheetId(currentSpreadsheetUrl);
        boolean needsNewSheet = (spreadsheetId == null);

        if (!needsNewSheet) {
            try {
                clearSpreadsheet(spreadsheetId);
                writeSpreadsheetData(spreadsheetId, rows);
            } catch (GoogleSheetsNotFoundException e) {
                needsNewSheet = true;
            }
        }

        if (needsNewSheet) {
            String newUrl = createSpreadsheet(title, folderId);
            String newId = extractSpreadsheetId(newUrl);
            writeSpreadsheetData(newId, rows);
            return newUrl;
        }
        return currentSpreadsheetUrl;
    }
}

