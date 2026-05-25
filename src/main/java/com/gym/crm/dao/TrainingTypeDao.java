package com.gym.crm.dao;

import com.gym.crm.domain.TrainingType;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class TrainingTypeDao {
    private final SessionFactory sessionFactory;

    public TrainingTypeDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional
    public void save(TrainingType trainingType) {
        sessionFactory.getCurrentSession().merge(trainingType);
    }

    public Optional<TrainingType> findById(Long id) {
        return Optional.ofNullable(sessionFactory.getCurrentSession().get(TrainingType.class, id));
    }

    public Optional<TrainingType> findByName(String name) {
        if (name == null) return Optional.empty();
        return sessionFactory.getCurrentSession()
                .createQuery("""
                        select tt from TrainingType tt
                        where upper(tt.trainingTypeName) = :name
                        """, TrainingType.class)
                .setParameter("name", name.toUpperCase(Locale.ROOT))
                .uniqueResultOptional();
    }

    public List<TrainingType> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("select tt from TrainingType tt order by tt.id", TrainingType.class)
                .getResultList();
    }
}
