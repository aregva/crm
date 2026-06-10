package com.gym.crm.controller;

import com.gym.crm.domain.Trainer;
import com.gym.crm.rest.auth.AuthenticatedUser;
import com.gym.crm.rest.auth.RestUserRole;
import com.gym.crm.rest.controller.TrainerRestController;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TrainerRestControllerTest extends BaseControllerTest {

    @Override
    protected Object getController() {
        return new TrainerRestController(facade, auth);
    }

    @Test
    void registerTrainer() throws Exception {

        Trainer t = new Trainer();
        t.setUsername("john");
        t.setPassword("pass");

        when(facade.createTrainer(any())).thenReturn(t);

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new com.gym.crm.rest.dto.TrainerRegistrationRequest(
                                        "John", "Doe", "Yoga"
                                )
                        )))
                .andExpect(status().isOk());
    }

    @Test
    void getTrainerProfile() throws Exception {

        Trainer t = new Trainer();
        t.setUsername("john");

        when(auth.requireTrainer(any(), eq("john")))
                .thenReturn(new AuthenticatedUser("john", "pass", RestUserRole.TRAINER));

        when(facade.getTrainerByUsername("john", "pass"))
                .thenReturn(Optional.of(t));

        mockMvc.perform(get("/api/trainers/john"))
                .andExpect(status().isOk());
    }

    @Test
    void changeStatus() throws Exception {

        when(auth.requireTrainer(any(), eq("john")))
                .thenReturn(new AuthenticatedUser("john", "pass", RestUserRole.TRAINER));

        mockMvc.perform(patch("/api/trainers/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"username":"john","isActive":true}
                        """))
                .andExpect(status().isOk());

        verify(facade).activateTrainer("john", "pass");
    }
}