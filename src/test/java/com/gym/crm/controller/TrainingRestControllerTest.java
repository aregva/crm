package com.gym.crm.controller;

import com.gym.crm.domain.Trainee;
import com.gym.crm.domain.Training;
import com.gym.crm.rest.auth.AuthenticatedUser;
import com.gym.crm.rest.auth.RestUserRole;
import com.gym.crm.rest.controller.TrainingRestController;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TrainingRestControllerTest extends BaseControllerTest {

    @Override
    protected Object getController() {
        return new TrainingRestController(facade, auth);
    }

    @Test
    void addTraining() throws Exception {

        when(auth.requireTrainee(any(), eq("trainee")))
                .thenReturn(new AuthenticatedUser("trainee", RestUserRole.TRAINEE));

        mockMvc.perform(post("/api/trainings/trainees/trainee/trainers/trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "trainingName":"Gym",
                  "trainingDuration":60,
                  "trainingDate":"2026-06-10"
                }
            """))
                .andExpect(status().isOk());

        verify(facade).addTraining(
                eq("trainee"),
                eq("trainer"),
                eq("Gym"),
                any(),
                eq(60)
        );
    }

    @Test
    void cancelTraining_Success() throws Exception {
        Training training = new Training();
        Trainee trainee = new Trainee();
        trainee.setUsername("trainee");
        training.setTrainee(trainee);

        when(facade.getTraining(5L)).thenReturn(Optional.of(training));
        when(auth.requireTrainee(any(), eq("trainee")))
                .thenReturn(new AuthenticatedUser("trainee", RestUserRole.TRAINEE));

        mockMvc.perform(delete("/api/trainings/5"))
                .andExpect(status().isNoContent());

        verify(facade).cancelTraining("trainee", 5L);
    }

    @Test
    void cancelTraining_NotFound_ShouldPropagateExceptionWithoutCancelling() {
        when(facade.getTraining(99L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> mockMvc.perform(delete("/api/trainings/99")));

        verify(facade, never()).cancelTraining(any(), any());
    }
}
