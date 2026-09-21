package com.creatorhire.controller;

import java.util.List;
import com.creatorhire.dto.ProjectResponse;
import com.creatorhire.dto.ProjectStatusRequest;
import com.creatorhire.entity.ProjectStatus;
import com.creatorhire.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/from-application/{applicationId}")
    public ResponseEntity<ProjectResponse> accept(@PathVariable Long applicationId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.accept(applicationId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ProjectResponse> updateStatus(
            @PathVariable Long id, @Valid @RequestBody ProjectStatusRequest request) {
        return ResponseEntity.ok(
                projectService.updateStatus(id, ProjectStatus.valueOf(request.status())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.get(id));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ProjectResponse>> mine() {
        return ResponseEntity.ok(projectService.mine());
    }
}
