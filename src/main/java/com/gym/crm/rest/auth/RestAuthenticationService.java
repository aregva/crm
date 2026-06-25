package com.gym.crm.rest.auth;

import com.gym.crm.facade.GymFacade;
import com.gym.crm.security.GymUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class RestAuthenticationService {
    private final GymFacade facade;

    public RestAuthenticationService(GymFacade facade) {
        this.facade = facade;
    }

    public AuthenticatedUser requireAuthenticated(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Authentication is required");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof GymUserDetails userDetails) {
            return new AuthenticatedUser(userDetails.getUsername(), userDetails.getRole());
        }

        return new AuthenticatedUser(authentication.getName(), resolveRole(authentication));
    }

    public AuthenticatedUser requireTrainee(HttpServletRequest request, String username) {
        AuthenticatedUser user = requireAuthenticated(request);
        if (user.role() != RestUserRole.TRAINEE || !user.username().equals(username)) {
            throw new SecurityException("Invalid trainee credentials");
        }
        return user;
    }

    public AuthenticatedUser requireTrainer(HttpServletRequest request, String username) {
        AuthenticatedUser user = requireAuthenticated(request);
        if (user.role() != RestUserRole.TRAINER || !user.username().equals(username)) {
            throw new SecurityException("Invalid trainer credentials");
        }
        return user;
    }

    public RestUserRole authenticateUsernamePassword(String username, String password) {
        if (facade.authenticateTrainee(username, password)) {
            return RestUserRole.TRAINEE;
        }
        if (facade.authenticateTrainer(username, password)) {
            return RestUserRole.TRAINER;
        }
        throw new SecurityException("Invalid credentials");
    }

    private RestUserRole resolveRole(Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_TRAINEE".equals(authority.getAuthority())) {
                return RestUserRole.TRAINEE;
            }
            if ("ROLE_TRAINER".equals(authority.getAuthority())) {
                return RestUserRole.TRAINER;
            }
        }
        throw new SecurityException("Authenticated user role is missing");
    }
}
