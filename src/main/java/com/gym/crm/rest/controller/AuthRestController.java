package com.gym.crm.rest.controller;

import com.gym.crm.facade.GymFacade;
import com.gym.crm.rest.auth.RestAuthenticationService;
import com.gym.crm.rest.auth.RestUserRole;
import com.gym.crm.rest.dto.ChangeLoginRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthRestController {

    private final GymFacade facade;
    private final RestAuthenticationService authenticationService;

    public AuthRestController(GymFacade facade,
                              RestAuthenticationService authenticationService) {
        this.facade = facade;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/login")
    public ResponseEntity<Void> login(@RequestParam String username,
                                      @RequestParam String password) {

        requireText(username, "username");
        requireText(password, "password");

        authenticationService.authenticateUsernamePassword(username, password);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/login")
    public ResponseEntity<Void> changeLogin(@Valid @RequestBody ChangeLoginRequest request) {

        RestUserRole role = authenticationService.authenticateUsernamePassword(
                request.username(),
                request.oldPassword()
        );

        if (role == RestUserRole.TRAINEE) {
            facade.changeTraineePassword(request.username(), request.oldPassword(), request.newPassword());
        } else {
            facade.changeTrainerPassword(request.username(), request.oldPassword(), request.newPassword());
        }

        return ResponseEntity.ok().build();
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}