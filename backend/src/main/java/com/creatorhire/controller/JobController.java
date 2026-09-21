package com.creatorhire.controller;

import java.util.List;
import com.creatorhire.dto.JobRequest;
import com.creatorhire.dto.JobResponse;
import com.creatorhire.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<JobResponse> create(@Valid @RequestBody JobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> update(@PathVariable Long id, @Valid @RequestBody JobRequest request) {
        return ResponseEntity.ok(jobService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        jobService.delete(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.get(id));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<JobResponse>> mine() {
        return ResponseEntity.ok(jobService.myJobs());
    }

    @GetMapping
    public ResponseEntity<List<JobResponse>> open(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Long> skillIds) {
        return ResponseEntity.ok(jobService.openJobs(keyword, skillIds));
    }
}
