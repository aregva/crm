package com.gym.crm.rest.controller;

import com.gym.crm.domain.Trainee;
import com.gym.crm.domain.Trainer;
import com.gym.crm.domain.Training;
import com.gym.crm.domain.TrainingType;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.rest.auth.AuthenticatedUser;
import com.gym.crm.rest.auth.RestAuthenticationService;
import com.gym.crm.rest.auth.RestUserRole;
import com.gym.crm.rest.dto.ActiveStatusRequest;
import com.gym.crm.rest.dto.AddTrainingRequest;
import com.gym.crm.rest.dto.ChangeLoginRequest;
import com.gym.crm.rest.dto.RegistrationResponse;
import com.gym.crm.rest.dto.TraineeProfileResponse;
import com.gym.crm.rest.dto.TraineeRegistrationRequest;
import com.gym.crm.rest.dto.TraineeSummaryResponse;
import com.gym.crm.rest.dto.TraineeTrainingResponse;
import com.gym.crm.rest.dto.TrainerProfileResponse;
import com.gym.crm.rest.dto.TrainerRegistrationRequest;
import com.gym.crm.rest.dto.TrainerSummaryResponse;
import com.gym.crm.rest.dto.TrainerTrainingResponse;
import com.gym.crm.rest.dto.TrainingTypeResponse;
import com.gym.crm.rest.dto.UpdateTraineeProfileRequest;
import com.gym.crm.rest.dto.UpdateTraineeTrainersRequest;
import com.gym.crm.rest.dto.UpdateTrainerProfileRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api")
@Api(tags = "Gym CRM REST API")
public class GymCrmRestController {

    private final GymFacade facade;
    private final RestAuthenticationService authenticationService;

    public GymCrmRestController(
            GymFacade facade,
            RestAuthenticationService authenticationService
    ) {
        this.facade = facade;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/trainees")
    @ApiOperation("Register trainee")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainee registered"),
            @ApiResponse(code = 400, message = "Invalid registration request")
    })
    public RegistrationResponse registerTrainee(
            @Valid @RequestBody TraineeRegistrationRequest request
    ) {
        Trainee trainee = new Trainee();
        trainee.setFirstName(request.firstName());
        trainee.setLastName(request.lastName());
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());
        trainee.setActive(true);

        Trainee created = facade.createTrainee(trainee);

        return new RegistrationResponse(
                created.getUsername(),
                created.getPassword()
        );
    }

    @PostMapping("/trainers")
    @ApiOperation("Register trainer")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainer registered"),
            @ApiResponse(code = 400, message = "Invalid registration request")
    })
    public RegistrationResponse registerTrainer(
            @Valid @RequestBody TrainerRegistrationRequest request
    ) {
        Trainer trainer = new Trainer();
        trainer.setFirstName(request.firstName());
        trainer.setLastName(request.lastName());
        trainer.setSpecialization(request.specialization());
        trainer.setActive(true);

        Trainer created = facade.createTrainer(trainer);

        return new RegistrationResponse(
                created.getUsername(),
                created.getPassword()
        );
    }

    @GetMapping("/login")
    @ApiOperation("Authenticate trainee or trainer credentials")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "username",
                    value = "Username",
                    required = true,
                    paramType = "query"
            ),
            @ApiImplicitParam(
                    name = "password",
                    value = "Password",
                    required = true,
                    paramType = "query"
            )
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "Credentials are valid"),
            @ApiResponse(code = 401, message = "Invalid credentials")
    })
    public ResponseEntity<Void> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password
    ) {
        requireText(username, "username");
        requireText(password, "password");

        authenticationService.authenticateUsernamePassword(username, password);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/login")
    @ApiOperation("Change trainee or trainer password")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Password changed"),
            @ApiResponse(code = 400, message = "Invalid password change request"),
            @ApiResponse(code = 401, message = "Invalid credentials")
    })
    public ResponseEntity<Void> changeLogin(
            @Valid @RequestBody ChangeLoginRequest request
    ) {
        RestUserRole role = authenticationService.authenticateUsernamePassword(
                request.username(),
                request.oldPassword()
        );

        if (role == RestUserRole.TRAINEE) {
            facade.changeTraineePassword(
                    request.username(),
                    request.oldPassword(),
                    request.newPassword()
            );
        } else {
            facade.changeTrainerPassword(
                    request.username(),
                    request.oldPassword(),
                    request.newPassword()
            );
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/trainees/{username}")
    @ApiOperation("Get trainee profile")
    @ApiImplicitParam(
            name = "Authorization",
            value = "Basic base64(username:password)",
            required = true,
            paramType = "header"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainee profile returned"),
            @ApiResponse(code = 401, message = "Invalid trainee credentials")
    })
    public TraineeProfileResponse getTraineeProfile(
            @PathVariable("username") String username,
            HttpServletRequest httpRequest
    ) {
        requireText(username, "username");

        AuthenticatedUser user =
                authenticationService.requireTrainee(httpRequest, username);

        Trainee trainee = facade.getTraineeByUsername(
                username,
                user.password()
        ).orElseThrow();

        return toTraineeProfileResponse(trainee);
    }

    @PutMapping("/trainees")
    @ApiOperation("Update trainee profile")
    @ApiImplicitParam(
            name = "Authorization",
            value = "Basic base64(username:password)",
            required = true,
            paramType = "header"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainee profile updated"),
            @ApiResponse(code = 400, message = "Invalid trainee profile request"),
            @ApiResponse(code = 401, message = "Invalid trainee credentials")
    })
    public TraineeProfileResponse updateTraineeProfile(
            @Valid @RequestBody UpdateTraineeProfileRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedUser user =
                authenticationService.requireTrainee(httpRequest, request.username());

        Trainee update = new Trainee();
        update.setFirstName(request.firstName());
        update.setLastName(request.lastName());
        update.setDateOfBirth(request.dateOfBirth());
        update.setAddress(request.address());
        update.setActive(request.isActive());

        Trainee updated = facade.updateTrainee(
                request.username(),
                user.password(),
                update
        ).orElseThrow();

        return toTraineeProfileResponse(updated);
    }

    @DeleteMapping("/trainees/{username}")
    @ApiOperation("Delete trainee profile")
    @ApiImplicitParam(
            name = "Authorization",
            value = "Basic base64(username:password)",
            required = true,
            paramType = "header"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainee profile deleted"),
            @ApiResponse(code = 401, message = "Invalid trainee credentials")
    })
    public ResponseEntity<Void> deleteTraineeProfile(
            @PathVariable("username") String username,
            HttpServletRequest httpRequest
    ) {
        requireText(username, "username");

        AuthenticatedUser user =
                authenticationService.requireTrainee(httpRequest, username);

        facade.deleteTrainee(username, user.password());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/trainers/{username}")
    @ApiOperation("Get trainer profile")
    @ApiImplicitParam(
            name = "Authorization",
            value = "Basic base64(username:password)",
            required = true,
            paramType = "header"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainer profile returned"),
            @ApiResponse(code = 401, message = "Invalid trainer credentials")
    })
    public TrainerProfileResponse getTrainerProfile(
            @PathVariable("username") String username,
            HttpServletRequest httpRequest
    ) {
        requireText(username, "username");

        AuthenticatedUser user =
                authenticationService.requireTrainer(httpRequest, username);

        Trainer trainer = facade.getTrainerByUsername(
                username,
                user.password()
        ).orElseThrow();

        return toTrainerProfileResponse(trainer);
    }

    @PutMapping("/trainers")
    @ApiOperation("Update trainer profile. Specialization is read-only.")
    @ApiImplicitParam(
            name = "Authorization",
            value = "Basic base64(username:password)",
            required = true,
            paramType = "header"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainer profile updated"),
            @ApiResponse(code = 400, message = "Invalid trainer profile request"),
            @ApiResponse(code = 401, message = "Invalid trainer credentials")
    })
    public TrainerProfileResponse updateTrainerProfile(
            @Valid @RequestBody UpdateTrainerProfileRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedUser user =
                authenticationService.requireTrainer(httpRequest, request.username());

        Trainer existing = facade.getTrainerByUsername(
                request.username(),
                user.password()
        ).orElseThrow();

        Trainer update = new Trainer();
        update.setFirstName(request.firstName());
        update.setLastName(request.lastName());

        // specialization is read-only
        update.setSpecialization(existing.getSpecialization());

        update.setActive(request.isActive());

        Trainer updated = facade.updateTrainer(
                request.username(),
                user.password(),
                update
        ).orElseThrow();

        return toTrainerProfileResponse(updated);
    }

    @GetMapping("/trainees/{username}/unassigned-trainers")
    public List<TrainerSummaryResponse> getUnassignedActiveTrainers(
            @PathVariable("username") String username,
            HttpServletRequest httpRequest
    ) {
        requireText(username, "username");

        AuthenticatedUser user =
                authenticationService.requireTrainee(httpRequest, username);

        return facade.getUnassignedTrainers(username, user.password())
                .stream()
                .map(this::toTrainerSummaryResponse)
                .toList();
    }

    @PutMapping("/trainees/trainers")
    public List<TrainerSummaryResponse> updateTraineeTrainers(
            @Valid @RequestBody UpdateTraineeTrainersRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedUser user =
                authenticationService.requireTrainee(
                        httpRequest,
                        request.traineeUsername()
                );

        List<String> trainerUsernames = request.trainersList()
                .stream()
                .map(t -> t.trainerUsername())
                .toList();

        Trainee updated = facade.updateTraineeTrainers(
                request.traineeUsername(),
                user.password(),
                trainerUsernames
        );

        return safeStream(updated.getTrainers())
                .map(this::toTrainerSummaryResponse)
                .sorted(Comparator.comparing(
                        TrainerSummaryResponse::trainerUsername
                ))
                .toList();
    }

    @GetMapping("/trainees/{username}/trainings")
    public List<TraineeTrainingResponse> getTraineeTrainings(
            @PathVariable("username") String username,

            @RequestParam(value = "periodFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodFrom,

            @RequestParam(value = "periodTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodTo,

            @RequestParam(value = "trainerName", required = false)
            String trainerName,

            @RequestParam(value = "trainingType", required = false)
            String trainingType,

            HttpServletRequest httpRequest
    ) {
        requireText(username, "username");

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

    @GetMapping("/trainers/{username}/trainings")
    public List<TrainerTrainingResponse> getTrainerTrainings(
            @PathVariable("username") String username,

            @RequestParam(value = "periodFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodFrom,

            @RequestParam(value = "periodTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodTo,

            @RequestParam(value = "traineeName", required = false)
            String traineeName,

            HttpServletRequest httpRequest
    ) {
        requireText(username, "username");

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

    @PostMapping("/trainings")
    public ResponseEntity<Void> addTraining(
            @Valid @RequestBody AddTrainingRequest request,
            HttpServletRequest httpRequest
    ) {
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

    @PatchMapping("/trainees/status")
    public ResponseEntity<Void> changeTraineeActiveStatus(
            @Valid @RequestBody ActiveStatusRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedUser user =
                authenticationService.requireTrainee(
                        httpRequest,
                        request.username()
                );

        if (request.isActive()) {
            facade.activateTrainee(request.username(), user.password());
        } else {
            facade.deactivateTrainee(request.username(), user.password());
        }

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/trainers/status")
    public ResponseEntity<Void> changeTrainerActiveStatus(
            @Valid @RequestBody ActiveStatusRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedUser user =
                authenticationService.requireTrainer(
                        httpRequest,
                        request.username()
                );

        if (request.isActive()) {
            facade.activateTrainer(request.username(), user.password());
        } else {
            facade.deactivateTrainer(request.username(), user.password());
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/training-types")
    public List<TrainingTypeResponse> getTrainingTypes(
            HttpServletRequest httpRequest
    ) {
        authenticationService.requireAuthenticated(httpRequest);

        return facade.getTrainingTypes()
                .stream()
                .map(type -> new TrainingTypeResponse(
                        type.getTrainingTypeName(),
                        type.getId()
                ))
                .toList();
    }

    private TraineeProfileResponse toTraineeProfileResponse(
            Trainee trainee
    ) {
        List<TrainerSummaryResponse> trainers =
                safeStream(trainee.getTrainers())
                        .map(this::toTrainerSummaryResponse)
                        .sorted(Comparator.comparing(
                                TrainerSummaryResponse::trainerUsername
                        ))
                        .toList();

        return new TraineeProfileResponse(
                trainee.getUsername(),
                trainee.getFirstName(),
                trainee.getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.isActive(),
                trainers
        );
    }

    private TrainerProfileResponse toTrainerProfileResponse(
            Trainer trainer
    ) {
        List<TraineeSummaryResponse> trainees =
                safeStream(trainer.getTrainees())
                        .map(this::toTraineeSummaryResponse)
                        .sorted(Comparator.comparing(
                                TraineeSummaryResponse::traineeUsername
                        ))
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

    private TrainerSummaryResponse toTrainerSummaryResponse(
            Trainer trainer
    ) {
        return new TrainerSummaryResponse(
                trainer.getUsername(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.getSpecialization()
        );
    }

    private TraineeSummaryResponse toTraineeSummaryResponse(
            Trainee trainee
    ) {
        return new TraineeSummaryResponse(
                trainee.getUsername(),
                trainee.getFirstName(),
                trainee.getLastName()
        );
    }

    private TraineeTrainingResponse toTraineeTrainingResponse(
            Training training
    ) {
        return new TraineeTrainingResponse(
                training.getTrainingName(),
                training.getTrainingDate(),
                trainingTypeName(training.getTrainingType()),
                training.getTrainingDurationMinutes(),
                fullName(training.getTrainer())
        );
    }

    private TrainerTrainingResponse toTrainerTrainingResponse(
            Training training
    ) {
        return new TrainerTrainingResponse(
                training.getTrainingName(),
                training.getTrainingDate(),
                trainingTypeName(training.getTrainingType()),
                training.getTrainingDurationMinutes(),
                fullName(training.getTrainee())
        );
    }

    private String trainingTypeName(TrainingType trainingType) {
        return trainingType == null
                ? null
                : trainingType.getTrainingTypeName();
    }

    private String fullName(Trainer trainer) {
        return trainer.getFirstName() + " " + trainer.getLastName();
    }

    private String fullName(Trainee trainee) {
        return trainee.getFirstName() + " " + trainee.getLastName();
    }

    private <T> Stream<T> safeStream(Collection<T> collection) {
        return collection == null
                ? Stream.empty()
                : collection.stream();
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " is required"
            );
        }
    }
}