package com.creatorhire.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.creatorhire.entity.Role;
import com.creatorhire.entity.User;
import com.creatorhire.repository.RoleRepository;
import com.creatorhire.repository.SkillRepository;
import com.creatorhire.repository.UserRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
class PlatformFlowTest extends OtpTestSupport {
    @Autowired
    private SkillRepository skills;

    @Autowired
    private UserRepository users;

    @Autowired
    private RoleRepository roles;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long skillId;
    private String clientToken;
    private String creatorToken;
    private String creatorEmail;
    private String adminToken;
    private long jobId;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        skillId = skills.findByName("Premiere Pro").orElseThrow().getId();
        clientToken = register("platclient" + suffix + "@hire.test", "CLIENT");
        creatorEmail = "platcreator" + suffix + "@hire.test";
        creatorToken = register(creatorEmail, "CREATOR");
        adminToken = createAdmin("platadmin" + suffix + "@hire.test");
        jobId = createJob(clientToken);
    }

    @Transactional
    String createAdmin(String email) {
        Role admin = roles.findByName("ADMIN").orElseThrow();
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setFirstName("Admin");
        user.setLastName("User");
        user.setEmailVerified(true);
        user.getRoles().add(admin);
        users.save(user);
        return login(email);
    }

    private String login(String email) {
        try {
            MvcResult result = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("email", email, "password", "password123"))))
                    .andReturn();
            if (result.getResponse().getStatus() != 200) {
                return null;
            }
            return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String register(String email, String role) throws Exception {
        return registerAndVerify(email, role);
    }

    private long createJob(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Product teaser",
                                "creativeBrief", "Bold kinetic type",
                                "budgetMin", 250,
                                "budgetMax", 700,
                                "skillIds", List.of(skillId)))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long createPortfolio(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/portfolios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Kinetic type reel",
                                "workType", "VIDEO",
                                "collaborationRole", "Lead Editor",
                                "outcomeStats", "1M views",
                                "skillIds", List.of(skillId)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.verificationStatus").value("UNVERIFIED"))
                .andExpect(jsonPath("$.skills[0]").value("Premiere Pro"))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void portfolioOwnershipAndVerificationFlow() throws Exception {
        long portfolioId = createPortfolio(creatorToken);

        // Another creator cannot touch it.
        String suffix = String.valueOf(System.nanoTime());
        String intruder = register("platintruder" + suffix + "@hire.test", "CREATOR");
        mockMvc.perform(put("/api/portfolios/" + portfolioId)
                        .header("Authorization", "Bearer " + intruder)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "Stolen"))))
                .andExpect(status().isForbidden());

        // Owner requests verification; admin approves.
        mockMvc.perform(patch("/api/portfolios/" + portfolioId + "/request-verification")
                        .header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("PENDING"));

        mockMvc.perform(get("/api/admin/portfolios/pending")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(portfolioId));

        mockMvc.perform(patch("/api/admin/portfolios/" + portfolioId + "/verify")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"));
    }

    @Test
    void notificationsFlowOnApplication() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "jobId", jobId,
                                "coverLetter", "I do kinetic type.",
                                "briefResponse", "Bold cuts on beat.",
                                "proposedRate", 500,
                                "estimatedDays", 3,
                                "portfolioSampleIds", List.of()))))
                .andExpect(status().isCreated());

        MvcResult unread = mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andReturn();
        long count = objectMapper.readTree(unread.getResponse().getContentAsString()).asLong();
        assert count >= 1;

        MvcResult list = mockMvc.perform(get("/api/notifications/mine")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andReturn();
        long notificationId = objectMapper.readTree(list.getResponse().getContentAsString()).get(0).get("id").asLong();

        mockMvc.perform(patch("/api/notifications/" + notificationId + "/read")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void reportAndAdminReviewFlow() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/reports")
                        .header("Authorization", "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "jobId", jobId,
                                "reason", "Suspected plagiarism",
                                "description", "Brief copies another post"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();
        long reportId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Empty report is rejected.
        mockMvc.perform(post("/api/reports")
                        .header("Authorization", "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("reason", "x"))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/admin/reports").param("status", "PENDING")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reportId));

        mockMvc.perform(patch("/api/admin/reports/" + reportId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DISMISSED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISMISSED"));
    }

    @Test
    void adminSuspendBlocksLoginAndNonAdminIsRejected() throws Exception {
        // Non-admin cannot list users.
        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isForbidden());

        MvcResult list = mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        long creatorId = 0;
        var root = objectMapper.readTree(list.getResponse().getContentAsString());
        for (var node : root) {
            if (creatorEmail.equals(node.get("email").asText())) {
                creatorId = node.get("id").asLong();
            }
        }
        assert creatorId != 0;

        mockMvc.perform(patch("/api/admin/users/" + creatorId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SUSPENDED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));

        // Suspended creator can no longer log in.
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", creatorEmail, "password", "password123"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void skillCatalogIsSeeded() throws Exception {
        mockMvc.perform(get("/api/skills").header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name=='Premiere Pro')]").exists());
    }
}
