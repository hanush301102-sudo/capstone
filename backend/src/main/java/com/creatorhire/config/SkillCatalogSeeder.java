package com.creatorhire.config;

import com.creatorhire.entity.Skill;
import com.creatorhire.entity.SkillCategory;
import com.creatorhire.repository.SkillRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Seeds the skill catalog used for job requirements, creator skills, and
 * portfolio tags. Idempotent: only inserts missing names.
 */
@Configuration
public class SkillCatalogSeeder {

    private static final String[][] CATALOG = {
        {"Premiere Pro", "EDITING"},
        {"After Effects", "EDITING"},
        {"DaVinci Resolve", "EDITING"},
        {"Color Grading", "EDITING"},
        {"Motion Graphics", "EDITING"},
        {"Photoshop", "DESIGN"},
        {"Illustrator", "DESIGN"},
        {"Figma", "DESIGN"},
        {"Thumbnail Design", "DESIGN"},
        {"Brand Identity", "DESIGN"},
        {"Short-form Scripts", "SCRIPTWRITING"},
        {"YouTube Scripts", "SCRIPTWRITING"},
        {"Ad Copy", "SCRIPTWRITING"},
        {"Storyboarding", "SCRIPTWRITING"},
        {"Documentary Writing", "SCRIPTWRITING"},
    };

    @Bean
    ApplicationRunner seedSkills(SkillRepository skills) {
        return args -> {
            for (String[] entry : CATALOG) {
                if (skills.findByName(entry[0]).isEmpty()) {
                    skills.save(new Skill(entry[0], SkillCategory.valueOf(entry[1])));
                }
            }
        };
    }
}
