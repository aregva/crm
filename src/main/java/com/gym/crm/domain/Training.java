package com.gym.crm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "training")
public class Training {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "trainee_id", nullable = false)
    private Trainee trainee;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @Column(name = "training_name", nullable = false)
    private String trainingName;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "training_type_id", nullable = false)
    private TrainingType trainingType;

    @Column(name = "training_date", nullable = false)
    private LocalDate trainingDate;

    @Column(name = "training_duration", nullable = false)
    private int trainingDurationMinutes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Trainee getTrainee() { return trainee; }
    public void setTrainee(Trainee trainee) { this.trainee = trainee; }

    public Trainer getTrainer() { return trainer; }
    public void setTrainer(Trainer trainer) { this.trainer = trainer; }

    public Long getTraineeId() { return trainee == null ? null : trainee.getId(); }
    public void setTraineeId(Long traineeId) {
        if (traineeId == null) {
            this.trainee = null;
            return;
        }
        Trainee traineeRef = new Trainee();
        traineeRef.setId(traineeId);
        this.trainee = traineeRef;
    }

    public Long getTrainerId() { return trainer == null ? null : trainer.getId(); }
    public void setTrainerId(Long trainerId) {
        if (trainerId == null) {
            this.trainer = null;
            return;
        }
        Trainer trainerRef = new Trainer();
        trainerRef.setId(trainerId);
        this.trainer = trainerRef;
    }

    public String getTrainingName() { return trainingName; }
    public void setTrainingName(String trainingName) { this.trainingName = trainingName; }

    public TrainingType getTrainingType() { return trainingType; }
    public void setTrainingType(TrainingType trainingType) { this.trainingType = trainingType; }

    public LocalDate getTrainingDate() { return trainingDate; }
    public void setTrainingDate(LocalDate trainingDate) { this.trainingDate = trainingDate; }

    public int getTrainingDurationMinutes() { return trainingDurationMinutes; }
    public void setTrainingDurationMinutes(int trainingDurationMinutes) { this.trainingDurationMinutes = trainingDurationMinutes; }
}
