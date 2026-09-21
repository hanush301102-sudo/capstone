package com.creatorhire.service;

import java.util.List;
import com.creatorhire.dto.JobRequest;
import com.creatorhire.dto.JobResponse;
import com.creatorhire.entity.ClientProfile;
import com.creatorhire.entity.Job;
import com.creatorhire.entity.JobSkill;
import com.creatorhire.entity.JobStatus;
import com.creatorhire.entity.Skill;
import com.creatorhire.entity.User;
import com.creatorhire.exception.ConflictException;
import com.creatorhire.exception.ForbiddenException;
import com.creatorhire.exception.ResourceNotFoundException;
import com.creatorhire.repository.ApplicationRepository;
import com.creatorhire.repository.ClientProfileRepository;
import com.creatorhire.repository.JobRepository;
import com.creatorhire.repository.JobSkillRepository;
import com.creatorhire.repository.SkillRepository;
import com.creatorhire.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobService {

    private final JobRepository jobs;
    private final JobSkillRepository jobSkills;
    private final SkillRepository skills;
    private final ClientProfileRepository clientProfiles;
    private final ApplicationRepository applications;
    private final UserRepository users;

    public JobService(
            JobRepository jobs,
            JobSkillRepository jobSkills,
            SkillRepository skills,
            ClientProfileRepository clientProfiles,
            ApplicationRepository applications,
            UserRepository users) {
        this.jobs = jobs;
        this.jobSkills = jobSkills;
        this.skills = skills;
        this.clientProfiles = clientProfiles;
        this.applications = applications;
        this.users = users;
    }

    @Transactional
    public JobResponse create(JobRequest request) {
        ClientProfile client = currentClient();
        Job job = new Job();
        job.setClientProfile(client);
        apply(job, request);
        jobs.save(job);
        attachSkills(job, request.skillIds());
        return toResponse(job);
    }

    @Transactional
    public JobResponse update(Long id, JobRequest request) {
        Job job = ownedJob(id);
        apply(job, request);
        jobSkills.findByJobId(id).forEach(jobSkills::delete);
        jobSkills.flush();
        attachSkills(job, request.skillIds());
        return toResponse(job);
    }

    @Transactional
    public void delete(Long id) {
        Job job = ownedJob(id);
        if (!applications.findByJobId(id).isEmpty()) {
            throw new ConflictException("Cannot delete a job that has applications");
        }
        jobSkills.findByJobId(id).forEach(jobSkills::delete);
        jobSkills.flush();
        jobs.delete(job);
    }

    @Transactional(readOnly = true)
    public JobResponse get(Long id) {
        Job job = jobs.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
        return toResponse(job);
    }

    @Transactional(readOnly = true)
    public List<JobResponse> myJobs() {
        return jobs.findByClientProfileId(currentClient().getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<JobResponse> openJobs(String keyword, List<Long> skillIds) {
        List<Job> result;
        if (skillIds == null || skillIds.isEmpty()) {
            result = jobs.findByStatus(JobStatus.OPEN);
            if (keyword != null && !keyword.isBlank()) {
                String lower = keyword.toLowerCase();
                result = result.stream()
                        .filter(j -> contains(j.getTitle(), lower)
                                || contains(j.getDescription(), lower)
                                || contains(j.getCreativeBrief(), lower))
                        .toList();
            }
        } else {
            result = jobs.searchOpenJobs(skillIds, keyword, JobStatus.OPEN);
        }
        return result.stream().map(this::toResponse).toList();
    }

    private void apply(Job job, JobRequest request) {
        if (request.budgetMin() != null
                && request.budgetMax() != null
                && request.budgetMin().compareTo(request.budgetMax()) > 0) {
            throw new IllegalArgumentException("budgetMin must not exceed budgetMax");
        }
        job.setTitle(request.title());
        job.setDescription(request.description());
        job.setCreativeBrief(request.creativeBrief());
        job.setStyleKeywords(request.styleKeywords());
        job.setReferenceLinks(request.referenceLinks());
        job.setBudgetMin(request.budgetMin());
        job.setBudgetMax(request.budgetMax());
        job.setDeadline(request.deadline());
    }

    private void attachSkills(Job job, List<Long> skillIds) {
        for (Long skillId : skillIds) {
            Skill skill = skills.findById(skillId)
                    .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + skillId));
            JobSkill link = new JobSkill();
            link.setJob(job);
            link.setSkill(skill);
            jobSkills.save(link);
        }
    }

    private ClientProfile currentClient() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = users.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return clientProfiles.findByUserId(user.getId())
                .orElseThrow(() -> new ForbiddenException("Client profile required"));
    }

    private Job ownedJob(Long id) {
        Job job = jobs.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
        if (!job.getClientProfile().getId().equals(currentClient().getId())) {
            throw new ForbiddenException("You do not own this job");
        }
        return job;
    }

    private JobResponse toResponse(Job job) {
        List<String> skillNames = jobSkills.findByJobId(job.getId()).stream()
                .map(link -> link.getSkill().getName())
                .toList();
        long count = applications.findByJobId(job.getId()).size();
        ClientProfile client = job.getClientProfile();
        return new JobResponse(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getCreativeBrief(),
                job.getStyleKeywords(),
                job.getReferenceLinks(),
                job.getBudgetMin(),
                job.getBudgetMax(),
                job.getDeadline(),
                job.getStatus().name(),
                job.getCreatedAt(),
                client.getId(),
                client.getCompanyName(),
                skillNames,
                count);
    }

    private boolean contains(String haystack, String lower) {
        return haystack != null && haystack.toLowerCase().contains(lower);
    }
}
