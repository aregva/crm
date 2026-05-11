package com.gym.crm.facade;

import com.gym.crm.domain.Trainee;
import com.gym.crm.domain.Trainer;
import com.gym.crm.domain.Training;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GymFacade {
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public GymFacade(TraineeService traineeService, TrainerService trainerService, TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    public Trainee createTrainee(Trainee trainee) { return traineeService.create(trainee); }
    public Optional<Trainee> getTrainee(Long id) { return traineeService.select(id); }
    public Optional<Trainee> updateTrainee(Long id, Trainee trainee) { return traineeService.update(id, trainee); }
    public boolean deleteTrainee(Long id) { return traineeService.delete(id); }

    public Trainer createTrainer(Trainer trainer) { return trainerService.create(trainer); }
    public Optional<Trainer> getTrainer(Long id) { return trainerService.select(id); }
    public Optional<Trainer> updateTrainer(Long id, Trainer trainer) { return trainerService.update(id, trainer); }

    public Training createTraining(Training training) { return trainingService.create(training); }
    public Optional<Training> getTraining(Long id) { return trainingService.select(id); }
}