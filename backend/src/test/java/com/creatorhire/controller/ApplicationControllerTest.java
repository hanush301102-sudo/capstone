package com.creatorhire.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.creatorhire.entity.CreatorProfile;
import com.creatorhire.entity.Job;
import com.creatorhire.entity.Portfolio;
import com.creatorhire.entity.Skill;
import com.creatorhire.entity.SkillCategory;
import com.creatorhire.repository.CreatorProfileRepository;
import com.creatorhire.repository.JobRepository;
import com.creatorhire.repository.PortfolioRepository;
import com.creatorhire.repository.SkillRepository;
import com.creatorhire.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SkillRepository skills;

    @Autowired
    private JobRepository jobs;

    @Autowired
    private UserRepository users;

    @Autowired
    private CreatorProfileRepository creatorProfiles;

    @Autowired
    private PortfolioRepository portfolios;

    private Long skillId;
    private String clientToken;
    private String otherClientToken;
    private String creatorToken;
    private String creatorEmail;
    private long jobId;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        skillId = skills.save(new Skill("ApplyEdit-" + suffix, SkillCategory.EDITING)).getId();
        clientToken = register("appclient" + suffix + "@hire.test", "CLIENT");
        otherClientToken = register("appother" + suffix + "@hire.test", "CLIENT");
        creatorEmail = "appcreator" + suffix + "@hire.test";
        creatorToken = register(creatorEmail, "CREATOR");
        jobId = createJob(clientToken);
    }

    private String register(String email, String role) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", "password123",
                                "firstName", "Test",
                                "lastName", "User",
                                "role", role))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private long createJob(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Music video edit",
                                "creativeBrief", "Moody neon cut, 60 seconds",
                                "budgetMin", 300,
                                "budgetMax", 800,
                                "skillIds", List.of(skillId)))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private Map<String, Object> applyBody(Long targetJobId, List<Long> sampleIds) {
        return Map.of(
                "jobId", targetJobId,
                "coverLetter", "I cut music videos.",
                "briefResponse", "Neon grade, beat-matched cuts.",
                "proposedRate", 500,
                "estimatedDays", 4,
                "portfolioSampleIds", sampleIds == null ? List.of() : sampleIds);
    }

    @Transactional
    long ownPortfolioId() {
        Long userId = users.findByEmail(creatorEmail).orElseThrow().getId();
        CreatorProfile creator = creatorProfiles.findByUserId(userId).orElseThrow();
        Portfolio portfolio = new Portfolio();
        portfolio.setCreatorProfile(creator);
        portfolio.setTitle("Neon cut");
        return portfolios.save(portfolio).getId();
    }

    @Test
    void applySucceedsWithMatchScore() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applyBody(jobId, List.of(ownPortfolioId())))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.matchScore").isNumber())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.samplePortfolioIds[0]").exists());
    }

    @Test
    void duplicateApplyYieldsConflict() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applyBody(jobId, null))))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applyBody(jobId, null))))
                .andExpect(status().isConflict());
    }

    @Test
    void foreignSampleIsRejected() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applyBody(jobId, List.of(999999L)))))
                .andExpect(status().isNotFound());
    }

    @Test
    void clientSeesRankedApplications() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        String secondCreator = register("apprank" + suffix + "@hire.test", "CREATOR");
        mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applyBody(jobId, null))))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + secondCreator)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applyBody(jobId, null))))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/applications").param("jobId", String.valueOf(jobId))
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void otherClientCannotViewApplications() throws Exception {
        mockMvc.perform(get("/api/applications").param("jobId", String.valueOf(jobId))
                        .header("Authorization", "Bearer " + otherClientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void clientCanShortlistButNotAcceptYet() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applyBody(jobId, null))))
                .andExpect(status().isCreated())
                .andReturn();
        long appId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/api/applications/" + appId + "/status")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SHORTLISTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHORTLISTED"));

        mockMvc.perform(patch("/api/applications/" + appId + "/status")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACCEPTED\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void closedJobRejectsApplications() throws Exception {
        long closedJobId = createJob(clientToken);
        Job job = jobs.findById(closedJobId).orElseThrow();
        job.setStatus(com.creatorhire.entity.JobStatus.CLOSED);
        jobs.save(job);
        mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applyBody(closedJobId, null))))
                .andExpect(status().isConflict());
    }
}
