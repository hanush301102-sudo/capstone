package com.creatorhire.service;

import jakarta.mail.internet.MimeMessage;
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

    private final JavaMailSender mailSender;
    private final String from;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${MAIL_FROM:creatorhire@example.com}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendOtpEmail(String to, String code, long expiryMinutes) {
        String subject = "Your CreatorHire verification code";
        String plain = "Your CreatorHire verification code is " + code
                + ". It expires in " + expiryMinutes + " minutes. If you did not request this, ignore this email.";
        String html = "<p>Your CreatorHire verification code is:</p>"
                + "<h2 style=\"letter-spacing:4px\">" + code + "</h2>"
                + "<p>It expires in " + expiryMinutes + " minutes. If you did not request this, ignore this email.</p>";
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

    private static String mask(String email) {
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
