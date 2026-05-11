package com.gym.crm.service;

import com.gym.crm.dao.TrainingDao;
import com.gym.crm.domain.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TrainingService {
    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private final AtomicLong seq = new AtomicLong(1);
    private TrainingDao trainingDao;

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) { this.trainingDao = trainingDao; }

    public Training create(Training training) {
        training.setId(seq.getAndIncrement());
        trainingDao.save(training);
        log.info("Created training id={}, name={}", training.getId(), training.getTrainingName());
        return training;
    }

    public Optional<Training> select(Long id) {
        return trainingDao.findById(id);
    }
}