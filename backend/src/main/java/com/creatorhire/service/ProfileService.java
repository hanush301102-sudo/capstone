package com.creatorhire.service;

import java.util.List;
import com.creatorhire.dto.ClientProfileRequest;
import com.creatorhire.dto.ClientProfileResponse;
import com.creatorhire.dto.CreatorProfileRequest;
import com.creatorhire.dto.CreatorProfileResponse;
import com.creatorhire.entity.Availability;
import com.creatorhire.entity.ClientProfile;
import com.creatorhire.entity.CreatorProfile;
import com.creatorhire.entity.CreatorSkill;
import com.creatorhire.entity.Skill;
import com.creatorhire.entity.SkillLevel;
import com.creatorhire.entity.User;
import com.creatorhire.exception.ForbiddenException;
import com.creatorhire.exception.ResourceNotFoundException;
import com.creatorhire.repository.ClientProfileRepository;
import com.creatorhire.repository.CreatorProfileRepository;
import com.creatorhire.repository.CreatorSkillRepository;
import com.creatorhire.repository.SkillRepository;
import com.creatorhire.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final ClientProfileRepository clients;
    private final CreatorProfileRepository creators;
    private final CreatorSkillRepository creatorSkills;
    private final SkillRepository skills;
    private final UserRepository users;

    public ProfileService(
            ClientProfileRepository clients,
            CreatorProfileRepository creators,
            CreatorSkillRepository creatorSkills,
            SkillRepository skills,
            UserRepository users) {
        this.clients = clients;
        this.creators = creators;
        this.creatorSkills = creatorSkills;
        this.skills = skills;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public CreatorProfileResponse myCreatorProfile() {
        return toCreatorResponse(ownCreator());
    }

    @Transactional(readOnly = true)
    public ClientProfileResponse myClientProfile() {
        return toClientResponse(ownClient());
    }

    @Transactional
    public CreatorProfileResponse updateCreator(CreatorProfileRequest request) {
        CreatorProfile creator = ownCreator();
        creator.setHeadline(request.headline());
        creator.setBio(request.bio());
        creator.setExperienceYears(request.experienceYears());
        if (request.availability() != null) {
            creator.setAvailability(Availability.valueOf(request.availability()));
        }
        creator.setHourlyRate(request.hourlyRate());
        if (request.skills() != null) {
            creatorSkills.findByCreatorProfileId(creator.getId()).forEach(creatorSkills::delete);
            creatorSkills.flush();
            for (var input : request.skills()) {
                Skill skill = skills.findById(input.skillId())
                        .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + input.skillId()));
                CreatorSkill link = new CreatorSkill();
                link.setCreatorProfile(creator);
                link.setSkill(skill);
                link.setLevel(SkillLevel.valueOf(input.level()));
                creatorSkills.save(link);
            }
        }
        return toCreatorResponse(creator);
    }

    @Transactional
    public ClientProfileResponse updateClient(ClientProfileRequest request) {
        ClientProfile client = ownClient();
        client.setCompanyName(request.companyName());
        client.setBio(request.bio());
        client.setIndustry(request.industry());
        return toClientResponse(client);
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return users.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private CreatorProfile ownCreator() {
        User user = currentUser();
        return creators.findByUserId(user.getId())
                .orElseThrow(() -> new ForbiddenException("Creator profile required"));
    }

    private ClientProfile ownClient() {
        User user = currentUser();
        return clients.findByUserId(user.getId())
                .orElseThrow(() -> new ForbiddenException("Client profile required"));
    }

    private CreatorProfileResponse toCreatorResponse(CreatorProfile creator) {
        List<CreatorProfileResponse.CreatorSkillView> skillViews = creatorSkills
                .findByCreatorProfileId(creator.getId())
                .stream()
                .map(link -> new CreatorProfileResponse.CreatorSkillView(
                        link.getSkill().getId(), link.getSkill().getName(), link.getLevel().name()))
                .toList();
        return new CreatorProfileResponse(
                creator.getId(),
                creator.getHeadline(),
                creator.getBio(),
                creator.getExperienceYears(),
                creator.getAvailability().name(),
                creator.getHourlyRate(),
                creator.getOnTimeDeliveryRate(),
                creator.getAvgResponseTimeHours(),
                creator.getCompletedProjects(),
                skillViews);
    }

    private ClientProfileResponse toClientResponse(ClientProfile client) {
        return new ClientProfileResponse(client.getId(), client.getCompanyName(), client.getBio(), client.getIndustry());
    }
}
