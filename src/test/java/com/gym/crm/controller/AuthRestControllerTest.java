package com.gym.crm.controller;

import com.gym.crm.rest.auth.RestUserRole;
import com.gym.crm.rest.controller.AuthRestController;
import com.gym.crm.rest.dto.ChangeLoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthRestControllerTest extends BaseControllerTest {

    @Override
    protected Object getController() {
        return new AuthRestController(facade, auth);
    }

    @Test
    void login_success() throws Exception {

        when(auth.authenticateUsernamePassword("john", "123"))
                .thenReturn(RestUserRole.TRAINEE);

        mockMvc.perform(get("/api/login")
                        .param("username", "john")
                        .param("password", "123"))
                .andExpect(status().isOk());

        verify(auth).authenticateUsernamePassword("john", "123");
    }

    @Test
    void changeLogin_success() throws Exception {

        ChangeLoginRequest req =
                new ChangeLoginRequest("john", "old", "new");

        when(auth.authenticateUsernamePassword("john", "old"))
                .thenReturn(RestUserRole.TRAINEE);

        mockMvc.perform(put("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(auth).authenticateUsernamePassword("john", "old");
        verify(facade).changeTraineePassword("john", "old", "new");
    }
}