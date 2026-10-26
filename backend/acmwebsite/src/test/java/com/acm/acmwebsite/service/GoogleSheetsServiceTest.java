package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Department;
import com.acm.acmwebsite.feature.exception.*;
import com.acm.acmwebsite.feature.service.GoogleSheetsService;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ClearValuesResponse;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleSheetsServiceTest {

    @Mock
    private ObjectProvider<Sheets> sheetsProvider;

    @Mock
    private ObjectProvider<Drive> driveProvider;

    @Mock
    private Sheets sheets;

    @Mock
    private Sheets.Spreadsheets spreadsheets;

    @Mock
    private Sheets.Spreadsheets.Values spreadsheetValues;

    @Mock
    private Sheets.Spreadsheets.Values.Clear clearValues;

    @Mock
    private Sheets.Spreadsheets.Values.Update updateValues;

    @Mock
    private Drive drive;

    @Mock
    private Drive.Files driveFiles;

    @Mock
    private Drive.Files.Create driveCreate;

    private GoogleSheetsService googleSheetsService;

    @BeforeEach
    void setUp() {
        when(sheetsProvider.getIfAvailable()).thenReturn(sheets);
        when(driveProvider.getIfAvailable()).thenReturn(drive);
        googleSheetsService = new GoogleSheetsService(sheetsProvider, driveProvider);
    }

    private void setupDriveMock() throws IOException {
        when(drive.files()).thenReturn(driveFiles);
        when(driveFiles.create(any(com.google.api.services.drive.model.File.class))).thenReturn(driveCreate);
        when(driveCreate.setFields(anyString())).thenReturn(driveCreate);
    }

    private void setupSheetsClearMock() throws IOException {
        when(sheets.spreadsheets()).thenReturn(spreadsheets);
        when(spreadsheets.values()).thenReturn(spreadsheetValues);
        when(spreadsheetValues.clear(anyString(), anyString(), any(ClearValuesRequest.class))).thenReturn(clearValues);
    }

    private void setupSheetsUpdateMock() throws IOException {
        when(sheets.spreadsheets()).thenReturn(spreadsheets);
        when(spreadsheets.values()).thenReturn(spreadsheetValues);
        when(spreadsheetValues.update(anyString(), anyString(), any(ValueRange.class))).thenReturn(updateValues);
        when(updateValues.setValueInputOption(anyString())).thenReturn(updateValues);
    }

    private GoogleJsonResponseException createMockGoogleException(int statusCode, String message) {
        GoogleJsonResponseException jsonEx = mock(GoogleJsonResponseException.class);
        when(jsonEx.getStatusCode()).thenReturn(statusCode);
        when(jsonEx.getMessage()).thenReturn(message);
        return jsonEx;
    }

    @Nested
    @DisplayName("Configuration & Spreadsheet ID Extraction Tests")
    class ConfigAndExtractionTests {

        @Test
        @DisplayName("checkConfigured - throws GoogleSheetsCredentialsException if sheetsService or driveService is missing")
        void checkConfigured_missingServices_throws() {
            ObjectProvider<Sheets> nullSheetsProvider = mock(ObjectProvider.class);
            ObjectProvider<Drive> nullDriveProvider = mock(ObjectProvider.class);

            when(nullSheetsProvider.getIfAvailable()).thenReturn(null);
            when(nullDriveProvider.getIfAvailable()).thenReturn(drive);
            GoogleSheetsService s1 = new GoogleSheetsService(nullSheetsProvider, nullDriveProvider);
            assertThrows(GoogleSheetsCredentialsException.class, () -> s1.createSpreadsheet("Title"));

            when(nullSheetsProvider.getIfAvailable()).thenReturn(sheets);
            when(nullDriveProvider.getIfAvailable()).thenReturn(null);
            GoogleSheetsService s2 = new GoogleSheetsService(nullSheetsProvider, nullDriveProvider);
            assertThrows(GoogleSheetsCredentialsException.class, () -> s2.createSpreadsheet("Title"));
        }

        @Test
        @DisplayName("extractSpreadsheetId - correctly extracts ID from URL or returns null")
        void extractSpreadsheetId_tests() {
            assertNull(googleSheetsService.extractSpreadsheetId(null));
            assertNull(googleSheetsService.extractSpreadsheetId("   "));
            assertNull(googleSheetsService.extractSpreadsheetId("https://google.com/search?q=test"));

            String id = googleSheetsService.extractSpreadsheetId("https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit");
            assertEquals("1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms", id);
        }

        @Test
        @DisplayName("buildMetadataRows - formats even, odd, and null label value pairs with trailing empty row")
        void buildMetadataRows_tests() {
            assertTrue(googleSheetsService.buildMetadataRows(null).isEmpty());

            List<List<Object>> evenRows = googleSheetsService.buildMetadataRows(List.of("Title", "Form 1", "Date", "2026-01-01"));
            assertEquals(3, evenRows.size());
            assertEquals(List.of("Title", "Form 1"), evenRows.get(0));
            assertEquals(List.of("Date", "2026-01-01"), evenRows.get(1));
            assertTrue(evenRows.get(2).isEmpty());

            List<List<Object>> oddRows = googleSheetsService.buildMetadataRows(List.of("Title", "Form 1", "OnlyKey"));
            assertEquals(3, oddRows.size());
            assertEquals(List.of("Title", "Form 1"), oddRows.get(0));
            assertEquals(List.of("OnlyKey"), oddRows.get(1));
            assertTrue(oddRows.get(2).isEmpty());
        }
    }

    @Nested
    @DisplayName("Create, Clear, and Write Tests")
    class CreateClearWriteTests {

        @Test
        @DisplayName("createSpreadsheet without folderId creates sheet at root")
        void createSpreadsheet_atRoot() throws IOException {
            setupDriveMock();
            com.google.api.services.drive.model.File createdFile = new com.google.api.services.drive.model.File().setId("sheet-123");
            when(driveCreate.execute()).thenReturn(createdFile);

            String url = googleSheetsService.createSpreadsheet("My Sheet");

            assertEquals("https://docs.google.com/spreadsheets/d/sheet-123/edit", url);

            ArgumentCaptor<com.google.api.services.drive.model.File> captor = ArgumentCaptor.forClass(com.google.api.services.drive.model.File.class);
            verify(driveFiles).create(captor.capture());
            assertEquals("My Sheet", captor.getValue().getName());
            assertNull(captor.getValue().getParents());
        }

        @Test
        @DisplayName("createSpreadsheet with blank folderId creates sheet at root")
        void createSpreadsheet_blankFolderId_createsAtRoot() throws IOException {
            setupDriveMock();
            com.google.api.services.drive.model.File createdFile = new com.google.api.services.drive.model.File().setId("sheet-blank");
            when(driveCreate.execute()).thenReturn(createdFile);

            String url = googleSheetsService.createSpreadsheet("My Sheet", "   ");

            assertEquals("https://docs.google.com/spreadsheets/d/sheet-blank/edit", url);

            ArgumentCaptor<com.google.api.services.drive.model.File> captor = ArgumentCaptor.forClass(com.google.api.services.drive.model.File.class);
            verify(driveFiles).create(captor.capture());
            assertNull(captor.getValue().getParents());
        }

        @Test
        @DisplayName("createSpreadsheet with folderId sets parent folder")
        void createSpreadsheet_insideFolder() throws IOException {
            setupDriveMock();
            com.google.api.services.drive.model.File createdFile = new com.google.api.services.drive.model.File().setId("sheet-456");
            when(driveCreate.execute()).thenReturn(createdFile);

            String url = googleSheetsService.createSpreadsheet("Folder Sheet", "folder-abc");

            assertEquals("https://docs.google.com/spreadsheets/d/sheet-456/edit", url);

            ArgumentCaptor<com.google.api.services.drive.model.File> captor = ArgumentCaptor.forClass(com.google.api.services.drive.model.File.class);
            verify(driveFiles).create(captor.capture());
            assertEquals(List.of("folder-abc"), captor.getValue().getParents());
        }

        @Test
        @DisplayName("clearSpreadsheet executes clear command on Sheet1!A:Z")
        void clearSpreadsheet_success() throws IOException {
            setupSheetsClearMock();
            when(clearValues.execute()).thenReturn(new ClearValuesResponse());

            googleSheetsService.clearSpreadsheet("sheet-123");

            verify(spreadsheetValues).clear(eq("sheet-123"), eq("Sheet1!A:Z"), any(ClearValuesRequest.class));
            verify(clearValues).execute();
        }

        @Test
        @DisplayName("writeSpreadsheetData executes update command with RAW value input option")
        void writeSpreadsheetData_success() throws IOException {
            setupSheetsUpdateMock();
            when(updateValues.execute()).thenReturn(new UpdateValuesResponse());

            List<List<Object>> data = List.of(List.of("Header1", "Header2"), List.of("Val1", "Val2"));
            googleSheetsService.writeSpreadsheetData("sheet-123", data);

            verify(spreadsheetValues).update(eq("sheet-123"), eq("Sheet1!A1"), any(ValueRange.class));
            verify(updateValues).setValueInputOption("RAW");
            verify(updateValues).execute();
        }

        @Test
        @DisplayName("createSpreadsheetWithData creates spreadsheet and writes data rows")
        void createSpreadsheetWithData_success() throws IOException {
            setupDriveMock();
            setupSheetsUpdateMock();

            com.google.api.services.drive.model.File createdFile = new com.google.api.services.drive.model.File().setId("sheet-789");
            when(driveCreate.execute()).thenReturn(createdFile);
            when(updateValues.execute()).thenReturn(new UpdateValuesResponse());

            List<List<Object>> rows = List.of(List.of("A", "B"));
            String url = googleSheetsService.createSpreadsheetWithData("New Data Sheet", "folder-1", rows);

            assertEquals("https://docs.google.com/spreadsheets/d/sheet-789/edit", url);
            verify(updateValues).execute();
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("handleGoogleException translates 429 to GoogleSheetsQuotaException")
        void handleGoogleException_429() throws IOException {
            setupDriveMock();
            GoogleJsonResponseException jsonEx = createMockGoogleException(429, "Quota exceeded");
            doThrow(jsonEx).when(driveCreate).execute();

            assertThrows(GoogleSheetsQuotaException.class, () -> googleSheetsService.createSpreadsheet("Sheet"));
        }

        @Test
        @DisplayName("handleGoogleException translates 403 to GoogleSheetsAcessDenialException")
        void handleGoogleException_403() throws IOException {
            setupDriveMock();
            GoogleJsonResponseException jsonEx = createMockGoogleException(403, "Forbidden");
            doThrow(jsonEx).when(driveCreate).execute();

            assertThrows(GoogleSheetsAcessDenialException.class, () -> googleSheetsService.createSpreadsheet("Sheet"));
        }

        @Test
        @DisplayName("handleGoogleException translates 404 to GoogleSheetsNotFoundException")
        void handleGoogleException_404() throws IOException {
            setupSheetsClearMock();
            GoogleJsonResponseException jsonEx = createMockGoogleException(404, "Not Found");
            doThrow(jsonEx).when(clearValues).execute();

            assertThrows(GoogleSheetsNotFoundException.class, () -> googleSheetsService.clearSpreadsheet("sheet-missing"));
        }

        @Test
        @DisplayName("handleGoogleException translates other codes to GoogleSheetsException")
        void handleGoogleException_500() throws IOException {
            setupSheetsUpdateMock();
            GoogleJsonResponseException jsonEx = createMockGoogleException(500, "Internal Server Error");
            doThrow(jsonEx).when(updateValues).execute();

            assertThrows(GoogleSheetsException.class, () -> googleSheetsService.writeSpreadsheetData("sheet-123", List.of()));
        }

        @Test
        @DisplayName("clearSpreadsheet translates GoogleJsonResponseException 429")
        void clearSpreadsheet_429() throws IOException {
            setupSheetsClearMock();
            doThrow(createMockGoogleException(429, "Too many requests")).when(clearValues).execute();
            assertThrows(GoogleSheetsQuotaException.class, () -> googleSheetsService.clearSpreadsheet("sheet-1"));
        }

        @Test
        @DisplayName("clearSpreadsheet translates GoogleJsonResponseException 403")
        void clearSpreadsheet_403() throws IOException {
            setupSheetsClearMock();
            doThrow(createMockGoogleException(403, "Access denied")).when(clearValues).execute();
            assertThrows(GoogleSheetsAcessDenialException.class, () -> googleSheetsService.clearSpreadsheet("sheet-1"));
        }

        @Test
        @DisplayName("clearSpreadsheet translates generic IOException")
        void clearSpreadsheet_ioException() throws IOException {
            setupSheetsClearMock();
            doThrow(new IOException("Socket closed")).when(clearValues).execute();
            assertThrows(GoogleSheetsException.class, () -> googleSheetsService.clearSpreadsheet("sheet-1"));
        }

        @Test
        @DisplayName("writeSpreadsheetData translates GoogleJsonResponseException 429")
        void writeSpreadsheetData_429() throws IOException {
            setupSheetsUpdateMock();
            doThrow(createMockGoogleException(429, "Too many requests")).when(updateValues).execute();
            assertThrows(GoogleSheetsQuotaException.class, () -> googleSheetsService.writeSpreadsheetData("sheet-1", List.of()));
        }

        @Test
        @DisplayName("writeSpreadsheetData translates GoogleJsonResponseException 403")
        void writeSpreadsheetData_403() throws IOException {
            setupSheetsUpdateMock();
            doThrow(createMockGoogleException(403, "Access denied")).when(updateValues).execute();
            assertThrows(GoogleSheetsAcessDenialException.class, () -> googleSheetsService.writeSpreadsheetData("sheet-1", List.of()));
        }

        @Test
        @DisplayName("writeSpreadsheetData translates GoogleJsonResponseException 404")
        void writeSpreadsheetData_404() throws IOException {
            setupSheetsUpdateMock();
            doThrow(createMockGoogleException(404, "Spreadsheet missing")).when(updateValues).execute();
            assertThrows(GoogleSheetsNotFoundException.class, () -> googleSheetsService.writeSpreadsheetData("sheet-1", List.of()));
        }

        @Test
        @DisplayName("writeSpreadsheetData translates generic IOException")
        void writeSpreadsheetData_ioException() throws IOException {
            setupSheetsUpdateMock();
            doThrow(new IOException("Pipe broken")).when(updateValues).execute();
            assertThrows(GoogleSheetsException.class, () -> googleSheetsService.writeSpreadsheetData("sheet-1", List.of()));
        }

        @Test
        @DisplayName("handleIOException translates invalid_grant cause to GoogleSheetsCredentialsException")
        void handleIOException_invalidGrant() throws IOException {
            setupDriveMock();
            IOException ioEx = new IOException("Error details: invalid_grant token");
            doThrow(ioEx).when(driveCreate).execute();

            assertThrows(GoogleSheetsCredentialsException.class, () -> googleSheetsService.createSpreadsheet("Sheet"));
        }

        @Test
        @DisplayName("handleIOException translates expired/revoked token cause to GoogleSheetsCredentialsException")
        void handleIOException_tokenExpired() throws IOException {
            setupDriveMock();
            IOException cause = new IOException("Token has been expired or revoked");
            IOException ioEx = new IOException("Failed", cause);
            doThrow(ioEx).when(driveCreate).execute();

            assertThrows(GoogleSheetsCredentialsException.class, () -> googleSheetsService.createSpreadsheet("Sheet"));
        }

        @Test
        @DisplayName("handleIOException handles null message in exception cause")
        void handleIOException_nullMessage() throws IOException {
            setupDriveMock();
            IOException ioEx = new IOException((String) null);
            doThrow(ioEx).when(driveCreate).execute();

            assertThrows(GoogleSheetsException.class, () -> googleSheetsService.createSpreadsheet("Sheet"));
        }

        @Test
        @DisplayName("handleIOException translates generic IOException to GoogleSheetsException")
        void handleIOException_generic() throws IOException {
            setupDriveMock();
            IOException ioEx = new IOException("Connection timed out");
            doThrow(ioEx).when(driveCreate).execute();

            assertThrows(GoogleSheetsException.class, () -> googleSheetsService.createSpreadsheet("Sheet"));
        }
    }

    @Nested
    @DisplayName("syncRegistrationData Integration Tests")
    class SyncRegistrationDataTests {

        record DummyRegistration(Long id, LocalDateTime registeredAt, User user, Map<Long, String> answers) {}
        record DummyQuestion(Long id, String text) {}

        @Test
        @DisplayName("syncRegistrationData creates new sheet when currentSpreadsheetUrl is null or sheet not found")
        void syncRegistrationData_createsNewSheet_whenUrlNullOrNotFound() throws IOException {
            setupDriveMock();
            setupSheetsUpdateMock();
            setupSheetsClearMock();

            com.google.api.services.drive.model.File createdFile = new com.google.api.services.drive.model.File().setId("new-sheet-id");
            when(driveCreate.execute()).thenReturn(createdFile);
            when(updateValues.execute()).thenReturn(new UpdateValuesResponse());

            User user1 = new User();
            user1.setName("Alice");
            user1.setEmail("alice@acm.org");
            user1.setPhoneNumber("01012345678");
            user1.setIsAlexEngStudent(true);
            user1.setBatch("2026");
            user1.setDepartment(Department.CCE);

            User user2 = new User();
            user2.setName(null);
            user2.setEmail(null);
            user2.setPhoneNumber(null);
            user2.setIsAlexEngStudent(false);
            user2.setBatch(null);
            user2.setDepartment(null);

            DummyRegistration reg1 = new DummyRegistration(101L, LocalDateTime.of(2026, 1, 15, 14, 30), user1, Map.of(1L, "Answer 1"));
            DummyRegistration reg2 = new DummyRegistration(102L, null, user2, null);
            DummyRegistration regNullUser = new DummyRegistration(103L, null, null, null);

            DummyQuestion q1 = new DummyQuestion(1L, "What is your track?");

            // Case A: currentSpreadsheetUrl is null
            String resultUrl1 = googleSheetsService.syncRegistrationData(
                    "Event Registrations",
                    "folder-123",
                    null,
                    List.of("Event", "Hackathon"),
                    List.of(reg1, reg2, regNullUser),
                    List.of(q1),
                    DummyRegistration::user,
                    DummyRegistration::id,
                    DummyRegistration::answers,
                    DummyQuestion::id,
                    DummyQuestion::text,
                    DummyRegistration::registeredAt
            );

            assertEquals("https://docs.google.com/spreadsheets/d/new-sheet-id/edit", resultUrl1);

            // Case B: currentSpreadsheetUrl is provided but clearSpreadsheet throws 404 (sheet was deleted on Drive)
            GoogleJsonResponseException notFoundEx = createMockGoogleException(404, "Not found");
            doThrow(notFoundEx).when(clearValues).execute();

            String resultUrl2 = googleSheetsService.syncRegistrationData(
                    "Event Registrations",
                    "folder-123",
                    "https://docs.google.com/spreadsheets/d/deleted-sheet/edit",
                    List.of("Event", "Hackathon"),
                    List.of(reg1),
                    List.of(q1),
                    DummyRegistration::user,
                    DummyRegistration::id,
                    DummyRegistration::answers,
                    DummyQuestion::id,
                    DummyQuestion::text,
                    DummyRegistration::registeredAt
            );

            assertEquals("https://docs.google.com/spreadsheets/d/new-sheet-id/edit", resultUrl2);
        }

        @Test
        @DisplayName("syncRegistrationData updates existing sheet when currentSpreadsheetUrl is valid")
        void syncRegistrationData_updatesExistingSheet() throws IOException {
            setupSheetsClearMock();
            setupSheetsUpdateMock();

            when(clearValues.execute()).thenReturn(new ClearValuesResponse());
            when(updateValues.execute()).thenReturn(new UpdateValuesResponse());

            User user = new User();
            user.setName("Bob");
            user.setIsAlexEngStudent(null);

            DummyRegistration reg = new DummyRegistration(201L, null, user, Collections.emptyMap());
            DummyQuestion q = new DummyQuestion(1L, "Question 1");

            String existingUrl = "https://docs.google.com/spreadsheets/d/existing-sheet-id/edit";
            String resultUrl = googleSheetsService.syncRegistrationData(
                    "Event Registrations",
                    "folder-123",
                    existingUrl,
                    List.of("Event", "CodeFest"),
                    List.of(reg),
                    List.of(q),
                    DummyRegistration::user,
                    DummyRegistration::id,
                    DummyRegistration::answers,
                    DummyQuestion::id,
                    DummyQuestion::text,
                    DummyRegistration::registeredAt
            );

            assertEquals(existingUrl, resultUrl);
            verify(spreadsheetValues).clear(eq("existing-sheet-id"), eq("Sheet1!A:Z"), any());
            verify(spreadsheetValues).update(eq("existing-sheet-id"), eq("Sheet1!A1"), any());
        }
    }
}
