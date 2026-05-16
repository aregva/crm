package com.gym.crm.storage;

import com.gym.crm.domain.Trainee;
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
public class TraineeDataParser {
    private static final Logger log = LoggerFactory.getLogger(TraineeDataParser.class);
    private static final int EXPECTED_COLUMNS = 6;

    public List<Trainee> parse(Resource resource) {
        List<Trainee> trainees = new ArrayList<>();
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
                    log.warn("Skipping malformed trainee line {}: '{}'", lineNo, trimmed);
                    continue;
                }

                Trainee trainee = new Trainee();
                trainee.setId(Long.parseLong(fields[0].trim()));
                trainee.setFirstName(fields[1].trim());
                trainee.setLastName(fields[2].trim());
                trainee.setUsername(fields[3].trim());
                trainee.setAddress(fields[4].trim());
                trainee.setActive(Boolean.parseBoolean(fields[5].trim()));
                trainees.add(trainee);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse trainee data from resource: " + resource, e);
        }

        return trainees;
    }
}
