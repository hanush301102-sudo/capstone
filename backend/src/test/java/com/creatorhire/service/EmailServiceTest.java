package com.creatorhire.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private EmailService emails;

    @BeforeEach
    void setUp() {
        emails = new EmailService(restTemplate, "test-api-key", "https://api.brevo.com/v3/smtp/email", "test@example.com");
    }

    private String capturePayload() {
        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(eq("https://api.brevo.com/v3/smtp/email"), captor.capture(), eq(String.class));
        return (String) captor.getValue().getBody();
    }

    @Test
    void clientRoleUsesClientTemplate() {
        emails.sendOtpEmail("client@example.com", "123456", 10, "CLIENT");
        String payload = capturePayload();
        assertTrue(payload.contains("start hiring"));
    }

    @Test
    void creatorRoleUsesCreatorTemplate() {
        emails.sendOtpEmail("creator@example.com", "123456", 10, "CREATOR");
        String payload = capturePayload();
        assertTrue(payload.contains("get hired"));
    }

    @Test
    void unknownRoleFallsBackToGenericTemplate() {
        emails.sendOtpEmail("other@example.com", "123456", 10, null);
        String payload = capturePayload();
        assertTrue(payload.contains("finish signing in"));
    }
}
