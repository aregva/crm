package com.gym.crm;

import com.gym.crm.config.AppConfig;
import com.gym.crm.domain.Trainee;
import com.gym.crm.domain.Trainer;
import com.gym.crm.domain.Training;
import com.gym.crm.domain.TrainingType;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TrainingServiceTest {

    private AnnotationConfigApplicationContext ctx;
    private TrainingService trainingService;
    private TraineeService traineeService;
    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        trainingService = ctx.getBean(TrainingService.class);
        traineeService = ctx.getBean(TraineeService.class);
        trainerService = ctx.getBean(TrainerService.class);
    }

    @Test
    void createAndSelect_ShouldWork() {
        Training training = new Training();
        training.setTraineeId(1L);
        training.setTrainerId(1L);
        training.setTrainingName("Morning Cardio");
        training.setTrainingType(TrainingType.CARDIO);
        training.setTrainingDate(LocalDate.now());
        training.setTrainingDurationMinutes(45);

        Training created = trainingService.create(training);
        Optional<Training> selected = trainingService.select(created.getId());

        assertNotNull(created.getId());
        assertTrue(selected.isPresent());
        assertEquals("Morning Cardio", selected.get().getTrainingName());
        ctx.close();
    }

    @Test
    void select_NotFound_ShouldReturnEmpty() {
        Optional<Training> opt = trainingService.select(1234567L);
        assertTrue(opt.isEmpty());
        ctx.close();
    }

    @Test
    void create_MissingRequiredFields_ShouldThrowException() {
        Training training = validTraining(1L, 1L);
        training.setTrainingName(" ");

        assertThrows(IllegalArgumentException.class, () -> trainingService.create(training));
        ctx.close();
    }

    @Test
    void create_InvalidDuration_ShouldThrowException() {
        Training training = validTraining(1L, 1L);
        training.setTrainingDurationMinutes(0);

        assertThrows(IllegalArgumentException.class, () -> trainingService.create(training));
        ctx.close();
    }

    @Test
    void create_UnknownTrainee_ShouldThrowException() {
        Training training = validTraining(999999L, 1L);

        assertThrows(IllegalArgumentException.class, () -> trainingService.create(training));
        ctx.close();
    }

    @Test
    void create_UnknownTrainer_ShouldThrowException() {
        Training training = validTraining(1L, 999999L);

        assertThrows(IllegalArgumentException.class, () -> trainingService.create(training));
        ctx.close();
    }

    @Test
    void create_UnknownTrainingType_ShouldThrowException() {
        Training training = validTraining(1L, 1L);
        training.setTrainingType(new TrainingType(999999L, "UNKNOWN"));

        assertThrows(IllegalArgumentException.class, () -> trainingService.create(training));
        ctx.close();
    }

    @Test
    void addTraining_WithInvalidTraineeCredentials_ShouldThrowSecurityException() {
        Trainee trainee = traineeService.create(trainee("Alan", "West"));
        Trainer trainer = trainerService.create(trainer("Dana", "Hall"));
        Training training = validTraining(trainee.getId(), trainer.getId());

        assertThrows(SecurityException.class,
                () -> trainingService.addTraining(trainee.getUsername(), "wrong", training));
        ctx.close();
    }

    @Test
    void getTraineeTrainings_WithInvalidCredentials_ShouldThrowSecurityException() {
        Trainee trainee = traineeService.create(trainee("Chris", "Ford"));

        assertThrows(SecurityException.class,
                () -> trainingService.getTraineeTrainings(
                        trainee.getUsername(), "wrong", null, null, null, null));
        ctx.close();
    }

    @Test
    void getTrainerTrainings_WithInvalidCredentials_ShouldThrowSecurityException() {
        Trainer trainer = trainerService.create(trainer("Mona", "King"));

        assertThrows(SecurityException.class,
                () -> trainingService.getTrainerTrainings(
                        trainer.getUsername(), "wrong", null, null, null));
        ctx.close();
    }

    @Test
    void getTrainingsByCriteria_ShouldReturnMatchingRecords() {
        Trainee trainee = traineeService.create(trainee("Peter", "Young"));
        Trainer trainer = trainerService.create(trainer("Clara", "Snow"));
        Training training = validTraining(trainee.getId(), trainer.getId());
        training.setTrainingDate(LocalDate.of(2026, 5, 20));
        trainingService.create(training);

        assertEquals(1, trainingService.getTraineeTrainings(
                trainee.getUsername(),
                trainee.getPassword(),
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                "Clara Snow",
                "CARDIO").size());
        assertEquals(1, trainingService.getTrainerTrainings(
                trainer.getUsername(),
                trainer.getPassword(),
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                "Peter Young").size());
        ctx.close();
    }

    private Training validTraining(Long traineeId, Long trainerId) {
        Training training = new Training();
        training.setTraineeId(traineeId);
        training.setTrainerId(trainerId);
        training.setTrainingName("Morning Cardio");
        training.setTrainingType(TrainingType.CARDIO);
        training.setTrainingDate(LocalDate.now());
        training.setTrainingDurationMinutes(45);
        return training;
    }

    private Trainee trainee(String firstName, String lastName) {
        Trainee trainee = new Trainee();
        trainee.setFirstName(firstName);
        trainee.setLastName(lastName);
        trainee.setActive(true);
        return trainee;
    }

    private Trainer trainer(String firstName, String lastName) {
        Trainer trainer = new Trainer();
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setSpecialization("CARDIO");
        trainer.setActive(true);
        return trainer;
    }
}
