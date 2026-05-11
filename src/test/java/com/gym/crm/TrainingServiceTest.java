package com.gym.crm;

import com.gym.crm.config.AppConfig;
import com.gym.crm.domain.Training;
import com.gym.crm.domain.TrainingType;
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

    @BeforeEach
    void setUp() {
        ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        trainingService = ctx.getBean(TrainingService.class);
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
}