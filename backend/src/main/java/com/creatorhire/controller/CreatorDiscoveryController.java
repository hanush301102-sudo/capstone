package com.creatorhire.controller;

import java.util.List;
import com.creatorhire.dto.CreatorCardResponse;
import com.creatorhire.entity.Availability;
import com.creatorhire.service.CreatorDiscoveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/discovery")
public class CreatorDiscoveryController {

    private final CreatorDiscoveryService discoveryService;

    public CreatorDiscoveryController(CreatorDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @GetMapping("/creators")
    public ResponseEntity<List<CreatorCardResponse>> discover(
            @RequestParam(required = false) List<Long> skillIds,
            @RequestParam(required = false) Availability availability) {
        return ResponseEntity.ok(discoveryService.discover(skillIds, availability));
    }
}
