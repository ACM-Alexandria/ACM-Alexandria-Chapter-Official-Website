## Sending an Email

Inject `EmailService` and call `send()`. It returns immediately; delivery happens on a background thread.

```java
@Service
@RequiredArgsConstructor
public class MyFeatureService {

    private final EmailService emailService;

    public void welcomeUser(String userEmail, String userName, String verifyUrl) {
        EmailRequest request = new EmailRequest(
            List.of(userEmail),                         // recipients
            "Welcome to ACM Alexandria!",               // subject
            "mail/welcome-email",                       // template name (no .html)
            Map.of(
                "userName",        userName,
                "verificationUrl", verifyUrl
            )
        );

        emailService.send(request);  // non-blocking
    }
}
```

### `EmailRequest` Fields

| Field          | Type                  | Required | Description                                      |
|----------------|-----------------------|----------|--------------------------------------------------|
| `to`           | `List<String>`        | ✅       | Recipient email addresses (at least one)         |
| `subject`      | `String`              | ✅       | Email subject line                               |
| `templateName` | `String`              | ✅       | Path to Thymeleaf template under `/templates/`   |
| `variables`    | `Map<String, Object>` | ❌       | Variables injected into the template context     |

---

## Creating a Template

Place HTML files under `src/main/resources/templates/mail/`.  
Use standard Thymeleaf expressions to bind variables from `EmailRequest.variables`.

```html
<!-- src/main/resources/templates/mail/my-template.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
  <p>Hi <strong th:text="${userName}">Member</strong>,</p>
  <a th:href="${actionUrl}">Click here</a>
</body>
</html>
```

Pass `"mail/my-template"` as the `templateName` when building the request.

### Using the Base Layout

A shared `base-layout.html` provides the ACM-branded header/footer via a Thymeleaf fragment.  
Extend it like `welcome-email.html` does using `th:replace`:

```html
<div th:replace="~{mail/base-layout :: content(~{::content})}">
  <th:block th:ref="content">
    <!-- your unique content here -->
  </th:block>
</div>
```

---


## Validation & Error Handling

`EmailRequest` fields are validated automatically via Jakarta Bean Validation.  
Invalid requests sent through a controller return a structured `400` response:

```json
{
  "status": 400,
  "error": "Validation Failed",
  "details": {
    "to": "must contain at least one valid email address",
    "subject": "must not be blank"
  }
}
```
