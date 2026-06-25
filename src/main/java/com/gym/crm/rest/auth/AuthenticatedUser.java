package com.gym.crm.rest.auth;

public record AuthenticatedUser(
        String username,
        RestUserRole role
) {
}
