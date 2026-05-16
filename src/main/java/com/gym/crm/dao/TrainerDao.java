package com.gym.crm.dao;

import com.gym.crm.domain.Trainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TrainerDao {

    private Map<Long, Trainer> trainerStorage;

    @Autowired
    public void setTrainerStorage(@Qualifier("trainerStorage") Map<Long, Trainer> trainerStorage) {
        this.trainerStorage = trainerStorage;
    }

    public void save(Trainer trainer) { trainerStorage.put(trainer.getId(), trainer); }

    public Optional<Trainer> findById(Long id) { return Optional.ofNullable(trainerStorage.get(id)); }

    public List<Trainer> findAll() { return new ArrayList<>(trainerStorage.values()); }

    public boolean existsByUsername(String username) {
        return trainerStorage.values().stream()
                .anyMatch(trainer -> username.equals(trainer.getUsername()));
    }
}
