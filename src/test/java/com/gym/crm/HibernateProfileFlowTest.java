package com.gym.crm;

import com.gym.crm.config.AppConfig;
import com.gym.crm.domain.Trainee;
import com.gym.crm.domain.Trainer;
import com.gym.crm.domain.Training;
import com.gym.crm.domain.TrainingType;
import com.gym.crm.facade.GymFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HibernateProfileFlowTest {
    private AnnotationConfigApplicationContext ctx;
    private GymFacade facade;

    @BeforeEach
    void setUp() {
        ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        facade = ctx.getBean(GymFacade.class);
    }

    @Test
    void traineeProfile_ShouldAuthenticateChangePasswordAndToggleActive() {
        Trainee trainee = new Trainee();
        trainee.setFirstName("Nick");
        trainee.setLastName("Adams");
        trainee.setActive(true);

        Trainee created = facade.createTrainee(trainee);
        String username = created.getUsername();
        String password = created.getPassword();

        assertTrue(facade.authenticateTrainee(username, password));
        assertTrue(facade.getTraineeByUsername(username, password).isPresent());

        facade.changeTraineePassword(username, password, "new-pass");
        assertFalse(facade.authenticateTrainee(username, password));
        assertTrue(facade.authenticateTrainee(username, "new-pass"));

        facade.deactivateTrainee(username, "new-pass");
        assertThrows(IllegalStateException.class, () -> facade.deactivateTrainee(username, "new-pass"));

        ctx.close();
    }

    @Test
    void trainerProfile_ShouldAuthenticateAndUpdateByUsername() {
        Trainer trainer = new Trainer();
        trainer.setFirstName("Sarah");
        trainer.setLastName("Connor");
        trainer.setSpecialization("YOGA");
        trainer.setActive(true);

        Trainer created = facade.createTrainer(trainer);

        Trainer update = new Trainer();
        update.setFirstName("Sara");
        update.setLastName("Connor");
        update.setSpecialization("STRENGTH");
        update.setActive(true);

        Trainer updated = facade.updateTrainer(created.getUsername(), created.getPassword(), update).orElseThrow();

        assertEquals("Sara", updated.getFirstName());
        assertEquals("STRENGTH", updated.getSpecialization());
        assertTrue(facade.authenticateTrainer(created.getUsername(), created.getPassword()));

        ctx.close();
    }

    @Test
    void trainingFlow_ShouldAssignTrainerAndFilterLists() {
        Trainee trainee = facade.createTrainee(profile("Tom", "Hill"));
        Trainer trainer = new Trainer();
        trainer.setFirstName("Eva");
        trainer.setLastName("Mills");
        trainer.setSpecialization("CARDIO");
        trainer.setActive(true);
        Trainer createdTrainer = facade.createTrainer(trainer);

        Training training = new Training();
        training.setTrainerId(createdTrainer.getId());
        training.setTrainingName("Cardio Basics");
        training.setTrainingType(TrainingType.CARDIO);
        training.setTrainingDate(LocalDate.of(2026, 5, 17));
        training.setTrainingDurationMinutes(40);

        Training created = facade.addTraining(trainee.getUsername(), trainee.getPassword(), training);

        assertNotNull(created.getId());
        assertEquals(1, facade.getTraineeTrainings(
                trainee.getUsername(), trainee.getPassword(), null, null, "Eva Mills", "CARDIO").size());
        assertEquals(1, facade.getTrainerTrainings(
                createdTrainer.getUsername(), createdTrainer.getPassword(), null, null, "Tom Hill").size());
        assertFalse(facade.getUnassignedTrainers(trainee.getUsername(), trainee.getPassword())
                .stream()
                .anyMatch(unassigned -> createdTrainer.getUsername().equals(unassigned.getUsername())));

        ctx.close();
    }

    @Test
    void updateTraineeTrainers_ShouldReplaceAssignedTrainerList() {
        Trainee trainee = facade.createTrainee(profile("Bill", "Sharp"));
        Trainer trainer = new Trainer();
        trainer.setFirstName("Lana");
        trainer.setLastName("Ray");
        trainer.setSpecialization("FITNESS");
        trainer.setActive(true);
        Trainer createdTrainer = facade.createTrainer(trainer);

        Trainee updated = facade.updateTraineeTrainers(
                trainee.getUsername(), trainee.getPassword(), List.of(createdTrainer.getUsername()));

        assertEquals(1, updated.getTrainers().size());

        ctx.close();
    }

    private Trainee profile(String firstName, String lastName) {
        Trainee trainee = new Trainee();
        trainee.setFirstName(firstName);
        trainee.setLastName(lastName);
        trainee.setActive(true);
        return trainee;
    }
}
