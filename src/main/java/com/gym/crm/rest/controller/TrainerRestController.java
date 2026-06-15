package com.gym.crm.rest.controller;

import com.gym.crm.domain.Trainee;
import com.gym.crm.domain.Trainer;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.rest.auth.AuthenticatedUser;
import com.gym.crm.rest.auth.RestAuthenticationService;
import com.gym.crm.rest.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/trainers")
public class TrainerRestController {

    private final GymFacade facade;
    private final RestAuthenticationService authenticationService;

    public TrainerRestController(GymFacade facade,
                                 RestAuthenticationService authenticationService) {
        this.facade = facade;
        this.authenticationService = authenticationService;
    }

    @PostMapping
    public RegistrationResponse registerTrainer(@Valid @RequestBody TrainerRegistrationRequest request) {

        Trainer trainer = new Trainer();
        trainer.setFirstName(request.firstName());
        trainer.setLastName(request.lastName());
        trainer.setSpecialization(request.specialization());
        trainer.setActive(true);

        Trainer created = facade.createTrainer(trainer);

        return new RegistrationResponse(created.getUsername(), created.getPassword());
    }

    @GetMapping("/{username}")
    public TrainerProfileResponse getTrainerProfile(@PathVariable String username,
                                                    HttpServletRequest httpRequest) {

        requireText(username, "username");

        AuthenticatedUser user =
                authenticationService.requireTrainer(httpRequest, username);

        Trainer trainer = facade.getTrainerByUsername(username, user.password()).orElseThrow();

        return toTrainerProfileResponse(trainer);
    }

    @PutMapping("/{username}")
    public TrainerProfileResponse updateTrainerProfile(
            @PathVariable String username,
            @Valid @RequestBody UpdateTrainerProfileRequest request,
            HttpServletRequest httpRequest) {

        AuthenticatedUser user =
                authenticationService.requireTrainer(httpRequest, username);

        Trainer existing = facade.getTrainerByUsername(
                username,
                user.password()
        ).orElseThrow();

        Trainer update = new Trainer();
        update.setFirstName(request.firstName());
        update.setLastName(request.lastName());

        // specialization is read-only
        update.setSpecialization(existing.getSpecialization());
        update.setActive(request.isActive());

        Trainer updated = facade.updateTrainer(
                username,
                user.password(),
                update
        ).orElseThrow();

        return toTrainerProfileResponse(updated);
    }

    @PatchMapping("/{username}/status")
    public ResponseEntity<Void> changeTrainerActiveStatus(
            @PathVariable String username,
            @Valid @RequestBody ActiveStatusRequest request,
            HttpServletRequest httpRequest) {

        AuthenticatedUser user =
                authenticationService.requireTrainer(httpRequest, username);

        if (request.isActive()) {
            facade.activateTrainer(username, user.password());
        } else {
            facade.deactivateTrainer(username, user.password());
        }

        return ResponseEntity.ok().build();
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    private TrainerProfileResponse toTrainerProfileResponse(Trainer trainer) {
        List<TraineeSummaryResponse> trainees =
                safeStream(trainer.getTrainees())
                        .map(this::toTraineeSummaryResponse)
                        .sorted(Comparator.comparing(TraineeSummaryResponse::traineeUsername))
                        .toList();

        return new TrainerProfileResponse(
                trainer.getUsername(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.getSpecialization(),
                trainer.isActive(),
                trainees
        );
    }

    private TraineeSummaryResponse toTraineeSummaryResponse(Trainee trainee) {
        return new TraineeSummaryResponse(
                trainee.getUsername(),
                trainee.getFirstName(),
                trainee.getLastName()
        );
    }

    private <T> Stream<T> safeStream(Collection<T> collection) {
        return collection == null ? Stream.empty() : collection.stream();
    }
}