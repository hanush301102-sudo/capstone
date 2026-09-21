package com.creatorhire.controller;

import com.creatorhire.dto.ClientProfileRequest;
import com.creatorhire.dto.ClientProfileResponse;
import com.creatorhire.dto.CreatorProfileRequest;
import com.creatorhire.dto.CreatorProfileResponse;
import com.creatorhire.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/creator/me")
    public ResponseEntity<CreatorProfileResponse> myCreatorProfile() {
        return ResponseEntity.ok(profileService.myCreatorProfile());
    }

    @PutMapping("/creator/me")
    public ResponseEntity<CreatorProfileResponse> updateCreator(
            @Valid @RequestBody CreatorProfileRequest request) {
        return ResponseEntity.ok(profileService.updateCreator(request));
    }

    @GetMapping("/client/me")
    public ResponseEntity<ClientProfileResponse> myClientProfile() {
        return ResponseEntity.ok(profileService.myClientProfile());
    }

    @PutMapping("/client/me")
    public ResponseEntity<ClientProfileResponse> updateClient(@RequestBody ClientProfileRequest request) {
        return ResponseEntity.ok(profileService.updateClient(request));
    }
}
