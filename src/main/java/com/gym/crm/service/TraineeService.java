package com.gym.crm.service;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.domain.Trainee;
import com.gym.crm.domain.Trainer;
import com.gym.crm.util.PasswordGenerator;
import com.gym.crm.util.UsernameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TraineeService {
    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;

    public TraineeService(TraineeDao traineeDao,
                          TrainerDao trainerDao,
                          UsernameGenerator usernameGenerator,
                          PasswordGenerator passwordGenerator) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
    }

    @Transactional
    public Trainee create(Trainee trainee) {
        // Ensure validations are run
        ValidationUtils.requireNonNull(trainee, "trainee");
        ValidationUtils.requireText(trainee.getFirstName(), "firstName");
        ValidationUtils.requireText(trainee.getLastName(), "lastName");

        String base = usernameGenerator.generateBase(trainee.getFirstName(), trainee.getLastName());
        String uniqueUsername = usernameGenerator.makeUnique(base, this::usernameExists);
        String generatedPassword = passwordGenerator.generate(10);

        // FIX: Populate top-level fields
        trainee.setUsername(uniqueUsername);
        trainee.setPassword(generatedPassword);

        // FIX: Synchronize underlying Hibernate User relational mapping object
        if (trainee.getUser() == null) {
            trainee.setUser(new com.gym.crm.domain.User());
        }
        trainee.getUser().setFirstName(trainee.getFirstName());
        trainee.getUser().setLastName(trainee.getLastName());
        trainee.getUser().setUsername(uniqueUsername);
        trainee.getUser().setPassword(generatedPassword);
        trainee.getUser().setActive(true);

        Trainee saved = traineeDao.save(trainee);
        log.info("Created trainee id={}, username={}", saved.getId(), saved.getUsername());
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<Trainee> select(Long id) {
        return traineeDao.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Trainee> selectByUsername(String username) {
        return traineeDao.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<Trainee> selectByUsername(String username, String password) {
        authenticateOrThrow(username, password);
        return traineeDao.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean authenticate(String username, String password) {
        return traineeDao.passwordMatches(username, password);
    }

    @Transactional
    public Optional<Trainee> update(Long id, Trainee update) {
        Optional<Trainee> opt = traineeDao.findById(id);
        if (opt.isEmpty()) return Optional.empty();
        return Optional.of(applyUpdate(opt.get(), update));
    }

    @Transactional
    public Optional<Trainee> update(String username, String password, Trainee update) {
        authenticateOrThrow(username, password);
        return traineeDao.findByUsername(username)
                .map(trainee -> applyUpdate(trainee, update));
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        authenticateOrThrow(username, oldPassword);
        ValidationUtils.requireText(newPassword, "newPassword");

        Trainee trainee = traineeDao.findByUsername(username).orElseThrow();
        trainee.setPassword(newPassword);
        log.info("Changed trainee password username={}", username);
    }

    @Transactional
    public void activate(String username, String password) {
        changeActive(username, password, true);
    }

    @Transactional
    public void deactivate(String username, String password) {
        changeActive(username, password, false);
    }

    @Transactional
    public boolean delete(Long id) {
        Optional<Trainee> trainee = traineeDao.findById(id);
        if (trainee.isEmpty()) return false;
        traineeDao.delete(trainee.get());
        log.info("Deleted trainee id={}", id);
        return true;
    }

    @Transactional
    public void deleteByUsername(String username, String password) {
        authenticateOrThrow(username, password);
        Trainee trainee = traineeDao.findByUsername(username).orElseThrow();
        traineeDao.delete(trainee);
        log.info("Deleted trainee username={}", username);
    }

    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(String traineeUsername, String password) {
        authenticateOrThrow(traineeUsername, password);
        return traineeDao.findUnassignedTrainers(traineeUsername);
    }

    @Transactional
    public Trainee updateTrainers(String traineeUsername, String password, List<String> trainerUsernames) {
        authenticateOrThrow(traineeUsername, password);
        Trainee trainee = traineeDao.findByUsername(traineeUsername).orElseThrow();
        Set<Trainer> trainers = new HashSet<>();
        for (String trainerUsername : trainerUsernames) {
            trainers.add(trainerDao.findByUsername(trainerUsername)
                    .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + trainerUsername)));
        }
        trainee.setTrainers(trainers);
        trainee.getTrainers().size();
        log.info("Updated trainee trainers username={}, trainersCount={}", traineeUsername, trainers.size());
        return trainee;
    }

    private Trainee applyUpdate(Trainee trainee, Trainee update) {
        trainee.setFirstName(update.getFirstName());
        trainee.setLastName(update.getLastName());
        trainee.setDateOfBirth(update.getDateOfBirth());
        trainee.setAddress(update.getAddress());
        trainee.setActive(update.isActive());

        if (trainee.getUser() != null) {
            trainee.getUser().setFirstName(update.getFirstName());
            trainee.getUser().setLastName(update.getLastName());
            trainee.getUser().setActive(update.isActive());
        }

        log.info("Updated trainee id={}", trainee.getId());
        return trainee;
    }

    private void changeActive(String username, String password, boolean active) {
        authenticateOrThrow(username, password);
        Trainee trainee = traineeDao.findByUsername(username).orElseThrow();
        if (trainee.isActive() == active) {
            throw new IllegalStateException("Trainee active state is already " + active);
        }
        trainee.setActive(active);
        log.info("Changed trainee active state username={}, active={}", username, active);
    }

    private void validateRequiredProfileFields(Trainee trainee) {
        ValidationUtils.requireNonNull(trainee, "trainee");
        ValidationUtils.requireText(trainee.getFirstName(), "firstName");
        ValidationUtils.requireText(trainee.getLastName(), "lastName");
    }

    private void authenticateOrThrow(String username, String password) {
        if (!authenticate(username, password)) {
            throw new SecurityException("Invalid trainee credentials");
        }
    }

    private boolean usernameExists(String username) {
        return traineeDao.existsByUsername(username) || trainerDao.existsByUsername(username);
    }
}
