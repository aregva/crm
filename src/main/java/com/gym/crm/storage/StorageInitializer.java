package com.gym.crm.storage;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.dao.TrainingTypeDao;
import com.gym.crm.domain.Trainee;
import com.gym.crm.domain.Trainer;
import com.gym.crm.domain.TrainingType;
import com.gym.crm.util.PasswordGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class StorageInitializer implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(StorageInitializer.class);

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final TrainingTypeDao trainingTypeDao;
    private final TraineeDataParser traineeDataParser;
    private final TrainerDataParser trainerDataParser;
    private final PasswordGenerator passwordGenerator;
    private final PasswordEncoder passwordEncoder;
    private final Resource traineesResource;
    private final Resource trainersResource;
    private final TransactionTemplate transactionTemplate;

    public StorageInitializer(TraineeDao traineeDao,
                              TrainerDao trainerDao,
                              TrainingTypeDao trainingTypeDao,
                              TraineeDataParser traineeDataParser,
                              TrainerDataParser trainerDataParser,
                              PasswordGenerator passwordGenerator,
                              PasswordEncoder passwordEncoder,
                              @Value("${storage.init.trainees-file}") Resource traineesResource,
                              @Value("${storage.init.trainers-file}") Resource trainersResource,
                              PlatformTransactionManager transactionManager) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.trainingTypeDao = trainingTypeDao;
        this.traineeDataParser = traineeDataParser;
        this.trainerDataParser = trainerDataParser;
        this.passwordGenerator = passwordGenerator;
        this.passwordEncoder = passwordEncoder;
        this.traineesResource = traineesResource;
        this.trainersResource = trainersResource;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void afterPropertiesSet() {
        transactionTemplate.executeWithoutResult(status -> initialize());
    }

    private void initialize() {
        log.info("Starting storage initialization from resources: {}, {}", traineesResource, trainersResource);

        seedTrainingTypes();

        List<Trainee> trainees = traineeDataParser.parse(traineesResource);
        List<Trainer> trainers = trainerDataParser.parse(trainersResource);
        validateUniqueUsernames(trainees, trainers);
        validateUniqueTraineeIds(trainees);
        validateUniqueTrainerIds(trainers);

        trainers.forEach(this::resolveSpecialization);
        trainees.forEach(this::ensurePassword);
        trainers.forEach(this::ensurePassword);
        trainees.forEach(traineeDao::save);
        trainers.forEach(trainerDao::save);

        log.info("Storage initialization completed. traineesLoaded={}, trainersLoaded={}",
                trainees.size(), trainers.size());
    }

    private void seedTrainingTypes() {
        trainingTypeDao.save(TrainingType.FITNESS);
        trainingTypeDao.save(TrainingType.YOGA);
        trainingTypeDao.save(TrainingType.CARDIO);
        trainingTypeDao.save(TrainingType.CROSSFIT);
        trainingTypeDao.save(TrainingType.STRENGTH);
    }

    private void resolveSpecialization(Trainer trainer) {
        String specialization = trainer.getSpecialization();
        TrainingType trainingType = trainingTypeDao.findByName(specialization)
                .orElseThrow(() -> new IllegalStateException("Unknown trainer specialization: " + specialization));
        trainer.setSpecializationType(trainingType);
    }

    private void ensurePassword(Trainee trainee) {
        String password = trainee.getPassword();
        if (password == null || password.isBlank()) {
            password = passwordGenerator.generate(10);
        }
        trainee.setGeneratedPassword(password);
        trainee.setPassword(isBcryptHash(password) ? password : passwordEncoder.encode(password));
    }

    private void ensurePassword(Trainer trainer) {
        String password = trainer.getPassword();
        if (password == null || password.isBlank()) {
            password = passwordGenerator.generate(10);
        }
        trainer.setGeneratedPassword(password);
        trainer.setPassword(isBcryptHash(password) ? password : passwordEncoder.encode(password));
    }

    private boolean isBcryptHash(String password) {
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }

    private void validateUniqueUsernames(List<Trainee> trainees, List<Trainer> trainers) {
        Set<String> usernames = new HashSet<>();

        trainees.stream()
                .map(Trainee::getUsername)
                .forEach(username -> addUsername(usernames, username));
        trainers.stream()
                .map(Trainer::getUsername)
                .forEach(username -> addUsername(usernames, username));
    }

    private void addUsername(Set<String> usernames, String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("Username is required in storage initialization files");
        }
        if (!usernames.add(username)) {
            throw new IllegalStateException("Duplicate username in storage initialization files: " + username);
        }
    }

    private void validateUniqueTraineeIds(List<Trainee> trainees) {
        Set<Long> ids = new HashSet<>();
        for (Trainee trainee : trainees) {
            addId(ids, trainee.getId(), "trainee");
        }
    }

    private void validateUniqueTrainerIds(List<Trainer> trainers) {
        Set<Long> ids = new HashSet<>();
        for (Trainer trainer : trainers) {
            addId(ids, trainer.getId(), "trainer");
        }
    }

    private void addId(Set<Long> ids, Long id, String entityName) {
        if (id == null) {
            throw new IllegalStateException("Id is required in " + entityName + " storage initialization files");
        }
        if (!ids.add(id)) {
            throw new IllegalStateException("Duplicate " + entityName + " id in storage initialization files: " + id);
        }
    }
}
