package com.gym.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.rest.auth.RestAuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public abstract class BaseControllerTest {

    protected MockMvc mockMvc;

    protected GymFacade facade = Mockito.mock(GymFacade.class);
    protected RestAuthenticationService auth = Mockito.mock(RestAuthenticationService.class);

    protected ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(getController())
                .build();
    }

    protected abstract Object getController();
}