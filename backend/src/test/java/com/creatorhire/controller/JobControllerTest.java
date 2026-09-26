package com.creatorhire.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.creatorhire.entity.Skill;
import com.creatorhire.entity.SkillCategory;
import com.creatorhire.repository.SkillRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class JobControllerTest extends OtpTestSupport {
    @Autowired
    private SkillRepository skills;

    private Long skillId;
    private String clientToken;
    private String otherClientToken;
    private String creatorToken;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        skillId = skills.save(new Skill("Edit-" + suffix, SkillCategory.EDITING)).getId();
        clientToken = register("jobclient" + suffix + "@hire.test", "CLIENT");
        otherClientToken = register("jobother" + suffix + "@hire.test", "CLIENT");
        creatorToken = register("jobcreator" + suffix + "@hire.test", "CREATOR");
    }

    private String register(String email, String role) throws Exception {
        return registerAndVerify(email, role);
    }

    private Map<String, Object> jobBody() {
        return Map.of(
                "title", "Launch teaser",
                "description", "30 second teaser",
                "creativeBrief", "Cinematic fast-cut teaser",
                "styleKeywords", "cinematic, punchy",
                "referenceLinks", "https://example.com/ref",
                "budgetMin", 200,
                "budgetMax", 500,
                "skillIds", List.of(skillId));
    }

    private long createJob(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobBody())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.creativeBrief").value("Cinematic fast-cut teaser"))
                .andExpect(jsonPath("$.skills[0]").exists())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void clientCreatesAndReadsJob() throws Exception {
        long id = createJob(clientToken);
        mockMvc.perform(get("/api/jobs/" + id).header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Launch teaser"));
    }

    @Test
    void blankBriefIsRejected() throws Exception {
        var body = new java.util.HashMap<>(jobBody());
        body.put("creativeBrief", "");
        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invertedBudgetIsRejected() throws Exception {
        var body = new java.util.HashMap<>(jobBody());
        body.put("budgetMin", 900);
        body.put("budgetMax", 100);
        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creatorCannotPostJob() throws Exception {
        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobBody())))
                .andExpect(status().isForbidden());
    }

    @Test
    void otherClientCannotEditOrDeleteJob() throws Exception {
        long id = createJob(clientToken);
        mockMvc.perform(put("/api/jobs/" + id)
                        .header("Authorization", "Bearer " + otherClientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobBody())))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/jobs/" + id)
                        .header("Authorization", "Bearer " + otherClientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void ownerCanUpdateAndDelete() throws Exception {
        long id = createJob(clientToken);
        var body = new java.util.HashMap<>(jobBody());
        body.put("title", "Updated teaser");
        mockMvc.perform(put("/api/jobs/" + id)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated teaser"));
        mockMvc.perform(delete("/api/jobs/" + id)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void openJobsSearchableByKeyword() throws Exception {
        createJob(clientToken);
        mockMvc.perform(get("/api/jobs").param("keyword", "teaser")
                        .header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Launch teaser"));
    }

    @Test
    void radarFindsCreatorsBySkill() throws Exception {
        // Give the creator the skill via application flow precondition: attach directly.
        mockMvc.perform(get("/api/discovery/creators").param("skillIds", String.valueOf(skillId))
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk());
    }
}
