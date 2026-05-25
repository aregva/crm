package com.gym.crm;

import com.gym.crm.config.AppConfig;
import com.gym.crm.domain.Trainee;
import com.gym.crm.domain.Trainer;
import com.gym.crm.domain.Training;
import com.gym.crm.domain.TrainingType;
import com.gym.crm.facade.GymFacade;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.List;

public class CrmApplication {
    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            GymFacade facade = ctx.getBean(GymFacade.class);

            Trainee trainee = createTrainee(facade);
            Trainer trainer = createTrainer(facade);

            printProfile("Created trainee", trainee.getUsername(), trainee.getPassword());
            printProfile("Created trainer", trainer.getUsername(), trainer.getPassword());

            boolean traineeAuthenticated = facade.authenticateTrainee(trainee.getUsername(), trainee.getPassword());
            boolean trainerAuthenticated = facade.authenticateTrainer(trainer.getUsername(), trainer.getPassword());
            System.out.println("Trainee authenticated: " + traineeAuthenticated);
            System.out.println("Trainer authenticated: " + trainerAuthenticated);

            facade.updateTraineeTrainers(
                    trainee.getUsername(),
                    trainee.getPassword(),
                    List.of(trainer.getUsername())
            );
            System.out.println("Assigned trainer " + trainer.getUsername() + " to trainee " + trainee.getUsername());

            Training training = new Training();
            training.setTrainerId(trainer.getId());
            training.setTrainingName("Cardio Fundamentals");
            training.setTrainingType(TrainingType.CARDIO);
            training.setTrainingDate(LocalDate.now());
            training.setTrainingDurationMinutes(45);
            Training createdTraining = facade.addTraining(trainee.getUsername(), trainee.getPassword(), training);
            System.out.println("Added training id=" + createdTraining.getId()
                    + ", name=" + createdTraining.getTrainingName());

            List<Training> traineeTrainings = facade.getTraineeTrainings(
                    trainee.getUsername(),
                    trainee.getPassword(),
                    LocalDate.now().minusDays(1),
                    LocalDate.now().plusDays(1),
                    trainer.getFirstName() + " " + trainer.getLastName(),
                    "CARDIO"
            );
            System.out.println("Trainee trainings found by criteria: " + traineeTrainings.size());

            List<Training> trainerTrainings = facade.getTrainerTrainings(
                    trainer.getUsername(),
                    trainer.getPassword(),
                    LocalDate.now().minusDays(1),
                    LocalDate.now().plusDays(1),
                    trainee.getFirstName() + " " + trainee.getLastName()
            );
            System.out.println("Trainer trainings found by criteria: " + trainerTrainings.size());

            facade.deactivateTrainee(trainee.getUsername(), trainee.getPassword());
            boolean active = facade.getTraineeByUsername(trainee.getUsername(), trainee.getPassword())
                    .orElseThrow()
                    .isActive();
            System.out.println("Trainee active after deactivation: " + active);
        }
    }

    private static Trainee createTrainee(GymFacade facade) {
        Trainee trainee = new Trainee();
        trainee.setFirstName("John");
        trainee.setLastName("Smith");
        trainee.setDateOfBirth(LocalDate.of(1998, 3, 15));
        trainee.setAddress("New York");
        trainee.setActive(true);
        return facade.createTrainee(trainee);
    }

    private static Trainer createTrainer(GymFacade facade) {
        Trainer trainer = new Trainer();
        trainer.setFirstName("Kate");
        trainer.setLastName("Brown");
        trainer.setSpecialization("CARDIO");
        trainer.setActive(true);
        return facade.createTrainer(trainer);
    }

    private static void printProfile(String label, String username, String password) {
        System.out.println(label + ": username=" + username + ", password=" + password);
    }
}
