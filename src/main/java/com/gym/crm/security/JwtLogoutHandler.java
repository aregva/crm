package com.gym.crm.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class JwtLogoutHandler implements LogoutHandler {
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;
    private final TokenRevocationService tokenRevocationService;

    public JwtLogoutHandler(JwtTokenService jwtTokenService,
                            TokenRevocationService tokenRevocationService) {
        this.jwtTokenService = jwtTokenService;
        this.tokenRevocationService = tokenRevocationService;
    }

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {
        String token = resolveBearerToken(request);
        jwtTokenService.parseAndValidate(token)
                .ifPresent(claims -> tokenRevocationService.revoke(token, claims.expiresAt()));
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        return token.isBlank() ? null : token;
    }
}
