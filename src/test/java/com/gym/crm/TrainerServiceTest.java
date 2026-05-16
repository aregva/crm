package com.gym.crm;

import com.gym.crm.config.AppConfig;
import com.gym.crm.domain.Trainer;
import com.gym.crm.service.TrainerService;
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
        tr.setSpecialization("Box");
        tr.setActive(true);

        Trainer created = trainerService.create(tr);

        assertNotNull(created.getId());
        assertEquals("Mike.Tyson", created.getUsername());
        assertNotNull(created.getPassword());
        assertEquals(10, created.getPassword().length());
        ctx.close();
    }

    @Test
    void create_SameAsPreloadedTrainer_ShouldAppendSerial() {
        // preloaded: Mike.Brown from trainers-data.txt
        Trainer tr = new Trainer();
        tr.setFirstName("Mike");
        tr.setLastName("Brown");
        tr.setSpecialization("Strength");
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
        tr.setSpecialization("Yoga");
        tr.setActive(true);

        Trainer created = trainerService.create(tr);

        Trainer upd = new Trainer();
        upd.setFirstName("Ann");
        upd.setLastName("Lee");
        upd.setSpecialization("Pilates");
        upd.setActive(false);

        Optional<Trainer> updated = trainerService.update(created.getId(), upd);

        assertTrue(updated.isPresent());
        assertEquals("Ann", updated.get().getFirstName());
        assertEquals("Pilates", updated.get().getSpecialization());
        assertFalse(updated.get().isActive());
        ctx.close();
    }

    @Test
    void select_NotFound_ShouldReturnEmpty() {
        Optional<Trainer> opt = trainerService.select(999999L);
        assertTrue(opt.isEmpty());
        ctx.close();
    }
}
