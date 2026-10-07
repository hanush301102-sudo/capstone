package com.creatorhire.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emails;
    private MimeMessage message;

    @BeforeEach
    void setUp() {
        emails = new EmailService(mailSender, "test@example.com");
        message = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(message);
    }

    private static String findPlain(Object content) throws Exception {
        if (content instanceof MimeMultipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                jakarta.mail.BodyPart part = multipart.getBodyPart(i);
                if (part.isMimeType("text/plain") && part.getContent() instanceof String text) {
                    return text;
                }
                String nested = findPlain(part.getContent());
                if (nested != null) {
                    return nested;
                }
            }
            return null;
        }
        return null;
    }
    private String plainBody() throws Exception {
        String found = findPlain(message.getContent());
        if (found == null) {
            throw new IllegalStateException("No text/plain part found");
        }
        return found;
    }

    @Test
    void clientRoleUsesClientTemplate() throws Exception {
        emails.sendOtpEmail("client@example.com", "123456", 10, "CLIENT");
        verify(mailSender).send(message);
        assertTrue(plainBody().contains("start hiring"));
    }

    @Test
    void creatorRoleUsesCreatorTemplate() throws Exception {
        emails.sendOtpEmail("creator@example.com", "123456", 10, "CREATOR");
        verify(mailSender).send(message);
        assertTrue(plainBody().contains("get hired"));
    }

    @Test
    void unknownRoleFallsBackToGenericTemplate() throws Exception {
        emails.sendOtpEmail("other@example.com", "123456", 10, null);
        verify(mailSender).send(message);
        assertTrue(plainBody().contains("finish signing in"));
    }
}
