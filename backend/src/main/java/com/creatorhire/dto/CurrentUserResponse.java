package com.creatorhire.dto;

import java.util.Set;

public record CurrentUserResponse(String email, String firstName, String lastName, Set<String> roles) {
}
