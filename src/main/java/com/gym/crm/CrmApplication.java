package com.gym.crm;

import com.gym.crm.config.AppConfig;
import com.gym.crm.domain.Trainee;
import com.gym.crm.domain.Trainer;
import com.gym.crm.facade.GymFacade;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

public class CrmApplication {
    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            GymFacade facade = ctx.getBean(GymFacade.class);

            // 1) Create trainee with same name as preloaded trainee -> Alice.Green1 expected
            Trainee t1 = new Trainee();
            t1.setFirstName("Alice");
            t1.setLastName("Green");
            t1.setDateOfBirth(LocalDate.of(1995, 5, 10));
            t1.setAddress("Chicago");
            t1.setActive(true);
            facade.createTrainee(t1);

            // 2) Create trainer with same name as preloaded trainer -> Mike.Brown1 expected
            Trainer tr1 = new Trainer();
            tr1.setFirstName("Mike");
            tr1.setLastName("Brown");
            tr1.setSpecialization("Strength");
            tr1.setActive(true);
            facade.createTrainer(tr1);

            // 3) Cross-entity collision:
            // preloaded has trainee Alice.Green, and now trainee Alice.Green1 already exists,
            // so new trainer Alice Green should become Alice.Green2
            Trainer tr2 = new Trainer();
            tr2.setFirstName("Alice");
            tr2.setLastName("Green");
            tr2.setSpecialization("Yoga");
            tr2.setActive(true);
            facade.createTrainer(tr2);

            // 4) Normal new user without collision
            Trainee t2 = new Trainee();
            t2.setFirstName("John");
            t2.setLastName("Smith");
            t2.setDateOfBirth(LocalDate.of(1998, 3, 15));
            t2.setAddress("NY");
            t2.setActive(true);
            facade.createTrainee(t2);
        }
    }
}