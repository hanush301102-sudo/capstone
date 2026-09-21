package com.creatorhire.service;

import java.util.List;
import com.creatorhire.dto.ReportRequest;
import com.creatorhire.dto.ReportResponse;
import com.creatorhire.entity.Job;
import com.creatorhire.entity.Report;
import com.creatorhire.entity.ReportStatus;
import com.creatorhire.entity.User;
import com.creatorhire.exception.ResourceNotFoundException;
import com.creatorhire.repository.JobRepository;
import com.creatorhire.repository.ReportRepository;
import com.creatorhire.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final ReportRepository reports;
    private final UserRepository users;
    private final JobRepository jobs;

    public ReportService(ReportRepository reports, UserRepository users, JobRepository jobs) {
        this.reports = reports;
        this.users = users;
        this.jobs = jobs;
    }

    @Transactional
    public ReportResponse file(ReportRequest request) {
        if (request.reportedUserId() == null && request.jobId() == null) {
            throw new IllegalArgumentException("Report must target a user or a job");
        }
        Report report = new Report();
        report.setReporter(currentUser());
        if (request.reportedUserId() != null) {
            report.setReportedUser(users.findById(request.reportedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.reportedUserId())));
        }
        if (request.jobId() != null) {
            Job job = jobs.findById(request.jobId())
                    .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + request.jobId()));
            report.setJob(job);
        }
        report.setReason(request.reason());
        report.setDescription(request.description());
        reports.save(report);
        return toResponse(report);
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> byStatus(ReportStatus status) {
        List<Report> result = status == null ? reports.findAll() : reports.findByStatus(status);
        return result.stream().map(this::toResponse).toList();
    }

    @Transactional
    public ReportResponse review(Long id, ReportStatus status) {
        Report report = reports.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + id));
        report.setStatus(status);
        return toResponse(report);
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return users.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private ReportResponse toResponse(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getReporter().getEmail(),
                report.getReportedUser() == null ? null : report.getReportedUser().getId(),
                report.getJob() == null ? null : report.getJob().getId(),
                report.getReason(),
                report.getDescription(),
                report.getStatus().name(),
                report.getCreatedAt());
    }
}
