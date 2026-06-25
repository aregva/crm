package com.gym.crm.service;

import com.gym.crm.actuator.metrics.GymMetrics;
import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.domain.Trainee;
import com.gym.crm.domain.Trainer;
import com.gym.crm.util.PasswordGenerator;
import com.gym.crm.util.UsernameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final GymMetrics metrics;

    public TraineeService(TraineeDao traineeDao,
                          TrainerDao trainerDao,
                          UsernameGenerator usernameGenerator,
                          PasswordGenerator passwordGenerator,
                          PasswordEncoder passwordEncoder,
                          GymMetrics gymMetrics) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
        this.passwordEncoder = passwordEncoder;
        this.metrics = gymMetrics;
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
        String encodedPassword = passwordEncoder.encode(generatedPassword);

        trainee.setUsername(uniqueUsername);
        trainee.setPassword(encodedPassword);
        trainee.setGeneratedPassword(generatedPassword);

        if (trainee.getUser() == null) {
            trainee.setUser(new com.gym.crm.domain.User());
        }
        trainee.getUser().setFirstName(trainee.getFirstName());
        trainee.getUser().setLastName(trainee.getLastName());
        trainee.getUser().setUsername(uniqueUsername);
        trainee.getUser().setPassword(encodedPassword);
        trainee.getUser().setGeneratedPassword(generatedPassword);
        trainee.getUser().setActive(true);

        Trainee saved = traineeDao.save(trainee);
        saved.setGeneratedPassword(generatedPassword);
        metrics.incrementTraineeCreated();
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
        boolean result = traineeDao.findByUsername(username)
                .map(Trainee::getPassword)
                .filter(encodedPassword -> password != null && passwordEncoder.matches(password, encodedPassword))
                .isPresent();
        if (result){metrics.incrementLoginSuccess();}
        else {metrics.incrementLoginFailed();}
        return result;
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
        return updateAuthenticated(username, update);
    }

    @Transactional
    public Optional<Trainee> updateAuthenticated(String username, Trainee update) {
        return traineeDao.findByUsername(username)
                .map(trainee -> applyUpdate(trainee, update));
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        authenticateOrThrow(username, oldPassword);
        ValidationUtils.requireText(newPassword, "newPassword");

        Trainee trainee = traineeDao.findByUsername(username).orElseThrow();
        trainee.setPassword(passwordEncoder.encode(newPassword));
        log.info("Changed trainee password username={}", username);
    }

    @Transactional
    public void activate(String username, String password) {
        changeActive(username, password, true);
    }

    @Transactional
    public void activateAuthenticated(String username) {
        changeActiveAuthenticated(username, true);
    }

    @Transactional
    public void deactivate(String username, String password) {
        changeActive(username, password, false);
    }

    @Transactional
    public void deactivateAuthenticated(String username) {
        changeActiveAuthenticated(username, false);
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
        deleteByUsernameAuthenticated(username);
    }

    @Transactional
    public void deleteByUsernameAuthenticated(String username) {
        Trainee trainee = traineeDao.findByUsername(username).orElseThrow();
        traineeDao.delete(trainee);
        log.info("Deleted trainee username={}", username);
    }

    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(String traineeUsername, String password) {
        authenticateOrThrow(traineeUsername, password);
        return getUnassignedTrainersAuthenticated(traineeUsername);
    }

    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainersAuthenticated(String traineeUsername) {
        return traineeDao.findUnassignedTrainers(traineeUsername);
    }

    @Transactional
    public Trainee updateTrainers(String traineeUsername, String password, List<String> trainerUsernames) {
        authenticateOrThrow(traineeUsername, password);
        return updateTrainersAuthenticated(traineeUsername, trainerUsernames);
    }

    @Transactional
    public Trainee updateTrainersAuthenticated(String traineeUsername, List<String> trainerUsernames) {
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
        changeActiveAuthenticated(username, active);
    }

    private void changeActiveAuthenticated(String username, boolean active) {
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
