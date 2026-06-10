package com.gym.crm.rest.auth;

import com.gym.crm.facade.GymFacade;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class RestAuthenticationService {
    private static final String AUTHORIZATION = "Authorization";
    private static final String BASIC_PREFIX = "Basic ";

    private final GymFacade facade;

    public RestAuthenticationService(GymFacade facade) {
        this.facade = facade;
    }

    public AuthenticatedUser requireAuthenticated(HttpServletRequest request) {
        BasicCredentials credentials = readBasicCredentials(request);
        if (facade.authenticateTrainee(credentials.username(), credentials.password())) {
            return new AuthenticatedUser(credentials.username(), credentials.password(), RestUserRole.TRAINEE);
        }
        if (facade.authenticateTrainer(credentials.username(), credentials.password())) {
            return new AuthenticatedUser(credentials.username(), credentials.password(), RestUserRole.TRAINER);
        }
        throw new SecurityException("Invalid credentials");
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

    private BasicCredentials readBasicCredentials(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BASIC_PREFIX)) {
            throw new SecurityException("HTTP Basic authentication is required");
        }

        String encoded = authorization.substring(BASIC_PREFIX.length()).trim();
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new SecurityException("Invalid HTTP Basic authentication header");
        }

        int separator = decoded.indexOf(':');
        if (separator < 1) {
            throw new SecurityException("Invalid HTTP Basic authentication header");
        }
        return new BasicCredentials(decoded.substring(0, separator), decoded.substring(separator + 1));
    }

    private record BasicCredentials(String username, String password) {
    }
}
