package com.gym.crm.service;

import com.gym.crm.actuator.metrics.GymMetrics;
import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.dao.TrainingDao;
import com.gym.crm.dao.TrainingTypeDao;
import com.gym.crm.domain.Trainee;
import com.gym.crm.domain.Trainer;
import com.gym.crm.domain.Training;
import com.gym.crm.domain.TrainingType;
import com.gym.crm.integration.workload.ActionType;
import com.gym.crm.integration.workload.TrainerWorkloadClientService;
import com.gym.crm.integration.workload.TrainerWorkloadRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class TrainingService {
    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private final TrainingDao trainingDao;
    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final TrainingTypeDao trainingTypeDao;
    private final PasswordEncoder passwordEncoder;
    private final GymMetrics metrics;
    private final TrainerWorkloadClientService workloadClientService;

    public TrainingService(TrainingDao trainingDao,
                           TraineeDao traineeDao,
                           TrainerDao trainerDao,
                           TrainingTypeDao trainingTypeDao,
                           PasswordEncoder passwordEncoder,
                           GymMetrics metrics,
                           TrainerWorkloadClientService workloadClientService) {
        this.trainingDao = trainingDao;
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.trainingTypeDao = trainingTypeDao;
        this.passwordEncoder = passwordEncoder;
        this.metrics = metrics;
        this.workloadClientService = workloadClientService;
    }

    @Transactional
    public Training create(Training training) {
        validateTraining(training);
        resolveReferences(training);

        Training saved = trainingDao.save(training);
        metrics.incrementTrainingCreated();
        log.info("Created training id={}, name={}", saved.getId(), saved.getTrainingName());
        notifyWorkload(saved, ActionType.ADD);
        return saved;
    }

    @Transactional
    public void cancelTraining(String traineeUsername, Long trainingId) {
        ValidationUtils.requireText(traineeUsername, "traineeUsername");
        ValidationUtils.requireNonNull(trainingId, "trainingId");

        Training training = trainingDao.findById(trainingId)
                .orElseThrow(() -> new NoSuchElementException("Training not found: " + trainingId));

        if (!training.getTrainee().getUsername().equals(traineeUsername)) {
            throw new SecurityException("Training does not belong to trainee: " + traineeUsername);
        }
        if (training.getTrainingDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Cannot cancel a training that has already taken place");
        }

        trainingDao.delete(training);
        log.info("Cancelled training id={}, name={}", training.getId(), training.getTrainingName());
        notifyWorkload(training, ActionType.DELETE);
    }

    private void notifyWorkload(Training training, ActionType actionType) {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                training.getTrainer().getUsername(),
                training.getTrainer().getFirstName(),
                training.getTrainer().getLastName(),
                training.getTrainer().isActive(),
                training.getTrainingDate(),
                training.getTrainingDurationMinutes(),
                actionType);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    workloadClientService.notify(request);
                }
            });
        } else {
            workloadClientService.notify(request);
        }
    }

    @Transactional
    public Training addTraining(String traineeUsername, String traineePassword, Training training) {
        if (!authenticateTrainee(traineeUsername, traineePassword)) {
            throw new SecurityException("Invalid trainee credentials");
        }
        return addTrainingAuthenticated(traineeUsername, training);
    }

    @Transactional
    public Training addTrainingAuthenticated(String traineeUsername, Training training) {
        Trainee trainee = traineeDao.findByUsername(traineeUsername).orElseThrow();
        training.setTrainee(trainee);
        return create(training);
    }

    @Transactional
    public Training addTraining(String traineeUsername,
                                String traineePassword,
                                String trainerUsername,
                                String trainingName,
                                LocalDate trainingDate,
                                int trainingDurationMinutes) {
        ValidationUtils.requireText(traineeUsername, "traineeUsername");
        ValidationUtils.requireText(trainerUsername, "trainerUsername");
        ValidationUtils.requireText(trainingName, "trainingName");

        if (!authenticateTrainee(traineeUsername, traineePassword)) {
            throw new SecurityException("Invalid trainee credentials");
        }
        return addTrainingAuthenticated(
                traineeUsername,
                trainerUsername,
                trainingName,
                trainingDate,
                trainingDurationMinutes
        );
    }

    @Transactional
    public Training addTrainingAuthenticated(String traineeUsername,
                                             String trainerUsername,
                                             String trainingName,
                                             LocalDate trainingDate,
                                             int trainingDurationMinutes) {
        ValidationUtils.requireText(traineeUsername, "traineeUsername");
        ValidationUtils.requireText(trainerUsername, "trainerUsername");
        ValidationUtils.requireText(trainingName, "trainingName");

        Trainee trainee = traineeDao.findByUsername(traineeUsername).orElseThrow();
        Trainer trainer = trainerDao.findByUsername(trainerUsername)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + trainerUsername));

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName(trainingName);
        training.setTrainingType(trainer.getSpecializationType());
        training.setTrainingDate(trainingDate);
        training.setTrainingDurationMinutes(trainingDurationMinutes);

        return create(training);
    }

    @Transactional(readOnly = true)
    public Optional<Training> select(Long id) {
        return trainingDao.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(String traineeUsername,
                                              String password,
                                              LocalDate fromDate,
                                              LocalDate toDate,
                                              String trainerName,
                                              String trainingType) {
        if (!authenticateTrainee(traineeUsername, password)) {
            throw new SecurityException("Invalid trainee credentials");
        }
        return getTraineeTrainingsAuthenticated(traineeUsername, fromDate, toDate, trainerName, trainingType);
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainingsAuthenticated(String traineeUsername,
                                                           LocalDate fromDate,
                                                           LocalDate toDate,
                                                           String trainerName,
                                                           String trainingType) {
        return trainingDao.findByTraineeCriteria(traineeUsername, fromDate, toDate, trainerName, trainingType);
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(String trainerUsername,
                                              String password,
                                              LocalDate fromDate,
                                              LocalDate toDate,
                                              String traineeName) {
        if (!authenticateTrainer(trainerUsername, password)) {
            throw new SecurityException("Invalid trainer credentials");
        }
        return getTrainerTrainingsAuthenticated(trainerUsername, fromDate, toDate, traineeName);
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainingsAuthenticated(String trainerUsername,
                                                           LocalDate fromDate,
                                                           LocalDate toDate,
                                                           String traineeName) {
        return trainingDao.findByTrainerCriteria(trainerUsername, fromDate, toDate, traineeName);
    }

    private void validateTraining(Training training) {
        ValidationUtils.requireNonNull(training, "training");
        ValidationUtils.requireText(training.getTrainingName(), "trainingName");
        ValidationUtils.requireNonNull(training.getTrainingDate(), "trainingDate");
        ValidationUtils.requirePositive(training.getTrainingDurationMinutes(), "trainingDuration");
        ValidationUtils.requireNonNull(training.getTrainingType(), "trainingType");
        ValidationUtils.requireNonNull(training.getTraineeId(), "traineeId");
        ValidationUtils.requireNonNull(training.getTrainerId(), "trainerId");
    }

    private void resolveReferences(Training training) {
        Trainee trainee = traineeDao.findById(training.getTraineeId())
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + training.getTraineeId()));
        Trainer trainer = trainerDao.findById(training.getTrainerId())
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + training.getTrainerId()));
        TrainingType trainingType = resolveTrainingType(training.getTrainingType());

        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(trainingType);
        trainee.getTrainers().add(trainer);
    }

    private TrainingType resolveTrainingType(TrainingType trainingType) {
        if (trainingType.getId() != null) {
            return trainingTypeDao.findById(trainingType.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Unknown training type id: " + trainingType.getId()));
        }
        return trainingTypeDao.findByName(trainingType.getTrainingTypeName())
                .orElseThrow(() -> new IllegalArgumentException("Unknown training type: " + trainingType.getTrainingTypeName()));
    }

    private boolean authenticateTrainee(String username, String password) {
        return traineeDao.findByUsername(username)
                .map(Trainee::getPassword)
                .filter(encodedPassword -> password != null && passwordEncoder.matches(password, encodedPassword))
                .isPresent();
    }

    private boolean authenticateTrainer(String username, String password) {
        return trainerDao.findByUsername(username)
                .map(Trainer::getPassword)
                .filter(encodedPassword -> password != null && passwordEncoder.matches(password, encodedPassword))
                .isPresent();
    }
}
