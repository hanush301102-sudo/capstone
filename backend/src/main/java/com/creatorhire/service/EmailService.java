package com.creatorhire.service;

import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Sends OTP mail via SMTP (env-driven JavaMailSender). Credentials come only
 * from environment variables; nothing secret is ever logged.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String APP_NAME = "CreatorHire";

    private final JavaMailSender mailSender;
    private final String from;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${MAIL_FROM:creatorhire@example.com}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendOtpEmail(String to, String code, long expiryMinutes) {
        sendOtpEmail(to, code, expiryMinutes, null);
    }

    /**
     * Sends the OTP mail using the template set matching the account role:
     * {@code otp-email-client} for CLIENT accounts, {@code otp-email-creator}
     * otherwise. A null/unknown role falls back to the generic
     * {@code otp-email} templates.
     */
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
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plain, html);
            mailSender.send(message);
            log.info("OTP email sent to {}", mask(to));
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}", mask(to));
            throw new IllegalStateException("Could not send verification email. Please try again later");
        }
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
