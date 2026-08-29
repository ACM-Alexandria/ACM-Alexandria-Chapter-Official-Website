# Gmail SMTP Setup (Spring Boot)

This document describes how to configure Gmail SMTP for local development using Spring Boot's `spring.mail.*` properties.

## Prerequisites

- A Google account.
- **2-Step Verification** enabled on the account.

## Why an App Password

Google requires App Passwords for third-party apps when 2-Step Verification is enabled. Use an App Password instead of your regular Google password for SMTP access.

## Steps

1. Visit your Google Account: <https://myaccount.google.com/>
2. Open the **Security** section.
3. Under "Signing in to Google" enable **2-Step Verification** if it is not already enabled.
4. Once 2-Step Verification is enabled, go to **App passwords**.
5. Create a new App password:
   - For **Select app** choose **Mail**, or choose **Other (Custom name)** and enter a descriptive name such as `acm-website-local`.
   - Click **Generate** and copy the shown 16-character password. It may be displayed in groups of four; when you paste it into your properties file remove any spaces.

## Exact Spring Boot property mappings

Add the following to your local `application.properties` (or set equivalent environment variables):

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=YOUR_16_CHAR_APP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

## Notes and best practices

- `spring.mail.username` must be your full Gmail address.
- Do NOT use your regular Google account password; always use the generated App Password.
- Keep `spring.mail.password` secret. Provide it via a local `application.properties` (not committed), environment variables, or your OS secret manager.
- For testing only: if you prefer not to use a real account, consider a local SMTP testing tool (e.g., MailHog) during development.
