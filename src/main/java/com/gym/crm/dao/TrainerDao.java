package com.gym.crm.dao;

import com.gym.crm.domain.Trainer;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class TrainerDao {
    private final SessionFactory sessionFactory;

    public TrainerDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional
    public Trainer save(Trainer trainer) {
        return sessionFactory.getCurrentSession().merge(trainer);
    }

    public Optional<Trainer> findById(Long id) {
        return Optional.ofNullable(sessionFactory.getCurrentSession().get(Trainer.class, id));
    }

    public Optional<Trainer> findByUsername(String username) {
        return sessionFactory.getCurrentSession()
                .createQuery("""
                        select t from Trainer t
                        join fetch t.user u
                        join fetch t.specializationType
                        where u.username = :username
                        """, Trainer.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    public List<Trainer> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("select t from Trainer t join fetch t.user join fetch t.specializationType", Trainer.class)
                .getResultList();
    }

    public boolean existsByUsername(String username) {
        return sessionFactory.getCurrentSession()
                .createQuery("""
                        select count(t.id) from Trainer t
                        where t.user.username = :username
                        """, Long.class)
                .setParameter("username", username)
                .uniqueResult() > 0;
    }

}
