package com.gym.crm;

import com.gym.crm.config.AppConfig;
import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainerDao;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageInitializationTest {

    @Test
    void shouldLoadDataFromInitFileOnStartup() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TraineeDao traineeDao = ctx.getBean(TraineeDao.class);
            TrainerDao trainerDao = ctx.getBean(TrainerDao.class);

            boolean aliceLoaded = traineeDao.findAll().stream()
                    .anyMatch(t -> "Alice".equals(t.getFirstName()) && "Green".equals(t.getLastName()));

            boolean mikeLoaded = trainerDao.findAll().stream()
                    .anyMatch(t -> "Mike".equals(t.getFirstName()) && "Brown".equals(t.getLastName()));

            assertTrue(aliceLoaded);
            assertTrue(mikeLoaded);
        }
    }
}