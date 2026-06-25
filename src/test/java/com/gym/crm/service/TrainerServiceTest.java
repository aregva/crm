package com.gym.crm.service;

import com.gym.crm.config.AppConfig;
import com.gym.crm.domain.Trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TrainerServiceTest {

    private AnnotationConfigApplicationContext ctx;
    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        trainerService = ctx.getBean(TrainerService.class);
    }

    @Test
    void create_ShouldGenerateUsernameAndPassword() {
        Trainer tr = new Trainer();
        tr.setFirstName("Mike");
        tr.setLastName("Tyson");
        tr.setSpecialization("CARDIO");
        tr.setActive(true);

        Trainer created = trainerService.create(tr);

        assertNotNull(created.getId());
        assertEquals("Mike.Tyson", created.getUsername());
        assertNotNull(created.getGeneratedPassword());
        assertEquals(10, created.getGeneratedPassword().length());
        assertTrue(created.getPassword().startsWith("$2"));
        ctx.close();
    }

    @Test
    void create_SameAsPreloadedTrainer_ShouldAppendSerial() {
        // preloaded: Mike.Brown from trainers-data.txt
        Trainer tr = new Trainer();
        tr.setFirstName("Mike");
        tr.setLastName("Brown");
        tr.setSpecialization("STRENGTH");
        tr.setActive(true);

        Trainer created = trainerService.create(tr);

        assertEquals("Mike.Brown1", created.getUsername());
        ctx.close();
    }

    @Test
    void update_ShouldModifyTrainer() {
        Trainer tr = new Trainer();
        tr.setFirstName("Anna");
        tr.setLastName("Lee");
        tr.setSpecialization("YOGA");
        tr.setActive(true);

        Trainer created = trainerService.create(tr);

        Trainer upd = new Trainer();
        upd.setFirstName("Ann");
        upd.setLastName("Lee");
        upd.setSpecialization("STRENGTH");
        upd.setActive(false);

        Optional<Trainer> updated = trainerService.update(created.getId(), upd);

        assertTrue(updated.isPresent());
        assertEquals("Ann", updated.get().getFirstName());
        assertEquals("STRENGTH", updated.get().getSpecialization());
        assertFalse(updated.get().isActive());
        ctx.close();
    }

    @Test
    void select_NotFound_ShouldReturnEmpty() {
        Optional<Trainer> opt = trainerService.select(999999L);
        assertTrue(opt.isEmpty());
        ctx.close();
    }

    @Test
    void create_MissingRequiredField_ShouldThrowException() {
        Trainer trainer = new Trainer();
        trainer.setFirstName("Mike");
        trainer.setSpecialization("CARDIO");
        trainer.setActive(true);

        assertThrows(IllegalArgumentException.class, () -> trainerService.create(trainer));
        ctx.close();
    }

    @Test
    void create_UnknownTrainingType_ShouldThrowException() {
        Trainer trainer = new Trainer();
        trainer.setFirstName("Mike");
        trainer.setLastName("Unknown");
        trainer.setSpecialization("BOXING");
        trainer.setActive(true);

        assertThrows(IllegalArgumentException.class, () -> trainerService.create(trainer));
        ctx.close();
    }

    @Test
    void update_NotFound_ShouldReturnEmpty() {
        Trainer update = new Trainer();
        update.setFirstName("Ann");
        update.setLastName("Lee");
        update.setSpecialization("CARDIO");
        update.setActive(true);

        Optional<Trainer> updated = trainerService.update(999999L, update);

        assertTrue(updated.isEmpty());
        ctx.close();
    }

    @Test
    void selectByUsername_WithInvalidPassword_ShouldThrowSecurityException() {
        Trainer created = trainerService.create(trainer("Ivy", "Moore", "YOGA"));

        assertThrows(SecurityException.class,
                () -> trainerService.selectByUsername(created.getUsername(), "wrong"));
        ctx.close();
    }

    @Test
    void update_ByUsernameAndPassword_ShouldModifyFields() {
        Trainer created = trainerService.create(trainer("Gary", "Holt", "CARDIO"));
        Trainer update = trainer("Garry", "Holt", "STRENGTH");
        update.setActive(false);

        Optional<Trainer> updated = trainerService.update(created.getUsername(), created.getGeneratedPassword(), update);

        assertTrue(updated.isPresent());
        assertEquals("Garry", updated.get().getFirstName());
        assertEquals("STRENGTH", updated.get().getSpecialization());
        assertFalse(updated.get().isActive());
        ctx.close();
    }

    @Test
    void changePassword_ShouldRejectInvalidOldPassword() {
        Trainer created = trainerService.create(trainer("Helen", "Park", "FITNESS"));

        assertThrows(SecurityException.class,
                () -> trainerService.changePassword(created.getUsername(), "bad", "new-password"));
        ctx.close();
    }

    @Test
    void changePassword_ShouldUpdateCredentials() {
        Trainer created = trainerService.create(trainer("Evan", "Cole", "CROSSFIT"));

        trainerService.changePassword(created.getUsername(), created.getGeneratedPassword(), "new-password");

        assertFalse(trainerService.authenticate(created.getUsername(), created.getGeneratedPassword()));
        assertTrue(trainerService.authenticate(created.getUsername(), "new-password"));
        ctx.close();
    }

    @Test
    void activateAndDeactivate_ShouldRejectSameState() {
        Trainer created = trainerService.create(trainer("Grace", "Hill", "YOGA"));

        assertThrows(IllegalStateException.class,
                () -> trainerService.activate(created.getUsername(), created.getGeneratedPassword()));

        trainerService.deactivate(created.getUsername(), created.getGeneratedPassword());
        assertThrows(IllegalStateException.class,
                () -> trainerService.deactivate(created.getUsername(), created.getGeneratedPassword()));

        trainerService.activate(created.getUsername(), created.getGeneratedPassword());
        assertTrue(trainerService.selectByUsername(created.getUsername()).orElseThrow().isActive());
        ctx.close();
    }

    private Trainer trainer(String firstName, String lastName, String specialization) {
        Trainer trainer = new Trainer();
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setSpecialization(specialization);
        trainer.setActive(true);
        return trainer;
    }
}
