package com.gym.crm.dao;

import com.gym.crm.domain.Trainee;
import com.gym.crm.domain.Trainer;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class TraineeDao {
    private final SessionFactory sessionFactory;

    public TraineeDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional
    public Trainee save(Trainee trainee) {
        return sessionFactory.getCurrentSession().merge(trainee);
    }

    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(sessionFactory.getCurrentSession().get(Trainee.class, id));
    }

    public Optional<Trainee> findByUsername(String username) {
        return sessionFactory.getCurrentSession()
                .createQuery("""
                        select t from Trainee t
                        join fetch t.user u
                        where u.username = :username
                        """, Trainee.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    public List<Trainee> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("select t from Trainee t join fetch t.user", Trainee.class)
                .getResultList();
    }

    public boolean existsByUsername(String username) {
        return sessionFactory.getCurrentSession()
                .createQuery("""
                        select count(t.id) from Trainee t
                        where t.user.username = :username
                        """, Long.class)
                .setParameter("username", username)
                .uniqueResult() > 0;
    }

    @Transactional
    public void delete(Trainee trainee) {
        sessionFactory.getCurrentSession().remove(trainee);
    }

    public List<Trainer> findUnassignedTrainers(String traineeUsername) {
        return sessionFactory.getCurrentSession()
                .createQuery("""
                        select tr from Trainer tr
                        join fetch tr.user
                        where tr.id not in (
                            select assigned.id from Trainee te
                            join te.trainers assigned
                            where te.user.username = :username
                        )
                        order by tr.user.firstName, tr.user.lastName
                        """, Trainer.class)
                .setParameter("username", traineeUsername)
                .getResultList();
    }
}
