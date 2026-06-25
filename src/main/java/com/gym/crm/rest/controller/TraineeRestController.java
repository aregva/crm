package com.gym.crm.rest.controller;

import com.gym.crm.domain.Trainee;
import com.gym.crm.domain.Trainer;
import com.gym.crm.domain.Training;
import com.gym.crm.domain.TrainingType;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.rest.auth.RestAuthenticationService;
import com.gym.crm.rest.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/trainees")
public class TraineeRestController {

    private final GymFacade facade;
    private final RestAuthenticationService authenticationService;

    public TraineeRestController(GymFacade facade,
                                 RestAuthenticationService authenticationService) {
        this.facade = facade;
        this.authenticationService = authenticationService;
    }

    @PostMapping
    public RegistrationResponse registerTrainee(@Valid @RequestBody TraineeRegistrationRequest request) {

        Trainee trainee = new Trainee();
        trainee.setFirstName(request.firstName());
        trainee.setLastName(request.lastName());
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());
        trainee.setActive(true);

        Trainee created = facade.createTrainee(trainee);

        return new RegistrationResponse(created.getUsername(), created.getGeneratedPassword());
    }

    @GetMapping("/{username}")
    public TraineeProfileResponse getTraineeProfile(
            @PathVariable String username,
            HttpServletRequest httpRequest) {

        authenticationService.requireTrainee(httpRequest, username);

        Trainee trainee = facade.getTraineeByUsername(username)
                .orElseThrow();

        return toTraineeProfileResponse(trainee);
    }

    @PutMapping("/{username}")
    public TraineeProfileResponse updateTraineeProfile(
            @PathVariable String username,
            @Valid @RequestBody UpdateTraineeProfileRequest request,
            HttpServletRequest httpRequest) {

        authenticationService.requireTrainee(httpRequest, username);

        Trainee update = new Trainee();
        update.setFirstName(request.firstName());
        update.setLastName(request.lastName());
        update.setDateOfBirth(request.dateOfBirth());
        update.setAddress(request.address());
        update.setActive(request.isActive());

        Trainee updated = facade.updateTrainee(username, update)
                .orElseThrow();

        return toTraineeProfileResponse(updated);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteTraineeProfile(
            @PathVariable String username,
            HttpServletRequest httpRequest) {

        authenticationService.requireTrainee(httpRequest, username);

        facade.deleteTrainee(username);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{username}/unassigned-trainers")
    public List<TrainerSummaryResponse> getUnassignedActiveTrainers(
            @PathVariable String username,
            HttpServletRequest httpRequest) {

        requireText(username, "username");

        authenticationService.requireTrainee(httpRequest, username);

        return facade.getUnassignedTrainers(username)
                .stream()
                .map(this::toTrainerSummaryResponse)
                .toList();
    }

    @PutMapping("/{username}/trainers")
    public List<TrainerSummaryResponse> updateTraineeTrainers(
            @PathVariable String username,
            @Valid @RequestBody UpdateTraineeTrainersRequest request,
            HttpServletRequest httpRequest) {

        authenticationService.requireTrainee(httpRequest, username);

        List<String> trainerUsernames = request.trainersList()
                .stream()
                .map(TrainerUsernameRequest::trainerUsername)
                .toList();

        Trainee updated = facade.updateTraineeTrainers(
                username,
                trainerUsernames
        );

        return safeStream(updated.getTrainers())
                .map(this::toTrainerSummaryResponse)
                .toList();
    }

    @GetMapping("/{username}/trainings")
    public List<TraineeTrainingResponse> getTraineeTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType,
            HttpServletRequest httpRequest) {

        requireText(username, "username");

        authenticationService.requireTrainee(httpRequest, username);

        return facade.getTraineeTrainings(
                        username,
                        periodFrom,
                        periodTo,
                        trainerName,
                        trainingType
                )
                .stream()
                .map(this::toTraineeTrainingResponse)
                .toList();
    }

    @PatchMapping("/{username}/status")
    public ResponseEntity<Void> changeTraineeActiveStatus(
            @PathVariable String username,
            @Valid @RequestBody ActiveStatusRequest request,
            HttpServletRequest httpRequest) {

        authenticationService.requireTrainee(httpRequest, username);

        if (request.isActive()) {
            facade.activateTrainee(username);
        } else {
            facade.deactivateTrainee(username);
        }

        return ResponseEntity.ok().build();
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    private <T> Stream<T> safeStream(Collection<T> collection) {
        return collection == null ? Stream.empty() : collection.stream();
    }

    private TrainerSummaryResponse toTrainerSummaryResponse(Trainer trainer) {
        return new TrainerSummaryResponse(
                trainer.getUsername(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.getSpecialization()
        );
    }

    private TraineeProfileResponse toTraineeProfileResponse(Trainee trainee) {
        return new TraineeProfileResponse(
                trainee.getUsername(),
                trainee.getFirstName(),
                trainee.getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.isActive(),
                List.of()
        );
    }

    private TraineeTrainingResponse toTraineeTrainingResponse(Training training) {
        return new TraineeTrainingResponse(
                training.getTrainingName(),
                training.getTrainingDate(),
                training.getTrainingType() != null ? training.getTrainingType().getTrainingTypeName() : null,
                training.getTrainingDurationMinutes(),
                training.getTrainer().getFirstName() + " " + training.getTrainer().getLastName()
        );
    }
}
