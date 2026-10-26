package com.acm.acmwebsite.service;

import com.acm.acmwebsite.feature.dto.CustomEmailRequestDto;
import com.acm.acmwebsite.feature.dto.CustomEmailPreviewDto;
import com.acm.acmwebsite.feature.dto.CustomEmailPreviewRequestDto;
import com.acm.acmwebsite.feature.dto.CustomEmailResultDto;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.feature.service.AdminEmailService;
import com.acm.acmwebsite.feature.service.MemberTargetingService;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acm.acmwebsite.feature.exception.EmailsLockedException;
import com.acm.acmwebsite.feature.service.SystemSettingsService;

@ExtendWith(MockitoExtension.class)
class AdminEmailServiceTest {

    @Mock
    private MemberTargetingService memberTargetingService;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private SystemSettingsService systemSettingsService;

    @InjectMocks
    private AdminEmailService adminEmailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adminEmailService, "senderEmail", "sender@acm.org");
    }

    @Test
    void sendCustomEmailBatchesRecipientsAsBlindCopies() throws Exception {
        stubCustomEmailTemplate();
        when(mailSender.createMimeMessage())
                .thenAnswer(ignored -> new MimeMessage(Session.getInstance(new Properties())));
        List<String> recipients = IntStream.range(0, 51)
                .mapToObj(index -> "member" + index + "@acm.org")
                .toList();
        when(memberTargetingService.findRecipientEmails(any(), any(), any())).thenReturn(recipients);
        CustomEmailRequestDto request = request();

        CustomEmailResultDto result = adminEmailService.sendCustomEmail(request);

        ArgumentCaptor<MimeMessage> messages = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(2)).send(messages.capture());
        assertEquals(51, result.getTotalRecipients());
        assertEquals("Email sent to 51 recipient(s).", result.getMessage());
        assertEquals(50, messages.getAllValues().get(0).getRecipients(Message.RecipientType.BCC).length);
        assertEquals(1, messages.getAllValues().get(1).getRecipients(Message.RecipientType.BCC).length);
        assertNull(messages.getAllValues().get(0).getRecipients(Message.RecipientType.TO));
        assertEquals("sender@acm.org", messages.getAllValues().get(0).getFrom()[0].toString());
        assertEquals("Chapter update", messages.getAllValues().get(0).getSubject());
    }

    @Test
    void sendCustomEmailDeduplicatesMatchingFilterAndSelectedRecipients() throws Exception {
        stubCustomEmailTemplate();
        when(mailSender.createMimeMessage())
                .thenAnswer(ignored -> new MimeMessage(Session.getInstance(new Properties())));
        when(memberTargetingService.findRecipientEmails(any(), any(), any()))
                .thenReturn(List.of("shared@acm.org", "filtered@acm.org"));

        CustomEmailRequestDto request = request();
        request.setSelectedUserEmails(List.of("shared@acm.org", "manual@acm.org"));

        CustomEmailResultDto result = adminEmailService.sendCustomEmail(request);

        assertEquals(3, result.getTotalRecipients());
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void previewCustomEmailPrioritizesSelectedSearchRecipientsWhenTheyMatchFilters() {
        stubCustomEmailTemplate();
        User selected = User.builder().email("shared@acm.org").role(Role.ACM_MEMBER).build();
        User filtered = User.builder().email("filtered@acm.org").role(Role.ACM_MEMBER).build();
        when(memberTargetingService.findMembers(any(), any(), any())).thenReturn(List.of(
                new MemberTargetingService.TargetedMember(selected, null, null, null, null),
                new MemberTargetingService.TargetedMember(filtered, null, null, null, null)));
        CustomEmailPreviewRequestDto request = new CustomEmailPreviewRequestDto();
        request.setSubject("Chapter update");
        request.setBody("<p>Hello <strong>members</strong><script>alert(1)</script></p>");
        request.setRoles(List.of(Role.ACM_MEMBER));
        request.setSelectedUserEmails(List.of("  ", "SHARED@acm.org"));

        CustomEmailPreviewDto preview = adminEmailService.previewCustomEmail(request);

        assertEquals(2, preview.getTotalRecipients());
        assertEquals(Map.of("ACM Member", 1L, "Selected recipients", 1L), preview.getRecipientCounts());
        assertEquals("<html>ACM custom email</html>", preview.getEmailHtml());

        ArgumentCaptor<Context> context = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("mail/custom-email"), context.capture());
        String sanitizedBody = (String) context.getValue().getVariable("bodyHtml");
        assertTrue(sanitizedBody.contains("<strong>members</strong>"));
        assertFalse(sanitizedBody.contains("<script>"));
    }

    @Test
    void previewCustomEmailAllowsOnlySearchRecipients() {
        stubCustomEmailTemplate();
        CustomEmailPreviewRequestDto request = new CustomEmailPreviewRequestDto();
        request.setSubject("Chapter update");
        request.setBody("<p>Hello members</p>");
        request.setSelectedUserEmails(List.of("manual@acm.org"));

        CustomEmailPreviewDto preview = adminEmailService.previewCustomEmail(request);

        assertEquals(1, preview.getTotalRecipients());
        assertEquals(Map.of("Selected recipients", 1L), preview.getRecipientCounts());
        verify(memberTargetingService, never()).findMembers(any(), any(), any());
    }

    @Test
    void customEmailTemplateUsesTheSharedMailLayout() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);

        Context context = new Context(Locale.ENGLISH);
        context.setVariable("emailTitle", "Chapter update");
        context.setVariable("preheaderText", "Chapter update");
        context.setVariable("emailSubject", "Chapter update");
        context.setVariable("bodyHtml", "<p>Message content</p>");
        context.setVariable("showEmailPreferences", false);
        context.setVariable("websiteUrl", "https://alex.hosting.acm.org/");

        String html = engine.process("mail/custom-email", context);

        assertTrue(html.contains("Alexandria Student Chapter"));
        assertTrue(html.contains("Chapter update"), html);
        assertTrue(html.contains("<p>Message content</p>"));
        assertTrue(html.contains("Connect With Us"));
        assertTrue(html.contains("https://alex.hosting.acm.org/"));
        assertFalse(html.contains("Unsubscribe"));
        assertFalse(html.contains("Preferences"));
    }

    @Test
    void sendCustomEmailRejectsAnEmptyRecipientSet() {
        when(memberTargetingService.findRecipientEmails(any(), any(), any())).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> adminEmailService.sendCustomEmail(request()));

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendCustomEmailThrowsWhenEmailsLocked() {
        org.mockito.Mockito.doThrow(new EmailsLockedException()).when(systemSettingsService).assertEmailsEnabled();

        assertThrows(EmailsLockedException.class, () -> adminEmailService.sendCustomEmail(request()));

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void previewCustomEmailThrowsWhenRolesAndRecipientsAreEmpty() {
        CustomEmailPreviewRequestDto request = new CustomEmailPreviewRequestDto();
        request.setRoles(List.of());
        request.setSelectedUserEmails(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> adminEmailService.previewCustomEmail(request));
        assertEquals("Select at least one role or recipient.", ex.getMessage());
    }

    @Test
    void previewCustomEmailCoversAllRoleLabelsAndFiltersNullEmail() {
        stubCustomEmailTemplate();
        User superAdmin = User.builder().email("super@acm.org").role(Role.SUPER_ADMIN).build();
        User highBoard = User.builder().email("hb@acm.org").role(Role.ACM_HIGH_BOARD).build();
        User commBoard = User.builder().email("cb@acm.org").role(Role.ACM_COMMITTEE_BOARD).build();
        User clubBoard = User.builder().email("club@acm.org").role(Role.ACM_CLUB_BOARD).build();
        User user = User.builder().email("user@acm.org").role(Role.USER).build();
        User noEmailUser = User.builder().email(null).role(Role.USER).build();

        when(memberTargetingService.findMembers(any(), any(), any())).thenReturn(List.of(
                new MemberTargetingService.TargetedMember(superAdmin, null, null, null, null),
                new MemberTargetingService.TargetedMember(highBoard, null, null, null, null),
                new MemberTargetingService.TargetedMember(commBoard, null, null, null, null),
                new MemberTargetingService.TargetedMember(clubBoard, null, null, null, null),
                new MemberTargetingService.TargetedMember(user, null, null, null, null),
                new MemberTargetingService.TargetedMember(noEmailUser, null, null, null, null)
        ));

        CustomEmailPreviewRequestDto request = new CustomEmailPreviewRequestDto();
        request.setSubject("Subject");
        request.setBody("Body");
        request.setRoles(List.of(Role.SUPER_ADMIN, Role.ACM_HIGH_BOARD, Role.ACM_COMMITTEE_BOARD, Role.ACM_CLUB_BOARD, Role.USER));
        request.setSelectedUserEmails(null);

        CustomEmailPreviewDto preview = adminEmailService.previewCustomEmail(request);

        assertEquals(5, preview.getTotalRecipients());
        assertEquals(1L, preview.getRecipientCounts().get("Super Admin"));
        assertEquals(1L, preview.getRecipientCounts().get("High Board"));
        assertEquals(1L, preview.getRecipientCounts().get("Committee Board"));
        assertEquals(1L, preview.getRecipientCounts().get("Club Board"));
        assertEquals(1L, preview.getRecipientCounts().get("Standard User"));
        assertNull(preview.getRecipientCounts().get("Selected recipients"));
    }

    @Test
    void sendCustomEmailSendsOnlyToSelectedUserEmailsWhenRolesEmpty() throws Exception {
        stubCustomEmailTemplate();
        when(mailSender.createMimeMessage())
                .thenAnswer(ignored -> new MimeMessage(Session.getInstance(new Properties())));

        CustomEmailRequestDto request = new CustomEmailRequestDto();
        request.setSubject("Direct email");
        request.setBody("<p>Direct</p>");
        request.setRoles(List.of());
        request.setSelectedUserEmails(List.of("  ", "manual1@acm.org", "MANUAL1@ACM.ORG", "manual2@acm.org"));

        CustomEmailResultDto result = adminEmailService.sendCustomEmail(request);

        assertEquals(2, result.getTotalRecipients());
        verify(memberTargetingService, never()).findRecipientEmails(any(), any(), any());
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendCustomEmailWithNullSelectedEmailsSendsToFilteredRecipients() throws Exception {
        stubCustomEmailTemplate();
        when(mailSender.createMimeMessage())
                .thenAnswer(ignored -> new MimeMessage(Session.getInstance(new Properties())));
        when(memberTargetingService.findRecipientEmails(any(), any(), any()))
                .thenReturn(List.of("user@acm.org"));

        CustomEmailRequestDto request = request();
        request.setSelectedUserEmails(null);

        CustomEmailResultDto result = adminEmailService.sendCustomEmail(request);

        assertEquals(1, result.getTotalRecipients());
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendCustomEmailThrowsIllegalStateExceptionWhenMessagingExceptionOccurs() throws Exception {
        stubCustomEmailTemplate();
        MimeMessage mockMessage = org.mockito.Mockito.mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMessage);
        org.mockito.Mockito.doThrow(new jakarta.mail.MessagingException("Invalid recipient"))
                .when(mockMessage).setFrom(any(jakarta.mail.Address.class));

        when(memberTargetingService.findRecipientEmails(any(), any(), any()))
                .thenReturn(List.of("user@acm.org"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> adminEmailService.sendCustomEmail(request()));
        assertEquals("Unable to compose the email message.", ex.getMessage());
    }

    private CustomEmailRequestDto request() {
        CustomEmailRequestDto request = new CustomEmailRequestDto();
        request.setRecipientFilter("FILTERED_USERS");
        request.setSubject("Chapter update");
        request.setBody("<p>Hello members</p>");
        request.setRoles(List.of(Role.ACM_MEMBER));
        return request;
    }

    private void stubCustomEmailTemplate() {
        when(templateEngine.process(eq("mail/custom-email"), any(Context.class)))
                .thenReturn("<html>ACM custom email</html>");
    }
}