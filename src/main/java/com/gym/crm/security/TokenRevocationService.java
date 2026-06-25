package com.gym.crm.security;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenRevocationService {
    private final Map<String, Instant> revokedTokens = new ConcurrentHashMap<>();
    private final Clock clock;

    public TokenRevocationService() {
        this(Clock.systemUTC());
    }

    TokenRevocationService(Clock clock) {
        this.clock = clock;
    }

    public void revoke(String token, Instant expiresAt) {
        cleanupExpiredTokens();
        if (token != null && expiresAt != null && expiresAt.isAfter(clock.instant())) {
            revokedTokens.put(token, expiresAt);
        }
    }

    public boolean isRevoked(String token) {
        cleanupExpiredTokens();
        Instant expiresAt = revokedTokens.get(token);
        return expiresAt != null && expiresAt.isAfter(clock.instant());
    }

    private void cleanupExpiredTokens() {
        Instant now = clock.instant();
        revokedTokens.entrySet().removeIf(entry -> !entry.getValue().isAfter(now));
    }
}
