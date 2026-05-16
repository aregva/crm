package com.gym.crm.storage;

import com.gym.crm.domain.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class TrainerDataParser {
    private static final Logger log = LoggerFactory.getLogger(TrainerDataParser.class);
    private static final int EXPECTED_COLUMNS = 6;

    public List<Trainer> parse(Resource resource) {
        List<Trainer> trainers = new ArrayList<>();
        int lineNo = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

                String[] fields = trimmed.split(";", -1);
                if (fields.length < EXPECTED_COLUMNS) {
                    log.warn("Skipping malformed trainer line {}: '{}'", lineNo, trimmed);
                    continue;
                }

                Trainer trainer = new Trainer();
                trainer.setId(Long.parseLong(fields[0].trim()));
                trainer.setFirstName(fields[1].trim());
                trainer.setLastName(fields[2].trim());
                trainer.setUsername(fields[3].trim());
                trainer.setSpecialization(fields[4].trim());
                trainer.setActive(Boolean.parseBoolean(fields[5].trim()));
                trainers.add(trainer);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse trainer data from resource: " + resource, e);
        }

        return trainers;
    }
}
