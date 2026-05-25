package com.gym.crm.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trainee")
public class Trainee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, optional = false, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user = new User();

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "address")
    private String address;

    @ManyToMany
    @JoinTable(
            name = "trainee_trainer",
            joinColumns = @JoinColumn(name = "trainee_id"),
            inverseJoinColumns = @JoinColumn(name = "trainer_id")
    )
    private Set<Trainer> trainers = new HashSet<>();

    @OneToMany(mappedBy = "trainee", cascade = CascadeType.REMOVE, orphanRemoval = true)
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

    public boolean isActive() { return user.isActive(); }
    public void setActive(boolean active) { user.setActive(active); }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Set<Trainer> getTrainers() { return trainers; }
    public void setTrainers(Set<Trainer> trainers) { this.trainers = trainers; }

    public Set<Training> getTrainings() { return trainings; }
    public void setTrainings(Set<Training> trainings) { this.trainings = trainings; }
}
