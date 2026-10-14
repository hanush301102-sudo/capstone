package com.creatorhire.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String APP_NAME = "CreatorHire";

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String apiUrl;
    private final String from;

    public EmailService(
            RestTemplate restTemplate,
            @Value("${MAIL_PASSWORD:}") String apiKey,
            @Value("${MAIL_API_URL:https://api.brevo.com/v3/smtp/email}") String apiUrl,
            @Value("${MAIL_FROM:creatorhire@example.com}") String from) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.from = from;
    }

    public void sendOtpEmail(String to, String code, long expiryMinutes) {
        sendOtpEmail(to, code, expiryMinutes, null);
    }

    public void sendOtpEmail(String to, String code, long expiryMinutes, String roleName) {
        String base = "templates/otp-email";
        if (roleName != null && roleName.toUpperCase().contains("CLIENT")) {
            base = "templates/otp-email-client";
        } else if (roleName != null) {
            base = "templates/otp-email-creator";
        }
        String subject = "Your " + APP_NAME + " verification code";
        String plain = render(base + ".txt", code, expiryMinutes);
        String html = render(base + ".html", code, expiryMinutes);
        try {
            String payload = buildPayload(to, subject, plain, html);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);
            headers.set("accept", "application/json");
            restTemplate.postForEntity(apiUrl, new HttpEntity<>(payload, headers), String.class);
            log.info("OTP email sent to {}", mask(to));
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {} - {}", mask(to), e.getClass().getSimpleName(), e.getMessage());
            throw new IllegalStateException("Could not send verification email. Please try again later");
        }
    }

    private String buildPayload(String to, String subject, String plain, String html) {
        return "{\"sender\":{\"email\":\"" + from + "\"},"
                + "\"to\":[{\"email\":\"" + to + "\"}],"
                + "\"subject\":\"" + subject + "\","
                + "\"textContent\":\"" + escapeJson(plain) + "\","
                + "\"htmlContent\":\"" + escapeJson(html) + "\"}";
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

    private static String render(String location, String code, long expiryMinutes) {
        try {
            String template = StreamUtils.copyToString(
                    new ClassPathResource(location).getInputStream(), StandardCharsets.UTF_8);
            return template
                    .replace("{{appName}}", APP_NAME)
                    .replace("{{code}}", code)
                    .replace("{{expiryMinutes}}", String.valueOf(expiryMinutes));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not load email template: " + location, e);
        }
    }

    private static String mask(String email) {
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
