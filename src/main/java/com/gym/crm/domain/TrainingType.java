package com.gym.crm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "training_type")
public class TrainingType {
    public static final TrainingType FITNESS = new TrainingType(1L, "FITNESS");
    public static final TrainingType YOGA = new TrainingType(2L, "YOGA");
    public static final TrainingType CARDIO = new TrainingType(3L, "CARDIO");
    public static final TrainingType CROSSFIT = new TrainingType(4L, "CROSSFIT");
    public static final TrainingType STRENGTH = new TrainingType(5L, "STRENGTH");

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "training_type_name", nullable = false, unique = true, updatable = false)
    private String trainingTypeName;

    public TrainingType() {
    }

    public TrainingType(Long id, String trainingTypeName) {
        this.id = id;
        this.trainingTypeName = trainingTypeName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTrainingTypeName() { return trainingTypeName; }
    public void setTrainingTypeName(String trainingTypeName) { this.trainingTypeName = trainingTypeName; }

    @Override
    public String toString() {
        return trainingTypeName;
    }
}
