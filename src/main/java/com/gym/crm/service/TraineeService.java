package com.gym.crm.service;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.domain.Trainee;
import com.gym.crm.util.PasswordGenerator;
import com.gym.crm.util.UsernameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TraineeService {
    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    private final AtomicLong seq = new AtomicLong(1);

    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    @Autowired public void setTraineeDao(TraineeDao traineeDao) { this.traineeDao = traineeDao; }
    @Autowired public void setTrainerDao(TrainerDao trainerDao) { this.trainerDao = trainerDao; }
    @Autowired public void setUsernameGenerator(UsernameGenerator usernameGenerator) { this.usernameGenerator = usernameGenerator; }
    @Autowired public void setPasswordGenerator(PasswordGenerator passwordGenerator) { this.passwordGenerator = passwordGenerator; }

    public Trainee create(Trainee trainee) {
        Set<String> existing = collectAllUsernames();
        String base = usernameGenerator.generateBase(trainee.getFirstName(), trainee.getLastName());
        trainee.setUsername(usernameGenerator.makeUnique(base, existing));
        trainee.setPassword(passwordGenerator.generate(10));
        trainee.setId(seq.getAndIncrement());
        traineeDao.save(trainee);
        log.info("Created trainee id={}, username={}", trainee.getId(), trainee.getUsername());
        return trainee;
    }

    public Optional<Trainee> select(Long id) {
        return traineeDao.findById(id);
    }

    public Optional<Trainee> update(Long id, Trainee update) {
        Optional<Trainee> opt = traineeDao.findById(id);
        if (opt.isEmpty()) return Optional.empty();

        Trainee t = opt.get();
        t.setFirstName(update.getFirstName());
        t.setLastName(update.getLastName());
        t.setAddress(update.getAddress());
        t.setDateOfBirth(update.getDateOfBirth());
        t.setActive(update.isActive());
        traineeDao.save(t);

        log.info("Updated trainee id={}", id);
        return Optional.of(t);
    }

    public boolean delete(Long id) {
        if (traineeDao.findById(id).isEmpty()) return false;
        traineeDao.deleteById(id);
        log.info("Deleted trainee id={}", id);
        return true;
    }

    private Set<String> collectAllUsernames() {
        Set<String> set = new HashSet<>();
        traineeDao.findAll().forEach(t -> { if (t.getUsername() != null) set.add(t.getUsername()); });
        trainerDao.findAll().forEach(t -> { if (t.getUsername() != null) set.add(t.getUsername()); });
        return set;
    }
}