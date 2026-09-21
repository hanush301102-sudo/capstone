package com.creatorhire.service;

import java.util.List;
import com.creatorhire.dto.CreatorCardResponse;
import com.creatorhire.entity.Availability;
import com.creatorhire.entity.CreatorProfile;
import com.creatorhire.repository.CreatorProfileRepository;
import com.creatorhire.repository.CreatorSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creator Radar: inverse discovery — clients find creators by skill and
 * availability without posting a job.
 */
@Service
public class CreatorDiscoveryService {

    private final CreatorProfileRepository creators;
    private final CreatorSkillRepository creatorSkills;

    public CreatorDiscoveryService(
            CreatorProfileRepository creators, CreatorSkillRepository creatorSkills) {
        this.creators = creators;
        this.creatorSkills = creatorSkills;
    }

    @Transactional(readOnly = true)
    public List<CreatorCardResponse> discover(List<Long> skillIds, Availability availability) {
        List<CreatorProfile> candidates;
        if (skillIds == null || skillIds.isEmpty()) {
            candidates = availability == null ? creators.findAll() : creators.findByAvailability(availability);
        } else {
            candidates = creators.findRadarCandidates(skillIds, availability);
        }
        return candidates.stream().map(this::toCard).toList();
    }

    private CreatorCardResponse toCard(CreatorProfile creator) {
        List<String> skillNames = creatorSkills.findByCreatorProfileId(creator.getId()).stream()
                .map(link -> link.getSkill().getName())
                .toList();
        return new CreatorCardResponse(
                creator.getId(),
                creator.getHeadline(),
                creator.getBio(),
                creator.getExperienceYears(),
                creator.getAvailability().name(),
                creator.getHourlyRate(),
                creator.getOnTimeDeliveryRate(),
                creator.getAvgResponseTimeHours(),
                creator.getCompletedProjects(),
                skillNames);
    }
}
