package com.gym.crm.dao;

import com.gym.crm.domain.Trainee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TraineeDao {

    private Map<Long, Trainee> traineeStorage;

    @Autowired
    public void setTraineeStorage(@Qualifier("traineeStorage") Map<Long, Trainee> traineeStorage) {
        this.traineeStorage = traineeStorage;
    }

    public void save(Trainee trainee) { traineeStorage.put(trainee.getId(), trainee); }

    public Optional<Trainee> findById(Long id) { return Optional.ofNullable(traineeStorage.get(id)); }

    public List<Trainee> findAll() { return new ArrayList<>(traineeStorage.values()); }

    public boolean existsByUsername(String username) {
        return traineeStorage.values().stream()
                .anyMatch(trainee -> username.equals(trainee.getUsername()));
    }

    public void deleteById(Long id) { traineeStorage.remove(id); }
}
