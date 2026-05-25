package com.gym.crm;

import com.gym.crm.config.AppConfig;
import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.domain.Trainee;
import com.gym.crm.domain.Trainer;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageInitializationTest {

    @Test
    void shouldLoadDataFromInitFileOnStartup() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TraineeDao traineeDao = ctx.getBean(TraineeDao.class);
            TrainerDao trainerDao = ctx.getBean(TrainerDao.class);

            Optional<Trainee> alice = traineeDao.findAll().stream()
                    .filter(t -> "Alice".equals(t.getFirstName()) && "Green".equals(t.getLastName()))
                    .findFirst();

            Optional<Trainer> mike = trainerDao.findAll().stream()
                    .filter(t -> "Mike".equals(t.getFirstName()) && "Brown".equals(t.getLastName()))
                    .findFirst();

            assertTrue(alice.isPresent());
            assertEquals(10, alice.get().getPassword().length());
            assertTrue(mike.isPresent());
            assertEquals(10, mike.get().getPassword().length());
        }
    }
}
