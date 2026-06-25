package com.gym.crm.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trainer")
public class Trainer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, optional = false, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user = new User();

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "specialization", nullable = false)
    private TrainingType specializationType;

    @ManyToMany(mappedBy = "trainers")
    private Set<Trainee> trainees = new HashSet<>();

    @OneToMany(mappedBy = "trainer")
    private Set<Training> trainings = new HashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getFirstName() { return user.getFirstName(); }
    public void setFirstName(String firstName) { user.setFirstName(firstName); }

    public String getLastName() { return user.getLastName(); }
    public void setLastName(String lastName) { user.setLastName(lastName); }

    public String getUsername() { return user.getUsername(); }
    public void setUsername(String username) { user.setUsername(username); }

    public String getPassword() { return user.getPassword(); }
    public void setPassword(String password) { user.setPassword(password); }

    public String getGeneratedPassword() { return user.getGeneratedPassword(); }
    public void setGeneratedPassword(String generatedPassword) { user.setGeneratedPassword(generatedPassword); }

    public boolean isActive() { return user.isActive(); }
    public void setActive(boolean active) { user.setActive(active); }

    public String getSpecialization() {
        return specializationType == null ? null : specializationType.getTrainingTypeName();
    }

    public void setSpecialization(String specialization) {
        this.specializationType = specialization == null ? null : new TrainingType(null, specialization);
    }

    public TrainingType getSpecializationType() { return specializationType; }
    public void setSpecializationType(TrainingType specializationType) { this.specializationType = specializationType; }

    public Set<Trainee> getTrainees() { return trainees; }
    public void setTrainees(Set<Trainee> trainees) { this.trainees = trainees; }

    public Set<Training> getTrainings() { return trainings; }
    public void setTrainings(Set<Training> trainings) { this.trainings = trainings; }
}
