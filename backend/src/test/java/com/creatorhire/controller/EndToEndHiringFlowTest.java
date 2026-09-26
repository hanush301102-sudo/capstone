package com.creatorhire.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.creatorhire.entity.CreatorProfile;
import com.creatorhire.entity.CreatorSkill;
import com.creatorhire.entity.Skill;
import com.creatorhire.entity.SkillLevel;
import com.creatorhire.repository.CreatorProfileRepository;
import com.creatorhire.repository.CreatorSkillRepository;
import com.creatorhire.repository.SkillRepository;
import com.creatorhire.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

/**
 * TASK-017: complete hiring workflow end-to-end —
 * register → post job with brief → discover → apply with samples →
 * ranked review → shortlist → hire → track → complete → reliability.
 */
@SpringBootTest
@AutoConfigureMockMvc
class EndToEndHiringFlowTest extends OtpTestSupport {
    @Autowired
    private SkillRepository skills;

    @Autowired
    private UserRepository users;

    @Autowired
    private CreatorProfileRepository creatorProfiles;

    @Autowired
    private CreatorSkillRepository creatorSkills;

    private JsonNode apiPost(String url, String token, Object body, int expected) throws Exception {
        var builder = post(url).contentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (body != null) {
            builder.content(objectMapper.writeValueAsString(body));
        }
        MvcResult result = mockMvc.perform(builder).andExpect(status().is(expected)).andReturn();
        String content = result.getResponse().getContentAsString();
        return content.isBlank() ? null : objectMapper.readTree(content);
    }

    private String register(String email, String role) throws Exception {
        apiPost("/api/auth/register", null, Map.of(
                        "email", email,
                        "password", "password123",
                        "firstName", "E2E",
                        "lastName", "User",
                        "role", role),
                201);
        String code = lastSentCode();
        return apiPost("/api/auth/verify-otp", null, Map.of("email", email, "code", code), 200)
                .get("token").asText();
    }

    @Test
    void fullHiringWorkflow() throws Exception {
        String suffix = String.valueOf(System.nanoTime());

        // 1. Register client + creator, both authenticate.
        String clientToken = register("e2eclient" + suffix + "@hire.test", "CLIENT");
        String creatorToken = register("e2ecreator" + suffix + "@hire.test", "CREATOR");

        // 2. Client posts a job with a creative brief (< 3 minutes of API calls).
        Long skillId = skills.findByName("Premiere Pro").orElseThrow().getId();
        JsonNode job = apiPost("/api/jobs", clientToken, Map.of(
                        "title", "E2E launch film",
                        "description", "60s launch film",
                        "creativeBrief", "Warm cinematic grade, fast opening",
                        "styleKeywords", "cinematic, warm",
                        "referenceLinks", "https://example.com/ref",
                        "budgetMin", 300,
                        "budgetMax", 900,
                        "skillIds", List.of(skillId)),
                201);
        long jobId = job.get("id").asLong();

        // 3. Creator discovers the job via search.
        mockMvc.perform(get("/api/jobs").param("keyword", "launch")
                        .header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(jobId));

        // 4. Creator showcases work: portfolio with skill tag + verification request.
        JsonNode portfolio = apiPost("/api/portfolios", creatorToken, Map.of(
                        "title", "Launch reel",
                        "workType", "VIDEO",
                        "collaborationRole", "Lead Editor",
                        "skillIds", List.of(skillId)),
                201);
        long portfolioId = portfolio.get("id").asLong();

        // 5. Creator applies with brief response, rate, timeline, and sample.
        JsonNode application = apiPost("/api/applications", creatorToken, Map.of(
                        "jobId", jobId,
                        "coverLetter", "I cut launch films.",
                        "briefResponse", "Warm grade, 3-beat structure per your brief.",
                        "proposedRate", 600,
                        "estimatedDays", 6,
                        "portfolioSampleIds", List.of(portfolioId)),
                201);
        long applicationId = application.get("id").asLong();
        assert application.get("matchScore").asDouble() > 0;
        assert application.get("responseTimeHours").asInt() >= 0;

        // 6. Client reviews ranked applications and shortlists.
        mockMvc.perform(get("/api/applications").param("jobId", String.valueOf(jobId))
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(applicationId))
                .andExpect(jsonPath("$[0].samplePortfolioIds[0]").value(portfolioId));

        mockMvc.perform(patch("/api/applications/" + applicationId + "/status")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SHORTLISTED\"}"))
                .andExpect(status().isOk());

        // 7. Client hires: project created, job can no longer take applications after close.
        JsonNode project = apiPost("/api/projects/from-application/" + applicationId, clientToken, null, 201);
        long projectId = project.get("id").asLong();

        // 8. Both parties track the project.
        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NOT_STARTED"));

        // 9. Work progresses and completes on time.
        mockMvc.perform(patch("/api/projects/" + projectId + "/status")
                        .header("Authorization", "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/projects/" + projectId + "/status")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedOn").exists());

        // 10. Reliability record earned: radar card shows completed project + on-time rate.
        // Attach the skill to the creator (profile skill management ships in TASK-016).
        String creatorEmail = "e2ecreator" + suffix + "@hire.test";
        CreatorProfile creator = creatorProfiles
                .findByUserId(users.findByEmail(creatorEmail).orElseThrow().getId())
                .orElseThrow();
        Skill skill = skills.findById(skillId).orElseThrow();
        CreatorSkill link = new CreatorSkill();
        link.setCreatorProfile(creator);
        link.setSkill(skill);
        link.setLevel(SkillLevel.EXPERT);
        creatorSkills.saveAndFlush(link);
        MvcResult radar = mockMvc.perform(get("/api/discovery/creators")
                        .param("skillIds", String.valueOf(skillId))
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.completedProjects>=1)]").exists())
                .andReturn();
        boolean found = false;
        for (JsonNode card : objectMapper.readTree(radar.getResponse().getContentAsString())) {
            if (card.get("completedProjects").asInt() >= 1) {
                assert card.get("onTimeDeliveryRate").asDouble() == 100.0;
                found = true;
            }
        }
        assert found;

        // 11. Notifications reached both parties.
        mockMvc.perform(get("/api/notifications/mine")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());
        mockMvc.perform(get("/api/notifications/mine")
                        .header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());
    }
}
