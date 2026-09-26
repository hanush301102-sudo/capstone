package com.creatorhire.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import com.creatorhire.entity.EmailOtp;
import com.creatorhire.entity.User;
import com.creatorhire.exception.ConflictException;
import com.creatorhire.exception.RateLimitException;
import com.creatorhire.exception.ResourceNotFoundException;
import com.creatorhire.repository.EmailOtpRepository;
import com.creatorhire.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Email OTP issuance and verification.
 *
 * <ul>
 *   <li>6-digit code from SecureRandom, 10-minute expiry, max 5 attempts.</li>
 *   <li>Only the SHA-256 hash is persisted; plain codes never touch the DB or logs.</li>
 *   <li>Single-use: a verified code is marked used and replay fails.</li>
 *   <li>Resend invalidates previous unused codes and issues a fresh one.</li>
 * </ul>
 */
@Service
public class OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final EmailOtpRepository otps;
    private final UserRepository users;
    private final EmailService emails;
    private final long expiryMinutes;
    private final int maxAttempts;

    public OtpService(
            EmailOtpRepository otps,
            UserRepository users,
            EmailService emails,
            @Value("${app.otp.expiry-minutes:10}") long expiryMinutes,
            @Value("${app.otp.max-attempts:5}") int maxAttempts) {
        this.otps = otps;
        this.users = users;
        this.emails = emails;
        this.expiryMinutes = expiryMinutes;
        this.maxAttempts = maxAttempts;
    }

    @Transactional
    public void issueOtp(User user) {
        for (EmailOtp old : otps.findByUserIdAndUsedFalse(user.getId())) {
            old.setUsed(true);
            otps.save(old);
        }
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        EmailOtp otp = new EmailOtp();
        otp.setUser(user);
        otp.setOtpHash(sha256Hex(code));
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(expiryMinutes));
        otps.save(otp);
        emails.sendOtpEmail(user.getEmail(), code, expiryMinutes);
    }

    // Failed attempts must commit even though verification throws: otherwise
    // the brute-force counter would roll back and the limit never trigger.
    @Transactional(noRollbackFor = {ConflictException.class, RateLimitException.class})
    public User verifyOtp(String email, String code) {
        User user = users.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (user.isEmailVerified()) {
            throw new ConflictException("Email is already verified");
        }
        EmailOtp otp = otps.findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new ConflictException("No active verification code. Please request a new one"));
        if (otp.getAttempts() >= maxAttempts) {
            otp.setUsed(true);
            otps.save(otp);
            throw new RateLimitException("Too many incorrect attempts. Please request a new code");
        }
        if (LocalDateTime.now().isAfter(otp.getExpiresAt())) {
            otp.setUsed(true);
            otps.save(otp);
            throw new ConflictException("Verification code has expired. Please request a new one");
        }
        if (!sha256Hex(code).equals(otp.getOtpHash())) {
            otp.setAttempts(otp.getAttempts() + 1);
            if (otp.getAttempts() >= maxAttempts) {
                otp.setUsed(true);
            }
            otps.save(otp);
            throw new ConflictException("Incorrect verification code");
        }
        otp.setUsed(true);
        otps.save(otp);
        user.setEmailVerified(true);
        return users.save(user);
    }

    @Transactional
    public void resendOtp(String email) {
        User user = users.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (user.isEmailVerified()) {
            throw new ConflictException("Email is already verified");
        }
        issueOtp(user);
    }

    static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
