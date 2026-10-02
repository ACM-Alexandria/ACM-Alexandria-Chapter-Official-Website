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

@ExtendWith(MockitoExtension.class)
class AdminEmailServiceTest {

    @Mock
    private MemberTargetingService memberTargetingService;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

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
        request.setSelectedUserEmails(List.of("SHARED@acm.org"));

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