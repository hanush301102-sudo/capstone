package com.creatorhire.controller;

import java.util.List;
import com.creatorhire.entity.Skill;
import com.creatorhire.repository.SkillRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillRepository skills;

    public SkillController(SkillRepository skills) {
        this.skills = skills;
    }

    @GetMapping
    public ResponseEntity<List<Skill>> all() {
        return ResponseEntity.ok(skills.findAll());
    }
}
