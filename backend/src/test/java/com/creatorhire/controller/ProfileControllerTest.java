package com.creatorhire.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class ProfileControllerTest extends OtpTestSupport {
    @Autowired
    private SkillRepository skills;

    private Long skillId;
    private String creatorToken;
    private String clientToken;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        skillId = skills.findByName("Premiere Pro").orElseThrow().getId();
        creatorToken = register("profcreator" + suffix + "@hire.test", "CREATOR");
        clientToken = register("profclient" + suffix + "@hire.test", "CLIENT");
    }

    private String register(String email, String role) throws Exception {
        return registerAndVerify(email, role);
    }

    @Test
    void creatorUpdatesProfileWithSkills() throws Exception {
        mockMvc.perform(put("/api/profiles/creator/me")
                        .header("Authorization", "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "headline", "Teaser specialist",
                                "bio", "Cutting launch films for 5 years.",
                                "experienceYears", 5,
                                "availability", "AVAILABLE",
                                "hourlyRate", 60,
                                "skills", List.of(Map.of("skillId", skillId, "level", "EXPERT"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headline").value("Teaser specialist"))
                .andExpect(jsonPath("$.availability").value("AVAILABLE"))
                .andExpect(jsonPath("$.skills[0].skillName").value("Premiere Pro"))
                .andExpect(jsonPath("$.skills[0].level").value("EXPERT"));

        mockMvc.perform(get("/api/profiles/creator/me")
                        .header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedProjects").value(0));
    }

    @Test
    void clientCannotTouchCreatorProfile() throws Exception {
        mockMvc.perform(get("/api/profiles/creator/me")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/profiles/creator/me")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("headline", "Hijack"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void clientUpdatesOwnProfile() throws Exception {
        mockMvc.perform(put("/api/profiles/client/me")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "companyName", "Studio North",
                                "bio", "DTC brand.",
                                "industry", "E-commerce"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Studio North"));
    }

    @Test
    void unknownSkillIsRejected() throws Exception {
        mockMvc.perform(put("/api/profiles/creator/me")
                        .header("Authorization", "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "skills", List.of(Map.of("skillId", 999999, "level", "EXPERT"))))))
                .andExpect(status().isNotFound());
    }
}
