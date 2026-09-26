package com.creatorhire.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest extends OtpTestSupport {

    private String register(String email, String role) throws Exception {
        return registerAndVerify(email, role);
    }

    @Test
    void registerClientVerifyAndLogin() throws Exception {
        String email = "authclient@hire.test";
        String code = registerUnverified(email, "CLIENT");

        // Login is blocked while unverified
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", email, "password", "password123"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Email not verified. Please verify the code sent to your email"));

        // Verify OTP returns a usable token
        MvcResult result = mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", email, "code", code))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.roles[0]").value("CLIENT"))
                .andReturn();
        String token = objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();

        // Login works after verification
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", email, "password", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        // Authenticated /me works with the token
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void registerReturnsVerificationPromptWithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "pending@hire.test",
                                "password", "password123",
                                "firstName", "Test",
                                "lastName", "User",
                                "role", "CREATOR"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void duplicateEmailYieldsConflict() throws Exception {
        String email = "dupe@hire.test";
        register(email, "CREATOR");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", "password123",
                                "firstName", "Test",
                                "lastName", "User",
                                "role", "CREATOR"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void adminSelfRegistrationIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "sneaky@hire.test",
                                "password", "password123",
                                "firstName", "Test",
                                "lastName", "User",
                                "role", "ADMIN"))))
                .andExpect(status().isConflict());
    }

    @Test
    void validationFailuresYieldBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "not-an-email",
                                "password", "short",
                                "firstName", "",
                                "lastName", "",
                                "role", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void wrongPasswordYieldsUnauthorized() throws Exception {
        String email = "badpass@hire.test";
        register(email, "CLIENT");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", email, "password", "wrong-password"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void protectedEndpointWithoutTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/auth/me")).andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpointWithTamperedTokenIsRejected() throws Exception {
        String token = register("tamper@hire.test", "CLIENT") + "tampered";
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void creatorCannotAccessAdminArea() throws Exception {
        String token = register("norole@hire.test", "CREATOR");
        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
