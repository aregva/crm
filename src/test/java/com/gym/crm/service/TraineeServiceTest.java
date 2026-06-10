package com.gym.crm.service;

import com.gym.crm.config.AppConfig;
import com.gym.crm.domain.Trainee;
import com.gym.crm.domain.Trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TraineeServiceTest {

    private AnnotationConfigApplicationContext ctx;
    private TraineeService traineeService;
    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        traineeService = ctx.getBean(TraineeService.class);
        trainerService = ctx.getBean(TrainerService.class);
    }

    @Test
    void create_ShouldGenerateUsernameAndPassword() {
        Trainee t = new Trainee();
        t.setFirstName("John");
        t.setLastName("Smith");
        t.setAddress("NY");
        t.setDateOfBirth(LocalDate.of(1998, 3, 15));
        t.setActive(true);

        Trainee created = traineeService.create(t);

        assertNotNull(created.getId());
        assertEquals("John.Smith", created.getUsername());
        assertNotNull(created.getPassword());
        assertEquals(10, created.getPassword().length());
        ctx.close();
    }

    @Test
    void create_SameName_ShouldAppendSerial() {
        Trainee t1 = new Trainee();
        t1.setFirstName("John");
        t1.setLastName("Smith");
        t1.setActive(true);

        Trainee t2 = new Trainee();
        t2.setFirstName("John");
        t2.setLastName("Smith");
        t2.setActive(true);

        traineeService.create(t1);
        traineeService.create(t2);

        assertEquals("John.Smith", t1.getUsername());
        assertEquals("John.Smith1", t2.getUsername());
        ctx.close();
    }

    @Test
    void update_ShouldModifyFields() {
        Trainee t = new Trainee();
        t.setFirstName("John");
        t.setLastName("Smith");
        t.setAddress("Old");
        t.setActive(true);

        Trainee created = traineeService.create(t);

        Trainee upd = new Trainee();
        upd.setFirstName("Johnny");
        upd.setLastName("Smith");
        upd.setAddress("New Address");
        upd.setDateOfBirth(LocalDate.of(2000, 1, 1));
        upd.setActive(false);

        Optional<Trainee> updated = traineeService.update(created.getId(), upd);

        assertTrue(updated.isPresent());
        assertEquals("Johnny", updated.get().getFirstName());
        assertEquals("New Address", updated.get().getAddress());
        assertFalse(updated.get().isActive());
        ctx.close();
    }

    @Test
    void update_NotFound_ShouldReturnEmpty() {
        Trainee upd = new Trainee();
        upd.setFirstName("X");
        upd.setLastName("Y");

        Optional<Trainee> updated = traineeService.update(99999L, upd);

        assertTrue(updated.isEmpty());
        ctx.close();
    }

    @Test
    void delete_ShouldRemoveEntity() {
        Trainee t = new Trainee();
        t.setFirstName("A");
        t.setLastName("B");
        t.setActive(true);

        Trainee created = traineeService.create(t);

        boolean deleted = traineeService.delete(created.getId());
        Optional<Trainee> afterDelete = traineeService.select(created.getId());

        assertTrue(deleted);
        assertTrue(afterDelete.isEmpty());
        ctx.close();
    }

    @Test
    void delete_NotFound_ShouldReturnFalse() {
        boolean deleted = traineeService.delete(123456L);
        assertFalse(deleted);
        ctx.close();
    }

    @Test
    void create_MissingRequiredField_ShouldThrowException() {
        Trainee trainee = new Trainee();
        trainee.setLastName("Smith");
        trainee.setActive(true);

        assertThrows(IllegalArgumentException.class, () -> traineeService.create(trainee));
        ctx.close();
    }

    @Test
    void selectByUsername_WithInvalidPassword_ShouldThrowSecurityException() {
        Trainee created = traineeService.create(trainee("Mark", "Stone"));

        assertThrows(SecurityException.class,
                () -> traineeService.selectByUsername(created.getUsername(), "bad-password"));
        ctx.close();
    }

    @Test
    void update_ByUsernameAndPassword_ShouldModifyFields() {
        Trainee created = traineeService.create(trainee("Linda", "Grey"));

        Trainee update = trainee("Lina", "Grey");
        update.setAddress("Updated Address");
        update.setDateOfBirth(LocalDate.of(1990, 2, 3));
        update.setActive(false);

        Optional<Trainee> updated = traineeService.update(created.getUsername(), created.getPassword(), update);

        assertTrue(updated.isPresent());
        assertEquals("Lina", updated.get().getFirstName());
        assertEquals("Updated Address", updated.get().getAddress());
        assertFalse(updated.get().isActive());
        ctx.close();
    }

    @Test
    void changePassword_ShouldRejectBlankNewPassword() {
        Trainee created = traineeService.create(trainee("Paul", "Rivers"));

        assertThrows(IllegalArgumentException.class,
                () -> traineeService.changePassword(created.getUsername(), created.getPassword(), " "));
        ctx.close();
    }

    @Test
    void activateAndDeactivate_ShouldRejectSameState() {
        Trainee created = traineeService.create(trainee("Nora", "White"));

        assertThrows(IllegalStateException.class,
                () -> traineeService.activate(created.getUsername(), created.getPassword()));

        traineeService.deactivate(created.getUsername(), created.getPassword());
        assertThrows(IllegalStateException.class,
                () -> traineeService.deactivate(created.getUsername(), created.getPassword()));

        traineeService.activate(created.getUsername(), created.getPassword());
        assertTrue(traineeService.selectByUsername(created.getUsername()).orElseThrow().isActive());
        ctx.close();
    }

    @Test
    void deleteByUsername_ShouldHardDeleteProfile() {
        Trainee created = traineeService.create(trainee("Owen", "Lake"));

        traineeService.deleteByUsername(created.getUsername(), created.getPassword());

        assertTrue(traineeService.selectByUsername(created.getUsername()).isEmpty());
        ctx.close();
    }

    @Test
    void getUnassignedTrainers_WithInvalidPassword_ShouldThrowSecurityException() {
        Trainee created = traineeService.create(trainee("Rita", "Jones"));

        assertThrows(SecurityException.class,
                () -> traineeService.getUnassignedTrainers(created.getUsername(), "wrong"));
        ctx.close();
    }

    @Test
    void updateTrainers_WithUnknownTrainer_ShouldThrowException() {
        Trainee created = traineeService.create(trainee("Sam", "North"));

        assertThrows(IllegalArgumentException.class,
                () -> traineeService.updateTrainers(
                        created.getUsername(), created.getPassword(), List.of("missing.trainer")));
        ctx.close();
    }

    @Test
    void updateTrainers_ShouldAssignExistingTrainer() {
        Trainee created = traineeService.create(trainee("Tina", "Frost"));

        Trainer trainer = new Trainer();
        trainer.setFirstName("Greg");
        trainer.setLastName("Miles");
        trainer.setSpecialization("FITNESS");
        trainer.setActive(true);
        Trainer createdTrainer = trainerService.create(trainer);

        Trainee updated = traineeService.updateTrainers(
                created.getUsername(), created.getPassword(), List.of(createdTrainer.getUsername()));

        assertEquals(1, updated.getTrainers().size());
        ctx.close();
    }

    private Trainee trainee(String firstName, String lastName) {
        Trainee trainee = new Trainee();
        trainee.setFirstName(firstName);
        trainee.setLastName(lastName);
        trainee.setAddress("Address");
        trainee.setActive(true);
        return trainee;
    }
}
