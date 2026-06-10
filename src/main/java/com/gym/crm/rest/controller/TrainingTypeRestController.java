package com.gym.crm.rest.controller;

import com.gym.crm.facade.GymFacade;
import com.gym.crm.rest.auth.RestAuthenticationService;
import com.gym.crm.rest.dto.TrainingTypeResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/training-types")
public class TrainingTypeRestController {

    private final GymFacade facade;
    private final RestAuthenticationService authenticationService;

    public TrainingTypeRestController(GymFacade facade,
                                      RestAuthenticationService authenticationService) {
        this.facade = facade;
        this.authenticationService = authenticationService;
    }

    @GetMapping
    public List<TrainingTypeResponse> getTrainingTypes(HttpServletRequest httpRequest) {

        authenticationService.requireAuthenticated(httpRequest);

        return facade.getTrainingTypes()
                .stream()
                .map(type -> new TrainingTypeResponse(
                        type.getTrainingTypeName(),
                        type.getId()
                ))
                .toList();
    }
}