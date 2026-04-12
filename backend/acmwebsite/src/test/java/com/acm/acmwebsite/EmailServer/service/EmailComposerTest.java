package com.acm.acmwebsite.EmailServer.service;

import com.acm.acmwebsite.EmailServer.dto.EmailRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailComposerTest {

	@Mock
	private TemplateEngine templateEngine;

	@InjectMocks
	private EmailComposer emailComposer;

	@Test
	void buildHtmlBody_WithValidVariables_MapsContextAndReturnsHtml() {
		Map<String, Object> variables = Map.of("userName", "Mostafa", "token", "12345");
		EmailRequest request = new EmailRequest(
				List.of("student@alexu.edu.eg"),
				"Welcome to ACM",
				"welcome-email",
				variables
		);

		String expectedHtml = "<html>Welcome Mostafa</html>";

		when(templateEngine.process(eq("welcome-email"), any(Context.class)))
				.thenReturn(expectedHtml);

		String actualHtml = emailComposer.buildHtmlBody(request);
		assertEquals(expectedHtml, actualHtml, "The returned HTML should match the mock output.");

		ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
		verify(templateEngine).process(eq("welcome-email"), contextCaptor.capture());

		Context capturedContext = contextCaptor.getValue();
		assertEquals("Mostafa", capturedContext.getVariable("userName"), "userName variable should map correctly");
		assertEquals("12345", capturedContext.getVariable("token"), "token variable should map correctly");
	}

	@Test
	void buildHtmlBody_WithNullVariables_HandlesGracefully() {
		EmailRequest request = new EmailRequest(
				List.of("student@alexu.edu.eg"),
				"No Vars Subject",
				"static-email",
				null
		);

		when(templateEngine.process(eq("static-email"), any(Context.class)))
				.thenReturn("<html>Static Content</html>");

		String actualHtml = emailComposer.buildHtmlBody(request);

		assertEquals("<html>Static Content</html>", actualHtml);

		ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
		verify(templateEngine).process(eq("static-email"), contextCaptor.capture());

		assertTrue(contextCaptor.getValue().getVariableNames().isEmpty(), "Context variables should be empty when request variables are null");
	}

	@Test
	void buildHtmlBody_WithEmptyVariables_HandlesGracefully() {
		EmailRequest request = new EmailRequest(
				List.of("student@alexu.edu.eg"),
				"Empty Vars Subject",
				"static-email",
				Collections.emptyMap()
		);

		when(templateEngine.process(eq("static-email"), any(Context.class)))
				.thenReturn("<html>Static Content</html>");

		String actualHtml = emailComposer.buildHtmlBody(request);

		assertEquals("<html>Static Content</html>", actualHtml);

		ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
		verify(templateEngine).process(eq("static-email"), contextCaptor.capture());

		assertTrue(contextCaptor.getValue().getVariableNames().isEmpty(), "Context variables should be empty when request variables are empty");
	}
}