package com.gym.crm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * Excludes Boot's JPA autoconfiguration: this app wires its own classic-Hibernate
 * {@code SessionFactory}/{@code HibernateTransactionManager} in {@code HibernateConfig},
 * and letting Boot also create a competing JPA {@code EntityManagerFactory} +
 * transaction manager causes {@code @Transactional} methods to bind the wrong kind of
 * resource holder (EntityManagerHolder vs SessionHolder), throwing a ClassCastException
 * at runtime.
 */
@SpringBootApplication(exclude = HibernateJpaAutoConfiguration.class)
public class CrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrmApplication.class, args);
    }
}
