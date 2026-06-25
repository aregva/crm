package com.gym.crm.rest.dto;

import com.gym.crm.rest.auth.RestUserRole;

import java.time.Instant;

public record AuthTokenResponse(
        String tokenType,
        String token,
        String username,
        RestUserRole role,
        Instant expiresAt
) {
}
