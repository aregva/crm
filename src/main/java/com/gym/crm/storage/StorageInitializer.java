package com.gym.crm.storage;

import com.gym.crm.domain.Trainee;
import com.gym.crm.domain.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class StorageInitializer implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(StorageInitializer.class);

    private final Map<Long, Trainee> traineeStorage;
    private final Map<Long, Trainer> trainerStorage;
    private final TraineeDataParser traineeDataParser;
    private final TrainerDataParser trainerDataParser;
    private final Resource traineesResource;
    private final Resource trainersResource;

    public StorageInitializer(@Qualifier("traineeStorage") Map<Long, Trainee> traineeStorage,
                              @Qualifier("trainerStorage") Map<Long, Trainer> trainerStorage,
                              TraineeDataParser traineeDataParser,
                              TrainerDataParser trainerDataParser,
                              @Value("${storage.init.trainees-file}") Resource traineesResource,
                              @Value("${storage.init.trainers-file}") Resource trainersResource) {
        this.traineeStorage = traineeStorage;
        this.trainerStorage = trainerStorage;
        this.traineeDataParser = traineeDataParser;
        this.trainerDataParser = trainerDataParser;
        this.traineesResource = traineesResource;
        this.trainersResource = trainersResource;
    }

    @Override
    public void afterPropertiesSet() {
        log.info("Starting storage initialization from resources: {}, {}", traineesResource, trainersResource);

        List<Trainee> trainees = traineeDataParser.parse(traineesResource);
        List<Trainer> trainers = trainerDataParser.parse(trainersResource);
        validateUniqueUsernames(trainees, trainers);
        validateUniqueTraineeIds(trainees);
        validateUniqueTrainerIds(trainers);

        trainees.forEach(trainee -> traineeStorage.put(trainee.getId(), trainee));
        trainers.forEach(trainer -> trainerStorage.put(trainer.getId(), trainer));

        log.info("Storage initialization completed. traineesLoaded={}, trainersLoaded={}",
                trainees.size(), trainers.size());
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
