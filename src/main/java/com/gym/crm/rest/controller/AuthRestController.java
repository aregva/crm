package com.gym.crm.rest.controller;

import com.gym.crm.facade.GymFacade;
import com.gym.crm.rest.auth.AuthenticatedUser;
import com.gym.crm.rest.auth.RestAuthenticationService;
import com.gym.crm.rest.auth.RestUserRole;
import com.gym.crm.rest.dto.AuthTokenResponse;
import com.gym.crm.rest.dto.ChangeLoginRequest;
import com.gym.crm.rest.dto.LoginRequest;
import com.gym.crm.security.GeneratedToken;
import com.gym.crm.security.GymUserDetails;
import com.gym.crm.security.JwtTokenService;
import com.gym.crm.security.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class AuthRestController {

    private final GymFacade facade;
    private final RestAuthenticationService authenticationService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final LoginAttemptService loginAttemptService;

    public AuthRestController(GymFacade facade,
                              RestAuthenticationService authenticationService,
                              AuthenticationManager authenticationManager,
                              JwtTokenService jwtTokenService,
                              LoginAttemptService loginAttemptService) {
        this.facade = facade;
        this.authenticationService = authenticationService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.loginAttemptService = loginAttemptService;
    }

    @GetMapping("/login")
    public AuthTokenResponse login(@RequestParam String username,
                                   @RequestParam String password) {

        requireText(username, "username");
        requireText(password, "password");
        return authenticate(username, password);
    }

    @PostMapping("/login")
    public AuthTokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticate(request.username(), request.password());
    }

    @PutMapping("/login")
    public ResponseEntity<Void> changeLogin(@Valid @RequestBody ChangeLoginRequest request,
                                            HttpServletRequest httpRequest) {

        AuthenticatedUser authenticatedUser = authenticationService.requireAuthenticated(httpRequest);
        if (!authenticatedUser.username().equals(request.username())) {
            throw new SecurityException("Authenticated user cannot change another user's password");
        }

        if (authenticatedUser.role() == RestUserRole.TRAINEE) {
            facade.changeTraineePassword(request.username(), request.oldPassword(), request.newPassword());
        } else {
            facade.changeTrainerPassword(request.username(), request.oldPassword(), request.newPassword());
        }

        return ResponseEntity.ok().build();
    }

    private AuthTokenResponse authenticate(String username, String password) {
        try {
            loginAttemptService.assertNotBlocked(username);
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            loginAttemptService.loginSucceeded(username);

            GymUserDetails userDetails = (GymUserDetails) authentication.getPrincipal();
            GeneratedToken token = jwtTokenService.generateToken(userDetails);
            return new AuthTokenResponse(
                    "Bearer",
                    token.value(),
                    userDetails.getUsername(),
                    userDetails.getRole(),
                    token.expiresAt()
            );
        } catch (LockedException ex) {
            throw new ResponseStatusException(HttpStatus.LOCKED, ex.getMessage(), ex);
        } catch (AuthenticationException ex) {
            loginAttemptService.loginFailed(username);
            try {
                loginAttemptService.assertNotBlocked(username);
            } catch (LockedException lockedException) {
                throw new ResponseStatusException(HttpStatus.LOCKED, lockedException.getMessage(), lockedException);
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials", ex);
        }
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}
