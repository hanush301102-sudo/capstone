package com.creatorhire.dto;

import java.util.Set;

public record AdminUserResponse(
        Long id, String email, String firstName, String lastName, String status, Set<String> roles) {
}
