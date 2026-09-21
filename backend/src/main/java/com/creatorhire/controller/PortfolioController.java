package com.creatorhire.controller;

import java.util.List;
import com.creatorhire.dto.PortfolioRequest;
import com.creatorhire.dto.PortfolioResponse;
import com.creatorhire.service.PortfolioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping
    public ResponseEntity<PortfolioResponse> create(@Valid @RequestBody PortfolioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolioService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PortfolioResponse> update(
            @PathVariable Long id, @Valid @RequestBody PortfolioRequest request) {
        return ResponseEntity.ok(portfolioService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        portfolioService.delete(id);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<PortfolioResponse>> mine() {
        return ResponseEntity.ok(portfolioService.mine());
    }

    @GetMapping("/creator/{creatorProfileId}")
    public ResponseEntity<List<PortfolioResponse>> byCreator(@PathVariable Long creatorProfileId) {
        return ResponseEntity.ok(portfolioService.byCreator(creatorProfileId));
    }

    @PatchMapping("/{id}/request-verification")
    public ResponseEntity<PortfolioResponse> requestVerification(@PathVariable Long id) {
        return ResponseEntity.ok(portfolioService.requestVerification(id));
    }
}
