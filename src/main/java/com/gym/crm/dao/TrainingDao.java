package com.gym.crm.dao;

import com.gym.crm.domain.Training;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class TrainingDao {
    private final SessionFactory sessionFactory;

    public TrainingDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional
    public Training save(Training training) {
        return sessionFactory.getCurrentSession().merge(training);
    }

    @Transactional
    public void delete(Training training) {
        var session = sessionFactory.getCurrentSession();
        session.remove(session.contains(training) ? training : session.merge(training));
    }

    public Optional<Training> findById(Long id) {
        return Optional.ofNullable(sessionFactory.getCurrentSession().get(Training.class, id));
    }

    public List<Training> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("select tr from Training tr", Training.class)
                .getResultList();
    }

    public List<Training> findByTraineeCriteria(String traineeUsername,
                                                LocalDate fromDate,
                                                LocalDate toDate,
                                                String trainerName,
                                                String trainingType) {
        StringBuilder hql = new StringBuilder("""
                select tr from Training tr
                join fetch tr.trainee trainee
                join fetch trainee.user traineeUser
                join fetch tr.trainer trainer
                join fetch trainer.user trainerUser
                join fetch tr.trainingType type
                where traineeUser.username = :traineeUsername
                """);
        if (fromDate != null) hql.append(" and tr.trainingDate >= :fromDate");
        if (toDate != null) hql.append(" and tr.trainingDate <= :toDate");
        if (trainerName != null && !trainerName.isBlank()) {
            hql.append(" and concat(trainerUser.firstName, ' ', trainerUser.lastName) = :trainerName");
        }
        if (trainingType != null && !trainingType.isBlank()) {
            hql.append(" and type.trainingTypeName = :trainingType");
        }
        hql.append(" order by tr.trainingDate");

        var query = sessionFactory.getCurrentSession()
                .createQuery(hql.toString(), Training.class)
                .setParameter("traineeUsername", traineeUsername);
        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);
        if (trainerName != null && !trainerName.isBlank()) query.setParameter("trainerName", trainerName);
        if (trainingType != null && !trainingType.isBlank()) query.setParameter("trainingType", trainingType);
        return query.getResultList();
    }

    public List<Training> findByTrainerCriteria(String trainerUsername,
                                                LocalDate fromDate,
                                                LocalDate toDate,
                                                String traineeName) {
        StringBuilder hql = new StringBuilder("""
                select tr from Training tr
                join fetch tr.trainee trainee
                join fetch trainee.user traineeUser
                join fetch tr.trainer trainer
                join fetch trainer.user trainerUser
                join fetch tr.trainingType type
                where trainerUser.username = :trainerUsername
                """);
        if (fromDate != null) hql.append(" and tr.trainingDate >= :fromDate");
        if (toDate != null) hql.append(" and tr.trainingDate <= :toDate");
        if (traineeName != null && !traineeName.isBlank()) {
            hql.append(" and concat(traineeUser.firstName, ' ', traineeUser.lastName) = :traineeName");
        }
        hql.append(" order by tr.trainingDate");

        var query = sessionFactory.getCurrentSession()
                .createQuery(hql.toString(), Training.class)
                .setParameter("trainerUsername", trainerUsername);
        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);
        if (traineeName != null && !traineeName.isBlank()) query.setParameter("traineeName", traineeName);
        return query.getResultList();
    }
}
