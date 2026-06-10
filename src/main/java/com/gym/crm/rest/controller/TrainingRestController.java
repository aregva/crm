package com.gym.crm.rest.controller;

import com.gym.crm.domain.Training;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.rest.auth.AuthenticatedUser;
import com.gym.crm.rest.auth.RestAuthenticationService;
import com.gym.crm.rest.dto.AddTrainingRequest;
import com.gym.crm.rest.dto.TraineeTrainingResponse;
import com.gym.crm.rest.dto.TrainerTrainingResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trainings")
public class TrainingRestController {

    private final GymFacade facade;
    private final RestAuthenticationService authenticationService;

    public TrainingRestController(GymFacade facade,
                                  RestAuthenticationService authenticationService) {
        this.facade = facade;
        this.authenticationService = authenticationService;
    }

    @PostMapping
    public ResponseEntity<Void> addTraining(
            @Valid @RequestBody AddTrainingRequest request,
            HttpServletRequest httpRequest) {

        AuthenticatedUser user =
                authenticationService.requireTrainee(
                        httpRequest,
                        request.traineeUsername()
                );

        facade.addTraining(
                request.traineeUsername(),
                user.password(),
                request.trainerUsername(),
                request.trainingName(),
                request.trainingDate(),
                request.trainingDuration()
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping("/trainee/{username}")
    public List<TraineeTrainingResponse> getTraineeTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType,
            HttpServletRequest httpRequest) {

        AuthenticatedUser user =
                authenticationService.requireTrainee(httpRequest, username);

        return facade.getTraineeTrainings(
                        username,
                        user.password(),
                        periodFrom,
                        periodTo,
                        trainerName,
                        trainingType
                )
                .stream()
                .map(this::toTraineeTrainingResponse)
                .toList();
    }

    @GetMapping("/trainer/{username}")
    public List<TrainerTrainingResponse> getTrainerTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String traineeName,
            HttpServletRequest httpRequest) {

        AuthenticatedUser user =
                authenticationService.requireTrainer(httpRequest, username);

        return facade.getTrainerTrainings(
                        username,
                        user.password(),
                        periodFrom,
                        periodTo,
                        traineeName
                )
                .stream()
                .map(this::toTrainerTrainingResponse)
                .toList();
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

    private TrainerTrainingResponse toTrainerTrainingResponse(Training training) {
        return new TrainerTrainingResponse(
                training.getTrainingName(),
                training.getTrainingDate(),
                training.getTrainingType() != null ? training.getTrainingType().getTrainingTypeName() : null,
                training.getTrainingDurationMinutes(),
                training.getTrainee().getFirstName() + " " + training.getTrainee().getLastName()
        );
    }
}