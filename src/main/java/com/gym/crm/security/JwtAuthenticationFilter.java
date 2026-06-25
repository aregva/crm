package com.gym.crm.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;
    private final TokenRevocationService tokenRevocationService;
    private final GymUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService,
                                   TokenRevocationService tokenRevocationService,
                                   GymUserDetailsService userDetailsService) {
        this.jwtTokenService = jwtTokenService;
        this.tokenRevocationService = tokenRevocationService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveBearerToken(request);

        if (token != null
                && SecurityContextHolder.getContext().getAuthentication() == null
                && !tokenRevocationService.isRevoked(token)) {
            jwtTokenService.parseAndValidate(token)
                    .ifPresent(claims -> authenticate(request, claims));
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, JwtClaims claims) {
        try {
            GymUserDetails userDetails = userDetailsService.loadGymUserByUsername(claims.username());
            if (!userDetails.getRole().equals(claims.role()) || !userDetails.isEnabled()) {
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (UsernameNotFoundException ignored) {
            SecurityContextHolder.clearContext();
        }
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
