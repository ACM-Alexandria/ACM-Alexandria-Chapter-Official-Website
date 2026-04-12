package com.acm.acmwebsite.EmailServer.service;

import com.acm.acmwebsite.EmailServer.dto.EmailRequest;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
public class EmailComposer {

	private final TemplateEngine templateEngine;

	// Injecting Spring's TemplateEngine via constructor
	public EmailComposer(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	/**
	 * Extracts variables from the EmailRequest, loads them into a Thymeleaf context,
	 * and processes the HTML template.
	 *
	 * @param request The validated EmailRequest DTO
	 * @return The fully rendered HTML as a standard String
	 */
	public String composeHtml(EmailRequest request) {
		// 1. Create a Thymeleaf Context object
		Context context = new Context();

		// 2. Extract and load variables if they exist
		Map<String, Object> variables = request.getVariables();
		if (variables != null && !variables.isEmpty()) {
			context.setVariables(variables);
		}

		// 3. Process the template by name and return the rendered HTML
		return templateEngine.process(request.getTemplateName(), context);
	}
}