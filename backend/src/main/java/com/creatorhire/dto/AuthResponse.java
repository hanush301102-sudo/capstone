package com.creatorhire.dto;

import java.util.Set;

public record AuthResponse(String token, String email, Set<String> roles, boolean emailVerified) {

    public AuthResponse(String token, String email, Set<String> roles) {
        this(token, email, roles, true);
    }
}
