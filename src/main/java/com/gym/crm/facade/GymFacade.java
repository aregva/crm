package com.gym.crm.facade;

import com.gym.crm.domain.Trainee;
import com.gym.crm.domain.Trainer;
import com.gym.crm.domain.Training;
import com.gym.crm.domain.TrainingType;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingService;
import com.gym.crm.service.TrainingTypeService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class GymFacade {
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;

    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService,
                     TrainingTypeService trainingTypeService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
    }

    public Trainee createTrainee(Trainee trainee) { return traineeService.create(trainee); }
    public Optional<Trainee> getTrainee(Long id) { return traineeService.select(id); }
    public Optional<Trainee> getTraineeByUsername(String username) { return traineeService.selectByUsername(username); }
    public Optional<Trainee> getTraineeByUsername(String username, String password) {
        return traineeService.selectByUsername(username, password);
    }
    public Optional<Trainee> updateTrainee(Long id, Trainee trainee) { return traineeService.update(id, trainee); }
    public Optional<Trainee> updateTrainee(String username, String password, Trainee trainee) {
        return traineeService.update(username, password, trainee);
    }
    public Optional<Trainee> updateTrainee(String username, Trainee trainee) {
        return traineeService.updateAuthenticated(username, trainee);
    }
    public boolean deleteTrainee(Long id) { return traineeService.delete(id); }
    public void deleteTrainee(String username, String password) { traineeService.deleteByUsername(username, password); }
    public void deleteTrainee(String username) { traineeService.deleteByUsernameAuthenticated(username); }
    public boolean authenticateTrainee(String username, String password) { return traineeService.authenticate(username, password); }
    public void changeTraineePassword(String username, String oldPassword, String newPassword) {
        traineeService.changePassword(username, oldPassword, newPassword);
    }
    public void activateTrainee(String username, String password) { traineeService.activate(username, password); }
    public void deactivateTrainee(String username, String password) { traineeService.deactivate(username, password); }
    public void activateTrainee(String username) { traineeService.activateAuthenticated(username); }
    public void deactivateTrainee(String username) { traineeService.deactivateAuthenticated(username); }
    public List<Trainer> getUnassignedTrainers(String traineeUsername, String password) {
        return traineeService.getUnassignedTrainers(traineeUsername, password);
    }
    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        return traineeService.getUnassignedTrainersAuthenticated(traineeUsername);
    }
    public Trainee updateTraineeTrainers(String traineeUsername, String password, List<String> trainerUsernames) {
        return traineeService.updateTrainers(traineeUsername, password, trainerUsernames);
    }
    public Trainee updateTraineeTrainers(String traineeUsername, List<String> trainerUsernames) {
        return traineeService.updateTrainersAuthenticated(traineeUsername, trainerUsernames);
    }

    public Trainer createTrainer(Trainer trainer) { return trainerService.create(trainer); }
    public Optional<Trainer> getTrainer(Long id) { return trainerService.select(id); }
    public Optional<Trainer> getTrainerByUsername(String username) { return trainerService.selectByUsername(username); }
    public Optional<Trainer> getTrainerByUsername(String username, String password) {
        return trainerService.selectByUsername(username, password);
    }
    public Optional<Trainer> updateTrainer(Long id, Trainer trainer) { return trainerService.update(id, trainer); }
    public Optional<Trainer> updateTrainer(String username, String password, Trainer trainer) {
        return trainerService.update(username, password, trainer);
    }
    public Optional<Trainer> updateTrainer(String username, Trainer trainer) {
        return trainerService.updateAuthenticated(username, trainer);
    }
    public boolean authenticateTrainer(String username, String password) { return trainerService.authenticate(username, password); }
    public void changeTrainerPassword(String username, String oldPassword, String newPassword) {
        trainerService.changePassword(username, oldPassword, newPassword);
    }
    public void activateTrainer(String username, String password) { trainerService.activate(username, password); }
    public void deactivateTrainer(String username, String password) { trainerService.deactivate(username, password); }
    public void activateTrainer(String username) { trainerService.activateAuthenticated(username); }
    public void deactivateTrainer(String username) { trainerService.deactivateAuthenticated(username); }

    public Training createTraining(Training training) { return trainingService.create(training); }
    public Training addTraining(String traineeUsername, String traineePassword, Training training) {
        return trainingService.addTraining(traineeUsername, traineePassword, training);
    }
    public Training addTraining(String traineeUsername,
                                String traineePassword,
                                String trainerUsername,
                                String trainingName,
                                LocalDate trainingDate,
                                int trainingDurationMinutes) {
        return trainingService.addTraining(
                traineeUsername,
                traineePassword,
                trainerUsername,
                trainingName,
                trainingDate,
                trainingDurationMinutes);
    }
    public Training addTraining(String traineeUsername,
                                String trainerUsername,
                                String trainingName,
                                LocalDate trainingDate,
                                int trainingDurationMinutes) {
        return trainingService.addTrainingAuthenticated(
                traineeUsername,
                trainerUsername,
                trainingName,
                trainingDate,
                trainingDurationMinutes);
    }
    public Optional<Training> getTraining(Long id) { return trainingService.select(id); }
    public void cancelTraining(String traineeUsername, Long trainingId) {
        trainingService.cancelTraining(traineeUsername, trainingId);
    }
    public List<Training> getTraineeTrainings(String traineeUsername,
                                              String password,
                                              LocalDate fromDate,
                                              LocalDate toDate,
                                              String trainerName,
                                              String trainingType) {
        return trainingService.getTraineeTrainings(
                traineeUsername, password, fromDate, toDate, trainerName, trainingType);
    }
    public List<Training> getTraineeTrainings(String traineeUsername,
                                              LocalDate fromDate,
                                              LocalDate toDate,
                                              String trainerName,
                                              String trainingType) {
        return trainingService.getTraineeTrainingsAuthenticated(
                traineeUsername, fromDate, toDate, trainerName, trainingType);
    }
    public List<Training> getTrainerTrainings(String trainerUsername,
                                              String password,
                                              LocalDate fromDate,
                                              LocalDate toDate,
                                              String traineeName) {
        return trainingService.getTrainerTrainings(
                trainerUsername, password, fromDate, toDate, traineeName);
    }
    public List<Training> getTrainerTrainings(String trainerUsername,
                                              LocalDate fromDate,
                                              LocalDate toDate,
                                              String traineeName) {
        return trainingService.getTrainerTrainingsAuthenticated(
                trainerUsername, fromDate, toDate, traineeName);
    }

    public List<TrainingType> getTrainingTypes() {
        return trainingTypeService.findAll();
    }
}
