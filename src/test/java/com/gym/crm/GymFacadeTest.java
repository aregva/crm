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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GymFacadeTest {

    private AnnotationConfigApplicationContext ctx;
    private GymFacade facade;

    @BeforeEach
    void setUp() {
        ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        facade = ctx.getBean(GymFacade.class);
    }

    @Test
    void createAndGetTrainee_ShouldWork() {
        Trainee t = new Trainee();
        t.setFirstName("John");
        t.setLastName("Smith");
        t.setDateOfBirth(LocalDate.of(1999, 1, 1));
        t.setAddress("NY");
        t.setActive(true);

        Trainee created = facade.createTrainee(t);
        Optional<Trainee> loaded = facade.getTrainee(created.getId());

        assertNotNull(created.getId());
        assertTrue(loaded.isPresent());
        assertEquals("John", loaded.get().getFirstName());
        assertNotNull(loaded.get().getUsername());
        assertEquals(10, created.getGeneratedPassword().length());
        assertTrue(loaded.get().getPassword().startsWith("$2"));

        ctx.close();
    }

    @Test
    void updateAndDeleteTrainee_ShouldWork() {
        Trainee t = new Trainee();
        t.setFirstName("Alex");
        t.setLastName("Stone");
        t.setAddress("Old");
        t.setActive(true);

        Trainee created = facade.createTrainee(t);

        Trainee upd = new Trainee();
        upd.setFirstName("Alexander");
        upd.setLastName("Stone");
        upd.setAddress("New");
        upd.setDateOfBirth(LocalDate.of(2000, 5, 5));
        upd.setActive(false);

        Optional<Trainee> updated = facade.updateTrainee(created.getId(), upd);
        assertTrue(updated.isPresent());
        assertEquals("Alexander", updated.get().getFirstName());
        assertEquals("New", updated.get().getAddress());

        boolean deleted = facade.deleteTrainee(created.getId());
        assertTrue(deleted);
        assertTrue(facade.getTrainee(created.getId()).isEmpty());

        ctx.close();
    }

    @Test
    void createAndGetTrainer_ShouldWork() {
        Trainer tr = new Trainer();
        tr.setFirstName("Kate");
        tr.setLastName("Brown");
        tr.setSpecialization("YOGA");
        tr.setActive(true);

        Trainer created = facade.createTrainer(tr);
        Optional<Trainer> loaded = facade.getTrainer(created.getId());

        assertNotNull(created.getId());
        assertTrue(loaded.isPresent());
        assertEquals("Kate", loaded.get().getFirstName());
        assertNotNull(loaded.get().getUsername());
        assertEquals(10, created.getGeneratedPassword().length());
        assertTrue(loaded.get().getPassword().startsWith("$2"));

        ctx.close();
    }

    @Test
    void updateTrainer_ShouldWork() {
        Trainer tr = new Trainer();
        tr.setFirstName("Mark");
        tr.setLastName("Lee");
        tr.setSpecialization("CARDIO");
        tr.setActive(true);

        Trainer created = facade.createTrainer(tr);

        Trainer upd = new Trainer();
        upd.setFirstName("Marcus");
        upd.setLastName("Lee");
        upd.setSpecialization("STRENGTH");
        upd.setActive(false);

        Optional<Trainer> updated = facade.updateTrainer(created.getId(), upd);

        assertTrue(updated.isPresent());
        assertEquals("Marcus", updated.get().getFirstName());
        assertEquals("STRENGTH", updated.get().getSpecialization());
        assertFalse(updated.get().isActive());

        ctx.close();
    }

    @Test
    void createAndGetTraining_ShouldWork() {
        Training training = new Training();
        training.setTraineeId(1L);
        training.setTrainerId(1L);
        training.setTrainingName("Evening Session");
        training.setTrainingType(TrainingType.FITNESS);
        training.setTrainingDate(LocalDate.now());
        training.setTrainingDurationMinutes(60);

        Training created = facade.createTraining(training);
        Optional<Training> loaded = facade.getTraining(created.getId());

        assertNotNull(created.getId());
        assertTrue(loaded.isPresent());
        assertEquals("Evening Session", loaded.get().getTrainingName());
        assertEquals(60, loaded.get().getTrainingDurationMinutes());

        ctx.close();
    }
}
