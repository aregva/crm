package com.gym.crm;

import com.gym.crm.config.AppConfig;
import com.gym.crm.domain.Trainee;
import com.gym.crm.service.TraineeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TraineeServiceTest {

    private AnnotationConfigApplicationContext ctx;
    private TraineeService traineeService;

    @BeforeEach
    void setUp() {
        ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        traineeService = ctx.getBean(TraineeService.class);
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
}