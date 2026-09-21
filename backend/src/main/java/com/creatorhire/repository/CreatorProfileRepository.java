package com.creatorhire.repository;

import java.util.List;
import java.util.Optional;
import com.creatorhire.entity.Availability;
import com.creatorhire.entity.CreatorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CreatorProfileRepository extends JpaRepository<CreatorProfile, Long> {

    Optional<CreatorProfile> findByUserId(Long userId);

    List<CreatorProfile> findByAvailability(Availability availability);

    /**
     * Creator Radar: creators having at least one of the given skills,
     * optionally filtered by availability. Used by CreatorDiscoveryService (TASK-013).
     */
    @Query("""
            select distinct cp from CreatorProfile cp
            join CreatorSkill cs on cs.creatorProfile = cp
            where cs.skill.id in :skillIds
            and (:availability is null or cp.availability = :availability)
            """)
    List<CreatorProfile> findRadarCandidates(List<Long> skillIds, Availability availability);
}
