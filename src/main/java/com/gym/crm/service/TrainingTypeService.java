package com.gym.crm.service;

import com.gym.crm.dao.TrainingTypeDao;
import com.gym.crm.domain.TrainingType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainingTypeService {
    private final TrainingTypeDao trainingTypeDao;

    public TrainingTypeService(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @Transactional(readOnly = true)
    public List<TrainingType> findAll() {
        return trainingTypeDao.findAll();
    }
}
