package com.gym.crm.controller;

import com.gym.crm.domain.Trainee;
import com.gym.crm.rest.auth.AuthenticatedUser;
import com.gym.crm.rest.auth.RestUserRole;
import com.gym.crm.rest.controller.TraineeRestController;
import com.gym.crm.rest.dto.TraineeRegistrationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TraineeRestControllerTest extends BaseControllerTest {

    @Override
    protected Object getController() {
        return new TraineeRestController(facade, auth);
    }

    @Test
    void registerTrainee() throws Exception {

        TraineeRegistrationRequest req =
                new TraineeRegistrationRequest("John", "Doe", null, "Street");

        Trainee t = new Trainee();
        t.setUsername("john");
        t.setGeneratedPassword("pass");

        when(facade.createTrainee(any())).thenReturn(t);

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"));
    }

    @Test
    void getProfile() throws Exception {

        Trainee t = new Trainee();
        t.setUsername("john");

        when(auth.requireTrainee(any(), eq("john")))
                .thenReturn(new AuthenticatedUser("john", RestUserRole.TRAINEE));

        when(facade.getTraineeByUsername("john"))
                .thenReturn(Optional.of(t));

        mockMvc.perform(get("/api/trainees/john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"));
    }

    @Test
    void deleteProfile() throws Exception {

        when(auth.requireTrainee(any(), eq("john")))
                .thenReturn(new AuthenticatedUser("john", RestUserRole.TRAINEE));

        mockMvc.perform(delete("/api/trainees/john"))
                .andExpect(status().isNoContent());

        verify(facade).deleteTrainee("john");
    }
}
