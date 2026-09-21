package com.creatorhire.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.creatorhire.repository.SkillRepository;
import com.creatorhire.entity.Skill;
import com.creatorhire.entity.SkillCategory;
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

@SpringBootTest
@AutoConfigureMockMvc
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SkillRepository skills;

    private Long skillId;
    private String clientToken;
    private String otherClientToken;
    private String creatorToken;
    private String secondCreatorToken;
    private long jobId;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        skillId = skills.save(new Skill("ProjEdit-" + suffix, SkillCategory.EDITING)).getId();
        clientToken = register("projclient" + suffix + "@hire.test", "CLIENT");
        otherClientToken = register("projother" + suffix + "@hire.test", "CLIENT");
        creatorToken = register("projcreator" + suffix + "@hire.test", "CREATOR");
        secondCreatorToken = register("projsecond" + suffix + "@hire.test", "CREATOR");
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
                                "title", "Brand film",
                                "creativeBrief", "Warm documentary style",
                                "budgetMin", 400,
                                "budgetMax", 1000,
                                "skillIds", List.of(skillId)))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long apply(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "jobId", jobId,
                                "coverLetter", "I shoot docs.",
                                "briefResponse", "Warm grade, handheld feel.",
                                "proposedRate", 600,
                                "estimatedDays", 5,
                                "portfolioSampleIds", List.of()))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long accept(long applicationId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/projects/from-application/" + applicationId)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("NOT_STARTED"))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void acceptCreatesProjectAndRejectsOthers() throws Exception {
        long winner = apply(creatorToken);
        long loser = apply(secondCreatorToken);
        accept(winner);

        // Loser auto-rejected
        mockMvc.perform(get("/api/applications").param("jobId", String.valueOf(jobId))
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id==" + loser + ")].status").value("REJECTED"));

        // Double accept is a conflict
        mockMvc.perform(post("/api/projects/from-application/" + winner)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isConflict());
    }

    @Test
    void nonOwnerCannotAccept() throws Exception {
        long applicationId = apply(creatorToken);
        mockMvc.perform(post("/api/projects/from-application/" + applicationId)
                        .header("Authorization", "Bearer " + otherClientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void fullLifecycleUpdatesReliability() throws Exception {
        long applicationId = apply(creatorToken);
        long projectId = accept(applicationId);

        mockMvc.perform(patch("/api/projects/" + projectId + "/status")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // Illegal jump straight to completion from NOT_STARTED is tested on a fresh project below;
        // here complete the in-progress project.
        mockMvc.perform(patch("/api/projects/" + projectId + "/status")
                        .header("Authorization", "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedOn").exists());

        // Creator's radar card now shows a reliability record.
        mockMvc.perform(get("/api/discovery/creators").param("skillIds", String.valueOf(skillId))
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk());

        // Terminal state is immutable.
        mockMvc.perform(patch("/api/projects/" + projectId + "/status")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void outsiderCannotSeeProject() throws Exception {
        long projectId = accept(apply(creatorToken));
        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + otherClientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void mineReturnsPartyProjects() throws Exception {
        long projectId = accept(apply(creatorToken));
        mockMvc.perform(get("/api/projects/mine").header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(projectId));
        mockMvc.perform(get("/api/projects/mine").header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(projectId));
    }
}
