package com.acm.acmwebsite.feature.badges;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.AwardView;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;

// What a shared badge link points to. LinkedIn reads link previews from the page's HTML, which the React app
// can't provide, so the backend serves a small page with the preview tags (title, description, a 1200×627
// image) that sends people on to the badge page in the app. Both are public (SecurityConfig: /share/**).
@RestController
public class BadgeShareController {

    // Names and committees can be in any language (Arabic included), so the page says it is UTF-8.
    private static final MediaType HTML_UTF8 = new MediaType(MediaType.TEXT_HTML, java.nio.charset.StandardCharsets.UTF_8);

    private final BadgeAwardService awardService;
    private final BadgeCardRenderer renderer;
    private final String frontendUrl;
    private final String backendUrl;

    public BadgeShareController(BadgeAwardService awardService, BadgeCardRenderer renderer,
                                @Value("${frontend.url}") String frontendUrl,
                                @Value("${backend.url}") String backendUrl) {
        this.awardService = awardService;
        this.renderer = renderer;
        this.frontendUrl = stripSlash(frontendUrl);
        this.backendUrl = stripSlash(backendUrl);
    }

    @GetMapping(value = "/share/badges/{publicId}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> sharePage(@PathVariable UUID publicId) {
        AwardView badge;
        try {
            badge = awardService.publicBadge(publicId);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(HTML_UTF8)
                    .body(page("Badge not found", "This badge isn't available. It may have been removed.",
                            null, null, frontendUrl + "/"));
        }
        String name = badge.member().name();
        String title = name + " · " + badge.name() + ", " + badge.periodLabel();
        String description = name + " was named " + badge.name() + " of the " + badge.groupLabel()
                + " for " + badge.periodLabel() + " at ACM Alexandria Student Chapter.";
        String shareUrl = backendUrl + "/share/badges/" + publicId;
        return ResponseEntity.ok()
                .contentType(HTML_UTF8)
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)))
                .body(page(title, description, shareUrl + "/card.png", shareUrl, frontendUrl + "/badges/" + publicId));
    }

    @GetMapping(value = "/share/badges/{publicId}/card.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> card(@PathVariable UUID publicId) {
        AwardView badge;
        try {
            badge = awardService.publicBadge(publicId);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)))
                .body(renderer.render(badge));
    }

    // `image` and `url` are left out of the not-found page. `target` is where people are sent.
    private static String page(String title, String description, String image, String url, String target) {
        StringBuilder html = new StringBuilder("<!doctype html>\n<html lang=\"en\">\n<head>\n<meta charset=\"utf-8\">\n");
        html.append("<title>").append(escape(title)).append("</title>\n");
        meta(html, "name", "description", description);
        meta(html, "property", "og:type", "website");
        meta(html, "property", "og:site_name", "ACM Alexandria Student Chapter");
        meta(html, "property", "og:title", title);
        meta(html, "property", "og:description", description);
        if (image != null) {
            meta(html, "property", "og:image", image);
            meta(html, "property", "og:image:width", String.valueOf(BadgeCardRenderer.WIDTH));
            meta(html, "property", "og:image:height", String.valueOf(BadgeCardRenderer.HEIGHT));
            meta(html, "name", "twitter:card", "summary_large_image");
        }
        if (url != null) {
            meta(html, "property", "og:url", url);
        }
        html.append("<meta http-equiv=\"refresh\" content=\"0; url=").append(escape(target)).append("\">\n");
        html.append("</head>\n<body>\n<p><a href=\"").append(escape(target)).append("\">")
                .append(escape(title)).append("</a></p>\n</body>\n</html>\n");
        return html.toString();
    }

    private static void meta(StringBuilder html, String attr, String key, String value) {
        html.append("<meta ").append(attr).append("=\"").append(key).append("\" content=\"")
                .append(escape(value)).append("\">\n");
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String stripSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
