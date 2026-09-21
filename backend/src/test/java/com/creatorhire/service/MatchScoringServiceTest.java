package com.creatorhire.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.creatorhire.entity.Application;
import com.creatorhire.entity.Availability;
import com.creatorhire.entity.ClientProfile;
import com.creatorhire.entity.CreatorProfile;
import com.creatorhire.entity.CreatorSkill;
import com.creatorhire.entity.Job;
import com.creatorhire.entity.JobSkill;
import com.creatorhire.entity.Role;
import com.creatorhire.entity.Skill;
import com.creatorhire.entity.SkillCategory;
import com.creatorhire.entity.SkillLevel;
import com.creatorhire.entity.User;
import com.creatorhire.repository.CreatorSkillRepository;
import com.creatorhire.repository.JobSkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(MatchScoringService.class)
class MatchScoringServiceTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private JobSkillRepository jobSkills;

    @Autowired
    private CreatorSkillRepository creatorSkills;

    @Autowired
    private MatchScoringService scoring;

    private Skill editing;
    private Skill design;
    private Job job;
    private CreatorProfile creator;

    @BeforeEach
    void setUp() {
        editing = em.persist(new Skill("ScoreEdit-" + System.nanoTime(), SkillCategory.EDITING));
        design = em.persist(new Skill("ScoreDesign-" + System.nanoTime(), SkillCategory.DESIGN));

        User clientUser = new User();
        clientUser.setEmail("scoreclient" + System.nanoTime() + "@hire.test");
        clientUser.setPassword("hashed");
        clientUser.getRoles().add(em.persist(new Role("CLIENT")));
        em.persist(clientUser);
        ClientProfile client = new ClientProfile();
        client.setUser(clientUser);
        em.persist(client);

        job = new Job();
        job.setClientProfile(client);
        job.setTitle("Scored job");
        job.setCreativeBrief("Brief");
        job.setBudgetMin(new BigDecimal("200"));
        job.setBudgetMax(new BigDecimal("800"));
        job.setDeadline(LocalDate.now().plusDays(30));
        em.persist(job);
        link(job, editing);
        link(job, design);

        User creatorUser = new User();
        creatorUser.setEmail("scorecreator" + System.nanoTime() + "@hire.test");
        creatorUser.setPassword("hashed");
        creatorUser.getRoles().add(em.persist(new Role("CREATOR")));
        em.persist(creatorUser);
        creator = new CreatorProfile();
        creator.setUser(creatorUser);
        creator.setAvailability(Availability.AVAILABLE);
        em.persist(creator);
    }

    private void link(Job job, Skill skill) {
        JobSkill jobSkill = new JobSkill();
        jobSkill.setJob(job);
        jobSkill.setSkill(skill);
        em.persist(jobSkill);
    }

    private void grant(CreatorProfile creator, Skill skill) {
        CreatorSkill creatorSkill = new CreatorSkill();
        creatorSkill.setCreatorProfile(creator);
        creatorSkill.setSkill(skill);
        creatorSkill.setLevel(SkillLevel.EXPERT);
        em.persist(creatorSkill);
    }

    private Application application(BigDecimal rate, Integer days) {
        Application application = new Application();
        application.setJob(job);
        application.setCreatorProfile(creator);
        application.setProposedRate(rate);
        application.setEstimatedDays(days);
        application.setResponseTimeHours(1);
        return application;
    }

    @Test
    void perfectFitScoresMaximum() {
        grant(creator, editing);
        grant(creator, design);
        creator.setOnTimeDeliveryRate(new BigDecimal("100"));
        creator.setCompletedProjects(3);
        BigDecimal score = scoring.score(application(new BigDecimal("500"), 5));
        assertThat(score).isEqualByComparingTo("100.00");
    }

    @Test
    void partialSkillsHalveSkillPoints() {
        grant(creator, editing);
        creator.setOnTimeDeliveryRate(new BigDecimal("100"));
        creator.setCompletedProjects(3);
        // 20 (skills) + 20 (budget) + 15 (avail) + 10 (deadline) + 15 (reliability) = 80
        assertThat(scoring.score(application(new BigDecimal("500"), 5))).isEqualByComparingTo("80.00");
    }

    @Test
    void overBudgetReducesBudgetPoints() {
        grant(creator, editing);
        grant(creator, design);
        creator.setOnTimeDeliveryRate(new BigDecimal("100"));
        creator.setCompletedProjects(3);
        BigDecimal over = scoring.score(application(new BigDecimal("900"), 5));
        BigDecimal inside = scoring.score(application(new BigDecimal("500"), 5));
        assertThat(over).isLessThan(inside);
        assertThat(over).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    @Test
    void unavailableCreatorLosesAvailabilityPoints() {
        grant(creator, editing);
        grant(creator, design);
        creator.setAvailability(Availability.UNAVAILABLE);
        creator.setOnTimeDeliveryRate(new BigDecimal("100"));
        creator.setCompletedProjects(3);
        // 40 + 20 + 0 + 10 + 15 = 85
        assertThat(scoring.score(application(new BigDecimal("500"), 5))).isEqualByComparingTo("85.00");
    }

    @Test
    void missedDeadlineEstimateLosesDeadlinePoints() {
        grant(creator, editing);
        grant(creator, design);
        creator.setOnTimeDeliveryRate(new BigDecimal("100"));
        creator.setCompletedProjects(3);
        // 40 + 20 + 15 + 0 + 15 = 90
        assertThat(scoring.score(application(new BigDecimal("500"), 90))).isEqualByComparingTo("90.00");
    }

    @Test
    void newCreatorGetsNeutralReliability() {
        grant(creator, editing);
        grant(creator, design);
        // 40 + 20 + 15 + 10 + 7.5 = 92.50
        assertThat(scoring.score(application(new BigDecimal("500"), 5))).isEqualByComparingTo("92.50");
    }
}
