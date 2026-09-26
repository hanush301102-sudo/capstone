package com.creatorhire.config;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Explicit JavaMailSender wired purely from environment variables
 * (MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD). No credentials
 * live in files; unset vars fall back to a local dummy that tests replace
 * with a mock EmailService.
 */
@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender(
            @Value("${MAIL_HOST:localhost}") String host,
            @Value("${MAIL_PORT:25}") int port,
            @Value("${MAIL_USERNAME:}") String username,
            @Value("${MAIL_PASSWORD:}") String password) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);
        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(!username.isBlank()));
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        props.put("mail.debug", "false");
        return sender;
    }
}
