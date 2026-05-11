package com.gym.crm.storage;

import com.gym.crm.domain.Trainee;
import com.gym.crm.domain.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class StorageInitializerBeanPostProcessor implements BeanPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(StorageInitializerBeanPostProcessor.class);

    @Value("${storage.init.file}")
    private Resource initResource;

    private final AtomicLong idSeq = new AtomicLong(100);

    private Map<Long, Trainee> traineeStorage;
    private Map<Long, Trainer> trainerStorage;

    @SuppressWarnings("unchecked")
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if ("traineeStorage".equals(beanName)) {
            traineeStorage = (Map<Long, Trainee>) bean;
        } else if ("trainerStorage".equals(beanName)) {
            trainerStorage = (Map<Long, Trainer>) bean;
        }

        if (traineeStorage != null && trainerStorage != null) {
            initOnce();
        }

        return bean;
    }

    private volatile boolean initialized = false;

    private synchronized void initOnce() {
        if (initialized) return;

        log.info("Starting storage initialization from resource: {}", initResource);

        int traineesLoaded = 0;
        int trainersLoaded = 0;
        int skipped = 0;
        int lineNo = 0;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(initResource.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                lineNo++;
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] p = line.split(";");
                if (p.length < 4) {
                    skipped++;
                    log.warn("Skipping malformed line {}: '{}'", lineNo, line);
                    continue;
                }

                long id = idSeq.getAndIncrement();
                String firstName = p[1].trim();
                String lastName = p[2].trim();
                String username = firstName + "." + lastName;

                if ("TRAINEE".equalsIgnoreCase(p[0])) {
                    Trainee t = new Trainee();
                    t.setId(id);
                    t.setFirstName(firstName);
                    t.setLastName(lastName);
                    t.setUsername(username); // important for uniqueness checks
                    t.setAddress(p[3].trim());
                    t.setActive(true);
                    traineeStorage.put(id, t);
                    traineesLoaded++;
                } else if ("TRAINER".equalsIgnoreCase(p[0])) {
                    Trainer t = new Trainer();
                    t.setId(id);
                    t.setFirstName(firstName);
                    t.setLastName(lastName);
                    t.setUsername(username); // important for uniqueness checks
                    t.setSpecialization(p[3].trim());
                    t.setActive(true);
                    trainerStorage.put(id, t);
                    trainersLoaded++;
                } else {
                    skipped++;
                    log.warn("Unknown type at line {}: {}", lineNo, p[0]);
                }
            }

            initialized = true;
            log.info("Storage initialization completed. traineesLoaded={}, trainersLoaded={}, skippedLines={}",
                    traineesLoaded, trainersLoaded, skipped);

        } catch (Exception e) {
            log.error("Storage initialization failed for resource: {}", initResource, e);
        }
    }
}