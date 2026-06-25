package com.gym.crm.controller;

import com.gym.crm.domain.TrainingType;
import com.gym.crm.rest.auth.AuthenticatedUser;
import com.gym.crm.rest.auth.RestUserRole;
import com.gym.crm.rest.controller.TrainingTypeRestController;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TrainingTypeRestControllerTest extends BaseControllerTest {

    @Override
    protected Object getController() {
        return new TrainingTypeRestController(facade, auth);
    }

    @Test
    void getTrainingTypes() throws Exception {

        TrainingType t = new TrainingType();
        t.setId(1L);
        t.setTrainingTypeName("Yoga");

        when(auth.requireAuthenticated(any()))
                .thenReturn(new AuthenticatedUser("john", RestUserRole.TRAINEE));

        when(facade.getTrainingTypes()).thenReturn(List.of(t));

        mockMvc.perform(get("/api/training-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingType").value("Yoga"))
                .andExpect(jsonPath("$[0].trainingTypeId").value(1));
    }
}
