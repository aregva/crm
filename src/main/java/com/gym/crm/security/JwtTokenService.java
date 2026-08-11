package com.gym.crm.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.crm.rest.auth.RestUserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtTokenService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final Duration tokenLifetime;
    private final Clock clock;

    @Autowired
    public JwtTokenService(ObjectMapper objectMapper,
                           @Value("${security.jwt.secret}") String secret,
                           @Value("${security.jwt.expiration-minutes:60}") long expirationMinutes) {
        this(objectMapper, secret, Duration.ofMinutes(expirationMinutes), Clock.systemUTC());
    }

    JwtTokenService(ObjectMapper objectMapper, String secret, Duration tokenLifetime, Clock clock) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("security.jwt.secret is required");
        }
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.tokenLifetime = tokenLifetime;
        this.clock = clock;
    }

    public GeneratedToken generateToken(GymUserDetails userDetails) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(tokenLifetime);

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", userDetails.getUsername());
        payload.put("role", userDetails.getRole().name());
        payload.put("iat", issuedAt.getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());
        payload.put("jti", UUID.randomUUID().toString());

        String encodedHeader = encodeJson(header);
        String encodedPayload = encodeJson(payload);
        String signedContent = encodedHeader + "." + encodedPayload;
        return new GeneratedToken(signedContent + "." + sign(signedContent), expiresAt);
    }

    /**
     * Mints a short-lived, role-less bearer token for service-to-service calls
     * (e.g. gym-crm calling trainer-workload-service), using the same HMAC-SHA256
     * compact-JWT scheme and shared secret as user-facing tokens.
     */
    public String generateServiceToken(String subject, String audience, Duration ttl) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(ttl);

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", subject);
        payload.put("aud", audience);
        payload.put("iat", issuedAt.getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());
        payload.put("jti", UUID.randomUUID().toString());

        String encodedHeader = encodeJson(header);
        String encodedPayload = encodeJson(payload);
        String signedContent = encodedHeader + "." + encodedPayload;
        return signedContent + "." + sign(signedContent);
    }

    public Optional<JwtClaims> parseAndValidate(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return Optional.empty();
        }

        String signedContent = parts[0] + "." + parts[1];
        String expectedSignature = sign(signedContent);
        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                parts[2].getBytes(StandardCharsets.UTF_8))) {
            return Optional.empty();
        }

        try {
            Map<String, Object> payload = objectMapper.readValue(
                    URL_DECODER.decode(parts[1]),
                    new TypeReference<>() {
                    }
            );

            String username = asText(payload.get("sub"));
            RestUserRole role = RestUserRole.valueOf(asText(payload.get("role")));
            Instant issuedAt = Instant.ofEpochSecond(asLong(payload.get("iat")));
            Instant expiresAt = Instant.ofEpochSecond(asLong(payload.get("exp")));
            String tokenId = asText(payload.get("jti"));

            if (username.isBlank() || !expiresAt.isAfter(clock.instant())) {
                return Optional.empty();
            }

            return Optional.of(new JwtClaims(username, role, tokenId, issuedAt, expiresAt));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize JWT content", ex);
        }
    }

    private String sign(String content) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return URL_ENCODER.encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to sign JWT token", ex);
        }
    }

    private String asText(Object value) {
        return value == null ? "" : value.toString();
    }

    private long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(asText(value));
    }
}
