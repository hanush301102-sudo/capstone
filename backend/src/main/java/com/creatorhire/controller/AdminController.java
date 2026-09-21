package com.creatorhire.controller;

import java.util.List;
import com.creatorhire.dto.AdminUserResponse;
import com.creatorhire.dto.PortfolioResponse;
import com.creatorhire.dto.ReportResponse;
import com.creatorhire.dto.ReportStatusRequest;
import com.creatorhire.dto.UserStatusRequest;
import com.creatorhire.entity.ReportStatus;
import com.creatorhire.entity.UserStatus;
import com.creatorhire.service.AdminService;
import com.creatorhire.service.PortfolioService;
import com.creatorhire.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final ReportService reportService;
    private final PortfolioService portfolioService;

    public AdminController(
            AdminService adminService, ReportService reportService, PortfolioService portfolioService) {
        this.adminService = adminService;
        this.reportService = reportService;
        this.portfolioService = portfolioService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> users() {
        return ResponseEntity.ok(adminService.allUsers());
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<AdminUserResponse> updateUserStatus(
            @PathVariable Long id, @Valid @RequestBody UserStatusRequest request) {
        return ResponseEntity.ok(adminService.updateStatus(id, UserStatus.valueOf(request.status())));
    }

    @GetMapping("/reports")
    public ResponseEntity<List<ReportResponse>> reports(@RequestParam(required = false) String status) {
        ReportStatus filter = status == null ? null : ReportStatus.valueOf(status);
        return ResponseEntity.ok(reportService.byStatus(filter));
    }

    @PatchMapping("/reports/{id}")
    public ResponseEntity<ReportResponse> reviewReport(
            @PathVariable Long id, @Valid @RequestBody ReportStatusRequest request) {
        return ResponseEntity.ok(reportService.review(id, ReportStatus.valueOf(request.status())));
    }

    @GetMapping("/portfolios/pending")
    public ResponseEntity<List<PortfolioResponse>> pendingPortfolios() {
        return ResponseEntity.ok(portfolioService.pendingVerification());
    }

    @PatchMapping("/portfolios/{id}/verify")
    public ResponseEntity<PortfolioResponse> verify(@PathVariable Long id) {
        return ResponseEntity.ok(portfolioService.review(id, true));
    }

    @PatchMapping("/portfolios/{id}/reject-verification")
    public ResponseEntity<PortfolioResponse> rejectVerification(@PathVariable Long id) {
        return ResponseEntity.ok(portfolioService.review(id, false));
    }
}
