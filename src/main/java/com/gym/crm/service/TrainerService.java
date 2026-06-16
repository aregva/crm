package com.gym.crm.service;

import com.gym.crm.actuator.metrics.GymMetrics;
import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.dao.TrainingTypeDao;
import com.gym.crm.domain.Trainer;
import com.gym.crm.domain.TrainingType;
import com.gym.crm.util.PasswordGenerator;
import com.gym.crm.util.UsernameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TrainerService {
    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private final TrainerDao trainerDao;
    private final TraineeDao traineeDao;
    private final TrainingTypeDao trainingTypeDao;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final GymMetrics metrics;

    public TrainerService(TrainerDao trainerDao,
                          TraineeDao traineeDao,
                          TrainingTypeDao trainingTypeDao,
                          UsernameGenerator usernameGenerator,
                          PasswordGenerator passwordGenerator,
                          GymMetrics metrics) {
        this.trainerDao = trainerDao;
        this.traineeDao = traineeDao;
        this.trainingTypeDao = trainingTypeDao;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
        this.metrics = metrics;
    }

    @Transactional
    public Trainer create(Trainer trainer) {
        validateRequiredProfileFields(trainer);
        trainer.setSpecializationType(resolveTrainingType(trainer.getSpecialization()));

        String base = usernameGenerator.generateBase(trainer.getFirstName(), trainer.getLastName());
        String uniqueUsername = usernameGenerator.makeUnique(base, this::usernameExists);
        String generatedPassword = passwordGenerator.generate(10);

        if (trainer.getUser() == null) {
            trainer.setUser(new com.gym.crm.domain.User());
        }
        trainer.getUser().setFirstName(trainer.getFirstName());
        trainer.getUser().setLastName(trainer.getLastName());
        trainer.getUser().setUsername(uniqueUsername);
        trainer.getUser().setPassword(generatedPassword);

        trainer.setUsername(uniqueUsername);
        trainer.setPassword(generatedPassword);

        Trainer saved = trainerDao.save(trainer);
        metrics.incrementTrainerCreated();
        log.info("Created trainer id={}, username={}", saved.getId(), saved.getUsername());
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<Trainer> select(Long id) {
        return trainerDao.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Trainer> selectByUsername(String username) {
        return trainerDao.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<Trainer> selectByUsername(String username, String password) {
        authenticateOrThrow(username, password);
        return trainerDao.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean authenticate(String username, String password) {
        boolean result = trainerDao.passwordMatches(username, password);
        if (result){metrics.incrementLoginSuccess();}
        else {metrics.incrementLoginFailed();}
        return result;
    }

    @Transactional
    public Optional<Trainer> update(Long id, Trainer update) {
        Optional<Trainer> opt = trainerDao.findById(id);
        if (opt.isEmpty()) return Optional.empty();
        return Optional.of(applyUpdate(opt.get(), update));
    }

    @Transactional
    public Optional<Trainer> update(String username, String password, Trainer update) {
        authenticateOrThrow(username, password);
        return trainerDao.findByUsername(username)
                .map(trainer -> applyUpdate(trainer, update));
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        authenticateOrThrow(username, oldPassword);
        ValidationUtils.requireText(newPassword, "newPassword");

        Trainer trainer = trainerDao.findByUsername(username).orElseThrow();
        trainer.setPassword(newPassword);
        log.info("Changed trainer password username={}", username);
    }

    @Transactional
    public void activate(String username, String password) {
        changeActive(username, password, true);
    }

    @Transactional
    public void deactivate(String username, String password) {
        changeActive(username, password, false);
    }

    private Trainer applyUpdate(Trainer trainer, Trainer update) {
        validateRequiredProfileFields(update);

        trainer.setFirstName(update.getFirstName());
        trainer.setLastName(update.getLastName());
        trainer.setSpecializationType(resolveTrainingType(update.getSpecialization()));
        trainer.setActive(update.isActive());

        if (trainer.getUser() != null) {
            trainer.getUser().setFirstName(update.getFirstName());
            trainer.getUser().setLastName(update.getLastName());
            trainer.getUser().setActive(update.isActive());
        }

        log.info("Updated trainer id={}", trainer.getId());
        return trainer;
    }

    private void changeActive(String username, String password, boolean active) {
        authenticateOrThrow(username, password);
        Trainer trainer = trainerDao.findByUsername(username).orElseThrow();
        if (trainer.isActive() == active) {
            throw new IllegalStateException("Trainer active state is already " + active);
        }
        trainer.setActive(active);
        log.info("Changed trainer active state username={}, active={}", username, active);
    }

    private void validateRequiredProfileFields(Trainer trainer) {
        ValidationUtils.requireNonNull(trainer, "trainer");
        ValidationUtils.requireText(trainer.getFirstName(), "firstName");
        ValidationUtils.requireText(trainer.getLastName(), "lastName");
        ValidationUtils.requireText(trainer.getSpecialization(), "specialization");
    }

    private TrainingType resolveTrainingType(String specialization) {
        return trainingTypeDao.findByName(specialization)
                .orElseThrow(() -> new IllegalArgumentException("Unknown training type: " + specialization));
    }

    private void authenticateOrThrow(String username, String password) {
        if (!authenticate(username, password)) {
            throw new SecurityException("Invalid trainer credentials");
        }
    }

    private boolean usernameExists(String username) {
        return trainerDao.existsByUsername(username) || traineeDao.existsByUsername(username);
    }
}
