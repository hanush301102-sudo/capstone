package com.creatorhire.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.creatorhire.entity.EmailOtp;
import com.creatorhire.entity.User;
import com.creatorhire.repository.EmailOtpRepository;
import com.creatorhire.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
class OtpFlowTest extends OtpTestSupport {

    @Autowired
    private EmailOtpRepository otpRepository;

    @Autowired
    private UserRepository userRepository;

    private String wrongCode(String real) {
        return real.equals("000000") ? "000001" : "000000";
    }

    @Test
    void replayOfUsedOtpFails() throws Exception {
        String email = "replay" + System.nanoTime() + "@hire.test";
        String code = registerUnverified(email, "CLIENT");
        verify(email, code, 200);
        // Single-use: replay must fail
        verify(email, code, 409);
    }

    @Test
    void wrongThenCorrectCodeWithinLimit() throws Exception {
        String email = "wrongok" + System.nanoTime() + "@hire.test";
        String code = registerUnverified(email, "CREATOR");
        verify(email, wrongCode(code), 409);
        verify(email, code, 200);
    }

    @Test
    void bruteForceLocksCodeAfterFiveAttempts() throws Exception {
        String email = "brute" + System.nanoTime() + "@hire.test";
        String code = registerUnverified(email, "CLIENT");
        String wrong = wrongCode(code);
        for (int i = 0; i < 5; i++) {
            int expected = 409;
            mockMvc.perform(post("/api/auth/verify-otp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("email", email, "code", wrong))))
                    .andExpect(status().is(expected));
        }
        // Locked: even the correct code no longer works -> 409 or 429
        int status = verifyStatus(email, code);
        assert status == 409 || status == 429 : "expected lockout, got " + status;
    }

    @Test
    void expiredOtpIsRejected() throws Exception {
        String email = "expired" + System.nanoTime() + "@hire.test";
        String code = registerUnverified(email, "CLIENT");
        User user = userRepository.findByEmail(email).orElseThrow();
        EmailOtp otp = otpRepository.findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(user.getId()).orElseThrow();
        otp.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        otpRepository.save(otp);
        verify(email, code, 409);
    }

    @Test
    void resendInvalidatesOldCodeAndIssuesNew() throws Exception {
        String email = "resend" + System.nanoTime() + "@hire.test";
        String oldCode = registerUnverified(email, "CREATOR");
        mockMvc.perform(post("/api/auth/resend-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
        String newCode = lastSentCode();
        // Old code must fail
        verify(email, oldCode, 409);
        // New code works
        verify(email, newCode, 200);
    }

    @Test
    void verifyingAlreadyVerifiedAccountFails() throws Exception {
        String email = "already" + System.nanoTime() + "@hire.test";
        String code = registerUnverified(email, "CLIENT");
        verify(email, code, 200);
        mockMvc.perform(post("/api/auth/resend-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email))))
                .andExpect(status().isConflict());
    }

    @Test
    void malformedCodeYieldsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "x@hire.test", "code", "abc"))))
                .andExpect(status().isBadRequest());
    }

    private void verify(String email, String code, int expected) throws Exception {
        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", email, "code", code))))
                .andExpect(status().is(expected));
    }

    private int verifyStatus(String email, String code) throws Exception {
        return mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", email, "code", code))))
                .andReturn().getResponse().getStatus();
    }
}
