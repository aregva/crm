package com.gym.crm.security;

import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(5);

    private final Map<String, AttemptState> attempts = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginAttemptService() {
        this(Clock.systemUTC());
    }

    LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    public void assertNotBlocked(String username) {
        String key = key(username);
        AttemptState state = attempts.get(key);
        if (state == null) {
            return;
        }

        Instant now = clock.instant();
        if (state.isBlocked(now)) {
            throw new LockedException("User is blocked for 5 minutes after 3 unsuccessful login attempts");
        }

        if (state.blockedUntil() != null && !state.isBlocked(now)) {
            attempts.remove(key);
        }
    }

    public void loginSucceeded(String username) {
        attempts.remove(key(username));
    }

    public void loginFailed(String username) {
        String key = key(username);
        Instant now = clock.instant();
        attempts.compute(key, (ignored, state) -> {
            if (state != null && state.isBlocked(now)) {
                return state;
            }

            int failedAttempts = state == null ? 1 : state.failedAttempts() + 1;
            Instant blockedUntil = failedAttempts >= MAX_FAILED_ATTEMPTS
                    ? now.plus(LOCK_DURATION)
                    : null;
            return new AttemptState(failedAttempts, blockedUntil);
        });
    }

    private String key(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    private record AttemptState(int failedAttempts, Instant blockedUntil) {
        boolean isBlocked(Instant now) {
            return blockedUntil != null && blockedUntil.isAfter(now);
        }
    }
}
