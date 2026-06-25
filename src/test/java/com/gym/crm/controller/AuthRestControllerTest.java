package com.gym.crm.controller;

import com.gym.crm.rest.auth.RestUserRole;
import com.gym.crm.rest.controller.AuthRestController;
import com.gym.crm.rest.dto.ChangeLoginRequest;
import com.gym.crm.security.GeneratedToken;
import com.gym.crm.security.GymUserDetails;
import com.gym.crm.security.JwtTokenService;
import com.gym.crm.security.LoginAttemptService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.Instant;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthRestControllerTest extends BaseControllerTest {
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final JwtTokenService jwtTokenService = mock(JwtTokenService.class);
    private final LoginAttemptService loginAttemptService = mock(LoginAttemptService.class);

    @Override
    protected Object getController() {
        return new AuthRestController(facade, auth, authenticationManager, jwtTokenService, loginAttemptService);
    }

    @Test
    void login_success() throws Exception {

        GymUserDetails userDetails =
                new GymUserDetails("john", "encoded", true, RestUserRole.TRAINEE);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenService.generateToken(userDetails))
                .thenReturn(new GeneratedToken("jwt-token", Instant.parse("2026-06-25T00:00:00Z")));

        mockMvc.perform(get("/api/login")
                        .param("username", "john")
                        .param("password", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.role").value("TRAINEE"));

        verify(loginAttemptService).assertNotBlocked("john");
        verify(loginAttemptService).loginSucceeded("john");
    }

    @Test
    void changeLogin_success() throws Exception {

        ChangeLoginRequest req =
                new ChangeLoginRequest("john", "old", "new");

        when(auth.requireAuthenticated(any()))
                .thenReturn(new com.gym.crm.rest.auth.AuthenticatedUser("john", RestUserRole.TRAINEE));

        mockMvc.perform(put("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(auth).requireAuthenticated(any());
        verify(facade).changeTraineePassword("john", "old", "new");
    }
}
