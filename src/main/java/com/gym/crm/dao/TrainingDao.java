package com.gym.crm.dao;

import com.gym.crm.domain.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TrainingDao {

    private Map<Long, Training> trainingStorage;

    @Autowired
    public void setTrainingStorage(@Qualifier("trainingStorage") Map<Long, Training> trainingStorage) {
        this.trainingStorage = trainingStorage;
    }

    public void save(Training training) { trainingStorage.put(training.getId(), training); }

    public Optional<Training> findById(Long id) { return Optional.ofNullable(trainingStorage.get(id)); }

    public List<Training> findAll() { return new ArrayList<>(trainingStorage.values()); }
}