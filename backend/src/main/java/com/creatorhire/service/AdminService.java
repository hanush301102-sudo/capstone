package com.creatorhire.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.creatorhire.dto.AdminUserResponse;
import com.creatorhire.entity.User;
import com.creatorhire.entity.UserStatus;
import com.creatorhire.exception.ResourceNotFoundException;
import com.creatorhire.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserRepository users;

    public AdminService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> allUsers() {
        return users.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public AdminUserResponse updateStatus(Long id, UserStatus status) {
        User user = users.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        user.setStatus(status);
        return toResponse(user);
    }

    private AdminUserResponse toResponse(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());
        return new AdminUserResponse(
                user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getStatus().name(), roles);
    }
}
