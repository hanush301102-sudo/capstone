package com.creatorhire.controller;

import java.util.List;
import com.creatorhire.dto.ApplicationRequest;
import com.creatorhire.dto.ApplicationResponse;
import com.creatorhire.dto.ApplicationStatusRequest;
import com.creatorhire.entity.ApplicationStatus;
import com.creatorhire.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> apply(@Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.apply(request));
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> byJob(@RequestParam Long jobId) {
        return ResponseEntity.ok(applicationService.byJob(jobId));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ApplicationResponse>> mine() {
        return ResponseEntity.ok(applicationService.mine());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable Long id, @Valid @RequestBody ApplicationStatusRequest request) {
        return ResponseEntity.ok(
                applicationService.updateStatus(id, ApplicationStatus.valueOf(request.status())));
    }
}
