package com.gym.crm.service;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.domain.Trainer;
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
public class TrainerService {
    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private final AtomicLong seq = new AtomicLong(1);

    private TrainerDao trainerDao;
    private TraineeDao traineeDao;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    @Autowired public void setTrainerDao(TrainerDao trainerDao) { this.trainerDao = trainerDao; }
    @Autowired public void setTraineeDao(TraineeDao traineeDao) { this.traineeDao = traineeDao; }
    @Autowired public void setUsernameGenerator(UsernameGenerator usernameGenerator) { this.usernameGenerator = usernameGenerator; }
    @Autowired public void setPasswordGenerator(PasswordGenerator passwordGenerator) { this.passwordGenerator = passwordGenerator; }

    public Trainer create(Trainer trainer) {
        Set<String> existing = new HashSet<>();
        trainerDao.findAll().forEach(t -> { if (t.getUsername() != null) existing.add(t.getUsername()); });
        traineeDao.findAll().forEach(t -> { if (t.getUsername() != null) existing.add(t.getUsername()); });

        String base = usernameGenerator.generateBase(trainer.getFirstName(), trainer.getLastName());
        trainer.setUsername(usernameGenerator.makeUnique(base, existing));
        trainer.setPassword(passwordGenerator.generate(10));
        trainer.setId(seq.getAndIncrement());

        trainerDao.save(trainer);
        log.info("Created trainer id={}, username={}", trainer.getId(), trainer.getUsername());
        return trainer;
    }

    public Optional<Trainer> select(Long id) {
        return trainerDao.findById(id);
    }

    public Optional<Trainer> update(Long id, Trainer update) {
        Optional<Trainer> opt = trainerDao.findById(id);
        if (opt.isEmpty()) return Optional.empty();

        Trainer t = opt.get();
        t.setFirstName(update.getFirstName());
        t.setLastName(update.getLastName());
        t.setSpecialization(update.getSpecialization());
        t.setActive(update.isActive());
        trainerDao.save(t);

        log.info("Updated trainer id={}", id);
        return Optional.of(t);
    }
}