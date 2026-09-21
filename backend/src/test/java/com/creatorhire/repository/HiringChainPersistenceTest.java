package com.creatorhire.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import com.creatorhire.entity.Application;
import com.creatorhire.entity.ApplicationSample;
import com.creatorhire.entity.ApplicationStatus;
import com.creatorhire.entity.Availability;
import com.creatorhire.entity.ClientProfile;
import com.creatorhire.entity.CreatorProfile;
import com.creatorhire.entity.CreatorSkill;
import com.creatorhire.entity.Job;
import com.creatorhire.entity.JobSkill;
import com.creatorhire.entity.JobStatus;
import com.creatorhire.entity.Portfolio;
import com.creatorhire.entity.Project;
import com.creatorhire.entity.ProjectStatus;
import com.creatorhire.entity.Role;
import com.creatorhire.entity.Skill;
import com.creatorhire.entity.SkillCategory;
import com.creatorhire.entity.SkillLevel;
import com.creatorhire.entity.User;
import com.creatorhire.entity.VerificationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class HiringChainPersistenceTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ApplicationRepository applications;

    @Autowired
    private JobRepository jobs;

    @Autowired
    private CreatorProfileRepository creatorProfiles;

    private User persistUser(String email, String roleName) {
        Role role = em.persist(new Role(roleName));
        User user = new User();
        user.setEmail(email);
        user.setPassword("hashed");
        user.setFirstName("Test");
        user.setLastName("User");
        user.getRoles().add(role);
        return em.persist(user);
    }

    @Test
    void fullHiringChainPersistsAcrossAllTables() {
        // Client + job with creative brief
        User clientUser = persistUser("client@hire.test", "CLIENT");
        ClientProfile client = new ClientProfile();
        client.setUser(clientUser);
        client.setCompanyName("Studio One");
        em.persist(client);

        Skill editing = em.persist(new Skill("Premiere Pro", SkillCategory.EDITING));

        Job job = new Job();
        job.setClientProfile(client);
        job.setTitle("Launch teaser edit");
        job.setCreativeBrief("30s cinematic teaser, fast cuts");
        job.setStyleKeywords("cinematic, punchy");
        job.setReferenceLinks("https://example.com/ref1");
        job.setBudgetMin(new BigDecimal("200"));
        job.setBudgetMax(new BigDecimal("500"));
        em.persist(job);

        JobSkill jobSkill = new JobSkill();
        jobSkill.setJob(job);
        jobSkill.setSkill(editing);
        em.persist(jobSkill);

        // Creator + verified tagged portfolio
        User creatorUser = persistUser("creator@hire.test", "CREATOR");
        CreatorProfile creator = new CreatorProfile();
        creator.setUser(creatorUser);
        creator.setHeadline("Teaser specialist");
        creator.setAvailability(Availability.AVAILABLE);
        em.persist(creator);

        CreatorSkill creatorSkill = new CreatorSkill();
        creatorSkill.setCreatorProfile(creator);
        creatorSkill.setSkill(editing);
        creatorSkill.setLevel(SkillLevel.EXPERT);
        em.persist(creatorSkill);

        Portfolio portfolio = new Portfolio();
        portfolio.setCreatorProfile(creator);
        portfolio.setTitle("SaaS launch teaser");
        portfolio.setWorkType("VIDEO");
        portfolio.setCollaborationRole("Lead Editor");
        portfolio.setVerificationStatus(VerificationStatus.VERIFIED);
        em.persist(portfolio);

        // Application with brief response + match score + sample
        Application application = new Application();
        application.setJob(job);
        application.setCreatorProfile(creator);
        application.setCoverLetter("I cut teasers weekly.");
        application.setBriefResponse("Fast-cut structure matching your brief, 3-day turnaround.");
        application.setProposedRate(new BigDecimal("350"));
        application.setEstimatedDays(3);
        application.setMatchScore(new BigDecimal("87.50"));
        application.setResponseTimeHours(5);
        em.persist(application);

        ApplicationSample sample = new ApplicationSample();
        sample.setApplication(application);
        sample.setPortfolio(portfolio);
        em.persist(sample);

        // Hiring creates the project
        Project project = new Project();
        project.setJob(job);
        project.setApplication(application);
        project.setTitle("Launch teaser edit");
        project.setStatus(ProjectStatus.IN_PROGRESS);
        em.persist(project);
        em.flush();
        em.clear();

        // Ranked applications for the client
        List<Application> ranked = applications.findByJobIdOrderByMatchScoreDesc(job.getId());
        assertThat(ranked).hasSize(1);
        assertThat(ranked.get(0).getMatchScore()).isEqualByComparingTo("87.50");
        assertThat(ranked.get(0).getStatus()).isEqualTo(ApplicationStatus.PENDING);

        // Creator Radar finds the creator by skill + availability
        List<CreatorProfile> radar =
                creatorProfiles.findRadarCandidates(List.of(editing.getId()), Availability.AVAILABLE);
        assertThat(radar).extracting(CreatorProfile::getId).contains(creator.getId());

        // Job discovery finds the open job by skill
        List<Job> discovered = jobs.searchOpenJobs(List.of(editing.getId()), "teaser", JobStatus.OPEN);
        assertThat(discovered).extracting(Job::getId).contains(job.getId());

        // Duplicate application for the same job violates the unique constraint
        Application duplicate = new Application();
        duplicate.setJob(em.merge(job));
        duplicate.setCreatorProfile(em.merge(creator));
        assertThatThrownBy(() -> {
            em.persist(duplicate);
            em.flush();
        }).isInstanceOf(Exception.class);
    }
}
