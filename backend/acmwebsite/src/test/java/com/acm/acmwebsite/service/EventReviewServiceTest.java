package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.entity.Event;
import com.acm.acmwebsite.feature.entity.EventReview;
import com.acm.acmwebsite.feature.enums.ReviewStatus;
import com.acm.acmwebsite.feature.repository.EventRepository;
import com.acm.acmwebsite.feature.repository.EventReviewRepository;
import com.acm.acmwebsite.feature.repository.EventReviewRepository.EventRatingSummary;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventReviewServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private EventRepository eventRepository;
    @Mock private EventReviewRepository eventReviewRepository;
    @Mock private EventRegistrationService eventRegistrationService;

    @InjectMocks
    private EventReviewService service;

    private static final Long EVENT_ID = 1L;
    private final UUID userId = UUID.randomUUID();

    // =====================================================================
    // addReview
    // =====================================================================
    @Nested
    class AddReview {

        @ParameterizedTest
        @ValueSource(ints = {-1, 0, 6, 100})
        void ratingOutOfRange_throwsBadRequest_andTouchesNothing(int badRating) {
            assertThatThrownBy(() -> service.addReview(EVENT_ID, userId, badRating, "x"))
                    .isInstanceOfSatisfying(ResponseStatusException.class,
                            e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

            verifyNoInteractions(eventRepository, userRepository, eventReviewRepository, eventRegistrationService);
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 3, 5})
        void ratingBoundaries_areAccepted(int rating) {
            stubEventAndUser();
            when(eventRegistrationService.isUserRegistered(userId, EVENT_ID)).thenReturn(false);
            when(eventReviewRepository.save(any(EventReview.class))).thenAnswer(i -> i.getArgument(0));

            EventReview saved = service.addReview(EVENT_ID, userId, rating, "ok");

            assertThat(saved.getRating()).isEqualTo(rating);
        }

        @Test
        void eventNotFound_throwsNotFound() {
            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.addReview(EVENT_ID, userId, 5, "x"))
                    .isInstanceOfSatisfying(ResponseStatusException.class,
                            e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));

            verify(eventReviewRepository, never()).save(any());
        }

        @Test
        void userNotFound_throwsNotFound() {
            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(mock(Event.class)));
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.addReview(EVENT_ID, userId, 5, "x"))
                    .isInstanceOfSatisfying(ResponseStatusException.class,
                            e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));

            verify(eventReviewRepository, never()).save(any());
        }

        /**
         * Documents CURRENT behaviour: the service throws CONFLICT ("You already reviewed this event")
         * when eventRegistrationService.isUserRegistered(...) is true. The message suggests the intended
         * check is eventReviewRepository.existsByUserIdAndEventId(...) -- see the @Disabled test below.
         */
        @Test
        void userRegistered_currentlyThrowsConflict() {
            stubEventAndUser();
            when(eventRegistrationService.isUserRegistered(userId, EVENT_ID)).thenReturn(true);

            assertThatThrownBy(() -> service.addReview(EVENT_ID, userId, 5, "x"))
                    .isInstanceOfSatisfying(ResponseStatusException.class,
                            e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));

            verify(eventReviewRepository, never()).save(any());
        }

        /** Enable once the duplicate check uses eventReviewRepository.existsByUserIdAndEventId. */
        @Disabled("Service currently checks isUserRegistered instead of existsByUserIdAndEventId")
        @Test
        void duplicateReview_throwsConflict() {
            stubEventAndUser();
            when(eventReviewRepository.existsByUserIdAndEventId(userId, EVENT_ID)).thenReturn(true);

            assertThatThrownBy(() -> service.addReview(EVENT_ID, userId, 5, "x"))
                    .isInstanceOfSatisfying(ResponseStatusException.class,
                            e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
        }

        @Test
        void validReview_isSavedAsPrivateWithTrimmedComment() {
            Event event = mock(Event.class);
            User user = mock(User.class);
            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(eventRegistrationService.isUserRegistered(userId, EVENT_ID)).thenReturn(false);
            when(eventReviewRepository.save(any(EventReview.class))).thenAnswer(i -> i.getArgument(0));

            EventReview result = service.addReview(EVENT_ID, userId, 4, "   Great talk!  ");

            ArgumentCaptor<EventReview> captor = ArgumentCaptor.forClass(EventReview.class);
            verify(eventReviewRepository).save(captor.capture());
            EventReview saved = captor.getValue();

            assertThat(saved.getEvent()).isSameAs(event);
            assertThat(saved.getUser()).isSameAs(user);
            assertThat(saved.getRating()).isEqualTo(4);
            assertThat(saved.getComment()).isEqualTo("Great talk!");
            assertThat(saved.getReviewStatus()).isEqualTo(ReviewStatus.PRIVATE);
            assertThat(result).isSameAs(saved);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t\n"})
        void blankComment_isStoredAsNull(String blank) {
            stubEventAndUser();
            when(eventRegistrationService.isUserRegistered(userId, EVENT_ID)).thenReturn(false);
            when(eventReviewRepository.save(any(EventReview.class))).thenAnswer(i -> i.getArgument(0));

            EventReview saved = service.addReview(EVENT_ID, userId, 5, blank);

            assertThat(saved.getComment()).isNull();
        }

        @Test
        void nullComment_isStoredAsNull() {
            stubEventAndUser();
            when(eventRegistrationService.isUserRegistered(userId, EVENT_ID)).thenReturn(false);
            when(eventReviewRepository.save(any(EventReview.class))).thenAnswer(i -> i.getArgument(0));

            EventReview saved = service.addReview(EVENT_ID, userId, 5, null);

            assertThat(saved.getComment()).isNull();
        }

        private void stubEventAndUser() {
            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(mock(Event.class)));
            when(userRepository.findById(userId)).thenReturn(Optional.of(mock(User.class)));
        }
    }

    // =====================================================================
    // getRatingSummary
    // =====================================================================
    @Nested
    class GetRatingSummary {

        @Test
        void roundsAverageToOneDecimal() {
            when(eventReviewRepository.getRatingSummary(EVENT_ID)).thenReturn(summary(4.37, 10L));

            var result = service.getRatingSummary(EVENT_ID);

            assertThat(result.averageRating()).isEqualTo(4.4);
            assertThat(result.reviewCount()).isEqualTo(10L);
        }

        @Test
        void roundsDown_whenBelowHalf() {
            when(eventReviewRepository.getRatingSummary(EVENT_ID)).thenReturn(summary(4.32, 3L));

            assertThat(service.getRatingSummary(EVENT_ID).averageRating()).isEqualTo(4.3);
        }

        @Test
        void noReviews_nullsBecomeZero() {
            when(eventReviewRepository.getRatingSummary(EVENT_ID)).thenReturn(summary(null, null));

            var result = service.getRatingSummary(EVENT_ID);

            assertThat(result.averageRating()).isZero();
            assertThat(result.reviewCount()).isZero();
        }

        @Test
        void exactAverage_isUnchanged() {
            when(eventReviewRepository.getRatingSummary(EVENT_ID)).thenReturn(summary(5.0, 2L));

            assertThat(service.getRatingSummary(EVENT_ID).averageRating()).isEqualTo(5.0);
        }

        private EventRatingSummary summary(Double avg, Long count) {
            EventRatingSummary s = mock(EventRatingSummary.class);
            when(s.getAverageRating()).thenReturn(avg);
            when(s.getReviewCount()).thenReturn(count);
            return s;
        }
    }

    // =====================================================================
    // comments
    // =====================================================================
    @Nested
    class Comments {

        @Test
        void getPublicComments_queriesOnlyPublicStatus() {
            when(eventReviewRepository.findCommentsByEventIdAndStatus(EVENT_ID, ReviewStatus.PUBLIC))
                    .thenReturn(List.of("a", "b"));

            assertThat(service.getPublicComments(EVENT_ID)).containsExactly("a", "b");

            verify(eventReviewRepository).findCommentsByEventIdAndStatus(EVENT_ID, ReviewStatus.PUBLIC);
            verify(eventReviewRepository, never()).findCommentsByEventId(any());
        }

        @Test
        void getAllComments_returnsEveryComment() {
            when(eventReviewRepository.findCommentsByEventId(EVENT_ID)).thenReturn(List.of("pub", "priv"));

            assertThat(service.getAllComments(EVENT_ID, Role.SUPER_ADMIN)).containsExactly("pub", "priv");
        }

        /**
         * The comment above getAllComments says "only for admins/board", and the controller says
         * "Admins only", but the service never checks the role (unlike toggleVisibility).
         * Enable this once the check is added.
         */
        @Disabled("getAllComments does not currently enforce an admin role")
        @ParameterizedTest
        @MethodSource("com.acm.acmwebsite.feature.service.EventReviewServiceTest#nonAdminRoles")
        void getAllComments_nonAdmin_isForbidden(Role role) {
            assertThatThrownBy(() -> service.getAllComments(EVENT_ID, role))
                    .isInstanceOfSatisfying(ResponseStatusException.class,
                            e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));

            verifyNoInteractions(eventReviewRepository);
        }
    }

    // =====================================================================
    // toggleVisibility
    // =====================================================================
    @Nested
    class ToggleVisibility {

        @ParameterizedTest
        @MethodSource("com.acm.acmwebsite.feature.service.EventReviewServiceTest#nonAdminRoles")
        void nonAdmin_isForbidden_andRepositoryIsNotTouched(Role role) {
            assertThatThrownBy(() -> service.toggleVisibility(7L, role))
                    .isInstanceOfSatisfying(ResponseStatusException.class,
                            e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));

            verifyNoInteractions(eventReviewRepository);
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"SUPER_ADMIN", "ACM_HIGH_BOARD"})
        void admin_reviewNotFound_throwsNotFound(Role role) {
            when(eventReviewRepository.findById(7L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.toggleVisibility(7L, role))
                    .isInstanceOfSatisfying(ResponseStatusException.class,
                            e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"SUPER_ADMIN", "ACM_HIGH_BOARD"})
        void admin_privateBecomesPublic(Role role) {
            EventReview review = reviewWithStatus(ReviewStatus.PRIVATE);
            when(eventReviewRepository.findById(7L)).thenReturn(Optional.of(review));

            EventReview result = service.toggleVisibility(7L, role);

            assertThat(result.getReviewStatus()).isEqualTo(ReviewStatus.PUBLIC);
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"SUPER_ADMIN", "ACM_HIGH_BOARD"})
        void admin_publicBecomesPrivate(Role role) {
            EventReview review = reviewWithStatus(ReviewStatus.PUBLIC);
            when(eventReviewRepository.findById(7L)).thenReturn(Optional.of(review));

            EventReview result = service.toggleVisibility(7L, role);

            assertThat(result.getReviewStatus()).isEqualTo(ReviewStatus.PRIVATE);
        }

        @Test
        void togglingTwice_returnsToOriginalStatus() {
            EventReview review = reviewWithStatus(ReviewStatus.PRIVATE);
            when(eventReviewRepository.findById(7L)).thenReturn(Optional.of(review));

            service.toggleVisibility(7L, Role.SUPER_ADMIN);
            service.toggleVisibility(7L, Role.SUPER_ADMIN);

            assertThat(review.getReviewStatus()).isEqualTo(ReviewStatus.PRIVATE);
        }

        private EventReview reviewWithStatus(ReviewStatus status) {
            EventReview review = new EventReview();
            review.setReviewStatus(status);
            return review;
        }
    }

    // Every Role value that is NOT SUPER_ADMIN / ACM_HIGH_BOARD
    static Stream<Role> nonAdminRoles() {
        Set<Role> admins = Set.of(Role.SUPER_ADMIN, Role.ACM_HIGH_BOARD);
        return Arrays.stream(Role.values()).filter(r -> !admins.contains(r));
    }
}