package com.creatorhire.service;

import java.util.List;
import com.creatorhire.dto.PortfolioRequest;
import com.creatorhire.dto.PortfolioResponse;
import com.creatorhire.entity.CreatorProfile;
import com.creatorhire.entity.Portfolio;
import com.creatorhire.entity.PortfolioSkill;
import com.creatorhire.entity.Skill;
import com.creatorhire.entity.User;
import com.creatorhire.entity.VerificationStatus;
import com.creatorhire.exception.ForbiddenException;
import com.creatorhire.exception.ResourceNotFoundException;
import com.creatorhire.repository.CreatorProfileRepository;
import com.creatorhire.repository.PortfolioRepository;
import com.creatorhire.repository.PortfolioSkillRepository;
import com.creatorhire.repository.SkillRepository;
import com.creatorhire.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolios;
    private final PortfolioSkillRepository portfolioSkills;
    private final SkillRepository skills;
    private final CreatorProfileRepository creators;
    private final UserRepository users;

    public PortfolioService(
            PortfolioRepository portfolios,
            PortfolioSkillRepository portfolioSkills,
            SkillRepository skills,
            CreatorProfileRepository creators,
            UserRepository users) {
        this.portfolios = portfolios;
        this.portfolioSkills = portfolioSkills;
        this.skills = skills;
        this.creators = creators;
        this.users = users;
    }

    @Transactional
    public PortfolioResponse create(PortfolioRequest request) {
        CreatorProfile creator = currentCreator();
        Portfolio portfolio = new Portfolio();
        portfolio.setCreatorProfile(creator);
        apply(portfolio, request);
        portfolios.save(portfolio);
        attachSkills(portfolio, request.skillIds());
        return toResponse(portfolio);
    }

    @Transactional
    public PortfolioResponse update(Long id, PortfolioRequest request) {
        Portfolio portfolio = ownedPortfolio(id);
        apply(portfolio, request);
        portfolioSkills.findByPortfolioId(id).forEach(portfolioSkills::delete);
        portfolioSkills.flush();
        attachSkills(portfolio, request.skillIds());
        // Editing re-opens verification.
        if (portfolio.getVerificationStatus() == VerificationStatus.VERIFIED) {
            portfolio.setVerificationStatus(VerificationStatus.UNVERIFIED);
        }
        return toResponse(portfolio);
    }

    @Transactional
    public void delete(Long id) {
        Portfolio portfolio = ownedPortfolio(id);
        portfolioSkills.findByPortfolioId(id).forEach(portfolioSkills::delete);
        portfolioSkills.flush();
        portfolios.delete(portfolio);
    }

    @Transactional(readOnly = true)
    public List<PortfolioResponse> mine() {
        return portfolios.findByCreatorProfileId(currentCreator().getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PortfolioResponse> byCreator(Long creatorProfileId) {
        return portfolios.findByCreatorProfileId(creatorProfileId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PortfolioResponse requestVerification(Long id) {
        Portfolio portfolio = ownedPortfolio(id);
        if (portfolio.getVerificationStatus() == VerificationStatus.VERIFIED) {
            throw new IllegalArgumentException("Portfolio is already verified");
        }
        portfolio.setVerificationStatus(VerificationStatus.PENDING);
        return toResponse(portfolio);
    }

    @Transactional
    public PortfolioResponse review(Long id, boolean approved) {
        Portfolio portfolio = portfolios.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found: " + id));
        portfolio.setVerificationStatus(
                approved ? VerificationStatus.VERIFIED : VerificationStatus.UNVERIFIED);
        return toResponse(portfolio);
    }

    @Transactional(readOnly = true)
    public List<PortfolioResponse> pendingVerification() {
        return portfolios.findByVerificationStatus(VerificationStatus.PENDING).stream()
                .map(this::toResponse)
                .toList();
    }

    private void apply(Portfolio portfolio, PortfolioRequest request) {
        portfolio.setTitle(request.title());
        portfolio.setDescription(request.description());
        portfolio.setMediaUrl(request.mediaUrl());
        portfolio.setWorkType(request.workType());
        portfolio.setCollaborationRole(request.collaborationRole());
        portfolio.setOutcomeStats(request.outcomeStats());
    }

    private void attachSkills(Portfolio portfolio, List<Long> skillIds) {
        if (skillIds == null) {
            return;
        }
        for (Long skillId : skillIds) {
            Skill skill = skills.findById(skillId)
                    .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + skillId));
            PortfolioSkill link = new PortfolioSkill();
            link.setPortfolio(portfolio);
            link.setSkill(skill);
            portfolioSkills.save(link);
        }
    }

    private CreatorProfile currentCreator() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = users.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return creators.findByUserId(user.getId())
                .orElseThrow(() -> new ForbiddenException("Creator profile required"));
    }

    private Portfolio ownedPortfolio(Long id) {
        Portfolio portfolio = portfolios.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found: " + id));
        if (!portfolio.getCreatorProfile().getId().equals(currentCreator().getId())) {
            throw new ForbiddenException("You do not own this portfolio item");
        }
        return portfolio;
    }

    private PortfolioResponse toResponse(Portfolio portfolio) {
        List<String> skillNames = portfolioSkills.findByPortfolioId(portfolio.getId()).stream()
                .map(link -> link.getSkill().getName())
                .toList();
        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getCreatorProfile().getId(),
                portfolio.getTitle(),
                portfolio.getDescription(),
                portfolio.getMediaUrl(),
                portfolio.getWorkType(),
                portfolio.getVerificationStatus().name(),
                portfolio.getCollaborationRole(),
                portfolio.getOutcomeStats(),
                portfolio.getCreatedAt(),
                skillNames);
    }
}
