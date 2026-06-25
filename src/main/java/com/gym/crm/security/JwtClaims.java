package com.gym.crm.security;

import com.gym.crm.rest.auth.RestUserRole;

import java.time.Instant;

public record JwtClaims(
        String username,
        RestUserRole role,
        String tokenId,
        Instant issuedAt,
        Instant expiresAt
) {
}
