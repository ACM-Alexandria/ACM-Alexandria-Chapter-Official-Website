package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.dto.HRFeedbackRequest;
import com.acm.acmwebsite.feature.dto.HRFeedbackResponse;
import com.acm.acmwebsite.feature.entity.HRFeedback;
import com.acm.acmwebsite.feature.enums.HRFeedbackType;
import com.acm.acmwebsite.feature.repository.HRFeedbackRepository;
import com.acm.acmwebsite.feature.service.HRFeedbackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HRFeedbackServiceTest {

    @Mock
    private HRFeedbackRepository hrFeedbackRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private HRFeedbackService hrFeedbackService;

    private User sampleUser;
    private UUID sampleUserId;

    @BeforeEach
    void setUp() {
        sampleUserId = UUID.randomUUID();
        sampleUser = new User();
        sampleUser.setId(sampleUserId);
        sampleUser.setName("Jane HR");
        sampleUser.setEmail("jane@acm.org");
    }

    @Test
    @DisplayName("saveFeedback - non-anonymous with valid email attaches reporter and returns details in response")
    void saveFeedback_nonAnonymous_withValidUser_shouldAttachReporter() {
        HRFeedbackRequest request = new HRFeedbackRequest();
        request.setType(HRFeedbackType.COMPLAINT);
        request.setContent("Issue with meeting scheduling");
        request.setIsAnonymous(false);

        when(userRepository.findByEmail("jane@acm.org")).thenReturn(Optional.of(sampleUser));
        when(hrFeedbackRepository.save(any(HRFeedback.class))).thenAnswer(i -> {
            HRFeedback fb = i.getArgument(0);
            fb.setId(10L);
            return fb;
        });

        HRFeedbackResponse response = hrFeedbackService.saveFeedback(request, "jane@acm.org");

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(HRFeedbackType.COMPLAINT, response.getType());
        assertEquals("Issue with meeting scheduling", response.getContent());
        assertFalse(response.getIsAnonymous());
        assertEquals("Jane HR", response.getReporterName());
        assertEquals("jane@acm.org", response.getReporterEmail());

        verify(userRepository, times(1)).findByEmail("jane@acm.org");
        verify(hrFeedbackRepository, times(1)).save(any(HRFeedback.class));
    }

    @Test
    @DisplayName("saveFeedback - non-anonymous with user not found in database")
    void saveFeedback_nonAnonymous_userNotFound_shouldSaveWithoutReporter() {
        HRFeedbackRequest request = new HRFeedbackRequest();
        request.setType(HRFeedbackType.SUGGESTION);
        request.setContent("Improve team bonding");
        request.setIsAnonymous(false);

        when(userRepository.findByEmail("missing@acm.org")).thenReturn(Optional.empty());
        when(hrFeedbackRepository.save(any(HRFeedback.class))).thenAnswer(i -> {
            HRFeedback fb = i.getArgument(0);
            fb.setId(11L);
            return fb;
        });

        HRFeedbackResponse response = hrFeedbackService.saveFeedback(request, "missing@acm.org");

        assertNotNull(response);
        assertEquals(11L, response.getId());
        assertFalse(response.getIsAnonymous());
        assertNull(response.getReporterName());
        assertNull(response.getReporterEmail());
    }

    @Test
    @DisplayName("saveFeedback - anonymous feedback should not lookup user or attach reporter even if email is given")
    void saveFeedback_anonymous_shouldNotAttachReporter() {
        HRFeedbackRequest request = new HRFeedbackRequest();
        request.setType(HRFeedbackType.SUGGESTION);
        request.setContent("Great leadership!");
        request.setIsAnonymous(true);

        when(hrFeedbackRepository.save(any(HRFeedback.class))).thenAnswer(i -> {
            HRFeedback fb = i.getArgument(0);
            fb.setId(12L);
            return fb;
        });

        HRFeedbackResponse response = hrFeedbackService.saveFeedback(request, "jane@acm.org");

        assertNotNull(response);
        assertTrue(response.getIsAnonymous());
        assertNull(response.getReporterName());
        assertNull(response.getReporterEmail());
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("saveFeedback - defaults isAnonymous to true when null in request")
    void saveFeedback_nullIsAnonymous_shouldDefaultToTrue() {
        HRFeedbackRequest request = new HRFeedbackRequest();
        request.setType(HRFeedbackType.SUGGESTION);
        request.setContent("Suggestion content");
        request.setIsAnonymous(null);

        when(hrFeedbackRepository.save(any(HRFeedback.class))).thenAnswer(i -> {
            HRFeedback fb = i.getArgument(0);
            fb.setId(13L);
            return fb;
        });

        HRFeedbackResponse response = hrFeedbackService.saveFeedback(request, "jane@acm.org");

        assertNotNull(response);
        assertTrue(response.getIsAnonymous());
        assertNull(response.getReporterName());
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("saveFeedback - non-anonymous with null or blank email should not lookup user")
    void saveFeedback_nonAnonymous_nullOrBlankEmail_shouldNotLookupUser() {
        HRFeedbackRequest request = new HRFeedbackRequest();
        request.setType(HRFeedbackType.SUGGESTION);
        request.setContent("Suggestion content");
        request.setIsAnonymous(false);

        when(hrFeedbackRepository.save(any(HRFeedback.class))).thenAnswer(i -> {
            HRFeedback fb = i.getArgument(0);
            fb.setId(14L);
            return fb;
        });

        HRFeedbackResponse r1 = hrFeedbackService.saveFeedback(request, null);
        HRFeedbackResponse r2 = hrFeedbackService.saveFeedback(request, "   ");

        assertNotNull(r1);
        assertNotNull(r2);
        assertNull(r1.getReporterName());
        assertNull(r2.getReporterName());
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("getAllFeedback - maps all entries correctly to responses")
    void getAllFeedback_shouldReturnMappedList() {
        HRFeedback fb1 = new HRFeedback();
        fb1.setId(1L);
        fb1.setType(HRFeedbackType.COMPLAINT);
        fb1.setContent("Complaint 1");
        fb1.setIsAnonymous(false);
        fb1.setCreatedAt(LocalDateTime.now());
        fb1.setReporter(sampleUser);

        HRFeedback fb2 = new HRFeedback();
        fb2.setId(2L);
        fb2.setType(HRFeedbackType.SUGGESTION);
        fb2.setContent("Suggestion 2");
        fb2.setIsAnonymous(true);
        fb2.setCreatedAt(LocalDateTime.now());

        when(hrFeedbackRepository.findAll()).thenReturn(List.of(fb1, fb2));

        List<HRFeedbackResponse> result = hrFeedbackService.getAllFeedback();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).getId());
        assertEquals("Jane HR", result.get(0).getReporterName());
        assertEquals("jane@acm.org", result.get(0).getReporterEmail());

        assertEquals(2L, result.get(1).getId());
        assertNull(result.get(1).getReporterName());
        assertNull(result.get(1).getReporterEmail());
        assertTrue(result.get(1).getIsAnonymous());

        verify(hrFeedbackRepository, times(1)).findAll();
    }
}
