package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.dto.BugReportResponse;
import com.acm.acmwebsite.feature.dto.FeatureSuggestionResponse;
import com.acm.acmwebsite.feature.entity.BugReport;
import com.acm.acmwebsite.feature.entity.BugReportImage;
import com.acm.acmwebsite.feature.entity.FeatureSuggestion;
import com.acm.acmwebsite.feature.enums.FeedbackStatus;
import com.acm.acmwebsite.feature.repository.BugReportRepository;
import com.acm.acmwebsite.feature.repository.FeatureSuggestionRepository;
import com.acm.acmwebsite.feature.service.FeedbackService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeatureSuggestionRepository featureSuggestionRepository;

    @Mock
    private BugReportRepository bugReportRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    private User sampleUser;
    private UUID sampleUserId;

    @BeforeEach
    void setUp() {
        sampleUserId = UUID.randomUUID();
        sampleUser = new User();
        sampleUser.setId(sampleUserId);
        sampleUser.setName("John Doe");
        sampleUser.setEmail("john@acm.org");
    }

    // --- Feature Suggestions ---

    @Test
    @DisplayName("saveFeatureSuggestion - with valid user email should attach reporterId and save")
    void saveFeatureSuggestion_withValidUserEmail_shouldSetReporterId() {
        FeatureSuggestion fs = new FeatureSuggestion();
        fs.setName("Dark Mode");
        fs.setDescription("Add dark theme to portal");

        when(userRepository.findByEmail("john@acm.org")).thenReturn(Optional.of(sampleUser));
        when(featureSuggestionRepository.save(any(FeatureSuggestion.class))).thenAnswer(i -> i.getArgument(0));

        FeatureSuggestion result = feedbackService.saveFeatureSuggestion(fs, "john@acm.org");

        assertNotNull(result);
        assertEquals(sampleUserId, result.getReporterId());
        verify(userRepository, times(1)).findByEmail("john@acm.org");
        verify(featureSuggestionRepository, times(1)).save(fs);
    }

    @Test
    @DisplayName("saveFeatureSuggestion - user not found by email should save without setting reporterId")
    void saveFeatureSuggestion_userNotFound_shouldNotSetReporterId() {
        FeatureSuggestion fs = new FeatureSuggestion();
        fs.setName("Dark Mode");

        when(userRepository.findByEmail("unknown@acm.org")).thenReturn(Optional.empty());
        when(featureSuggestionRepository.save(any(FeatureSuggestion.class))).thenAnswer(i -> i.getArgument(0));

        FeatureSuggestion result = feedbackService.saveFeatureSuggestion(fs, "unknown@acm.org");

        assertNotNull(result);
        assertNull(result.getReporterId());
        verify(featureSuggestionRepository, times(1)).save(fs);
    }

    @Test
    @DisplayName("saveFeatureSuggestion - null or blank email should not look up user and save")
    void saveFeatureSuggestion_nullOrBlankEmail_shouldNotLookupUser() {
        FeatureSuggestion fs1 = new FeatureSuggestion();
        FeatureSuggestion fs2 = new FeatureSuggestion();

        when(featureSuggestionRepository.save(any(FeatureSuggestion.class))).thenAnswer(i -> i.getArgument(0));

        feedbackService.saveFeatureSuggestion(fs1, null);
        feedbackService.saveFeatureSuggestion(fs2, "   ");

        verify(userRepository, never()).findByEmail(any());
        verify(featureSuggestionRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("getAllFeatureSuggestions - maps reporter and status correctly")
    void getAllFeatureSuggestions_withReporterAndStatus() {
        FeatureSuggestion fs1 = new FeatureSuggestion();
        fs1.setId(1L);
        fs1.setReporterId(sampleUserId);
        fs1.setReporter(sampleUser);
        fs1.setName("Dark Mode");
        fs1.setDescription("Description 1");
        fs1.setStatus(FeedbackStatus.NEW);
        fs1.setCreatedAt(LocalDateTime.now());

        FeatureSuggestion fs2 = new FeatureSuggestion();
        fs2.setId(2L);
        fs2.setReporter(null);
        fs2.setName("Light Mode");
        fs2.setDescription("Description 2");
        fs2.setStatus(null);
        fs2.setCreatedAt(LocalDateTime.now());

        when(featureSuggestionRepository.findAllSorted()).thenReturn(List.of(fs1, fs2));

        List<FeatureSuggestionResponse> responses = feedbackService.getAllFeatureSuggestions();

        assertEquals(2, responses.size());
        assertEquals("John Doe", responses.get(0).getReporterName());
        assertEquals("john@acm.org", responses.get(0).getReporterEmail());
        assertEquals("NEW", responses.get(0).getStatus());

        assertNull(responses.get(1).getReporterName());
        assertNull(responses.get(1).getReporterEmail());
        assertNull(responses.get(1).getStatus());
    }

    @Test
    @DisplayName("toggleFeatureSuggestionStatus - toggles NEW to DONE and DONE to NEW")
    void toggleFeatureSuggestionStatus_toggleCorrectly() {
        FeatureSuggestion fs = new FeatureSuggestion();
        fs.setId(1L);
        fs.setStatus(FeedbackStatus.NEW);

        when(featureSuggestionRepository.findById(1L)).thenReturn(Optional.of(fs));
        when(featureSuggestionRepository.save(any(FeatureSuggestion.class))).thenAnswer(i -> i.getArgument(0));

        feedbackService.toggleFeatureSuggestionStatus(1L);
        assertEquals(FeedbackStatus.DONE, fs.getStatus());

        feedbackService.toggleFeatureSuggestionStatus(1L);
        assertEquals(FeedbackStatus.NEW, fs.getStatus());

        verify(featureSuggestionRepository, times(2)).save(fs);
    }

    @Test
    @DisplayName("toggleFeatureSuggestionStatus - not found should throw EntityNotFoundException")
    void toggleFeatureSuggestionStatus_notFound_shouldThrow() {
        when(featureSuggestionRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> feedbackService.toggleFeatureSuggestionStatus(99L));
        assertEquals("Feature suggestion not found with id: 99", ex.getMessage());
        verify(featureSuggestionRepository, never()).save(any());
    }

    // --- Bug Reports ---

    @Test
    @DisplayName("saveBugReport - with valid user email and images attaches reporterId, images and saves")
    void saveBugReport_withUserAndImages_shouldAttachAndSave() {
        BugReport br = new BugReport();
        br.setName("Crash on login");
        br.setDescription("Null pointer exception");

        List<String> imageUrls = List.of("https://example.com/err1.png", "https://example.com/err2.png");

        when(userRepository.findByEmail("john@acm.org")).thenReturn(Optional.of(sampleUser));
        when(bugReportRepository.save(any(BugReport.class))).thenAnswer(i -> i.getArgument(0));

        BugReport result = feedbackService.saveBugReport(br, imageUrls, "john@acm.org");

        assertNotNull(result);
        assertEquals(sampleUserId, result.getReporterId());
        assertEquals(2, result.getImages().size());
        assertEquals("https://example.com/err1.png", result.getImages().get(0).getImageUrl());
        verify(bugReportRepository, times(1)).save(br);
    }

    @Test
    @DisplayName("saveBugReport - user not found and null images should save cleanly")
    void saveBugReport_userNotFoundAndNullImages_shouldSave() {
        BugReport br = new BugReport();
        br.setName("404 Error");

        when(userRepository.findByEmail("unknown@acm.org")).thenReturn(Optional.empty());
        when(bugReportRepository.save(any(BugReport.class))).thenAnswer(i -> i.getArgument(0));

        BugReport result = feedbackService.saveBugReport(br, null, "unknown@acm.org");

        assertNotNull(result);
        assertNull(result.getReporterId());
        verify(bugReportRepository, times(1)).save(br);
    }

    @Test
    @DisplayName("saveBugReport - null or blank email should not lookup user")
    void saveBugReport_nullOrBlankEmail_shouldNotLookupUser() {
        BugReport br1 = new BugReport();
        BugReport br2 = new BugReport();

        when(bugReportRepository.save(any(BugReport.class))).thenAnswer(i -> i.getArgument(0));

        feedbackService.saveBugReport(br1, null, null);
        feedbackService.saveBugReport(br2, null, "   ");

        verify(userRepository, never()).findByEmail(any());
        verify(bugReportRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("getAllBugReports - maps reporter, images, and status correctly")
    void getAllBugReports_withReporterAndImages() {
        BugReport br1 = new BugReport();
        br1.setId(1L);
        br1.setReporterId(sampleUserId);
        br1.setReporter(sampleUser);
        br1.setName("Bug 1");
        br1.setDescription("Desc 1");
        br1.setStatus(FeedbackStatus.NEW);
        br1.setCreatedAt(LocalDateTime.now());
        br1.setImages(new ArrayList<>());
        br1.getImages().add(new BugReportImage(br1, "https://example.com/img1.png"));

        BugReport br2 = new BugReport();
        br2.setId(2L);
        br2.setReporter(null);
        br2.setName("Bug 2");
        br2.setDescription("Desc 2");
        br2.setStatus(null);
        br2.setCreatedAt(LocalDateTime.now());
        br2.setImages(new ArrayList<>());

        when(bugReportRepository.findAllSorted()).thenReturn(List.of(br1, br2));

        List<BugReportResponse> responses = feedbackService.getAllBugReports();

        assertEquals(2, responses.size());
        assertEquals("John Doe", responses.get(0).getReporterName());
        assertEquals("john@acm.org", responses.get(0).getReporterEmail());
        assertEquals(1, responses.get(0).getImageUrls().size());
        assertEquals("https://example.com/img1.png", responses.get(0).getImageUrls().get(0));
        assertEquals("NEW", responses.get(0).getStatus());

        assertNull(responses.get(1).getReporterName());
        assertNull(responses.get(1).getReporterEmail());
        assertTrue(responses.get(1).getImageUrls().isEmpty());
        assertNull(responses.get(1).getStatus());
    }

    @Test
    @DisplayName("toggleBugReportStatus - toggles NEW to DONE and DONE to NEW")
    void toggleBugReportStatus_toggleCorrectly() {
        BugReport br = new BugReport();
        br.setId(1L);
        br.setStatus(FeedbackStatus.NEW);

        when(bugReportRepository.findById(1L)).thenReturn(Optional.of(br));
        when(bugReportRepository.save(any(BugReport.class))).thenAnswer(i -> i.getArgument(0));

        feedbackService.toggleBugReportStatus(1L);
        assertEquals(FeedbackStatus.DONE, br.getStatus());

        feedbackService.toggleBugReportStatus(1L);
        assertEquals(FeedbackStatus.NEW, br.getStatus());

        verify(bugReportRepository, times(2)).save(br);
    }

    @Test
    @DisplayName("toggleBugReportStatus - not found should throw EntityNotFoundException")
    void toggleBugReportStatus_notFound_shouldThrow() {
        when(bugReportRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> feedbackService.toggleBugReportStatus(99L));
        assertEquals("Bug report not found with id: 99", ex.getMessage());
        verify(bugReportRepository, never()).save(any());
    }
}
