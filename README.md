# Gym CRM Spring Core

In-memory CRM application built with Spring Core. The project keeps storage in Spring-managed maps and exposes basic services for trainees, trainers, and trainings.

## Storage Initialization

Initial trainee and trainer data is loaded during Spring context initialization by `StorageInitializer`.

Seed data is split by domain entity:

| Entity | Resource | Format |
| :--- | :--- | :--- |
| Trainees | `src/main/resources/trainees-data.txt` | `id;firstName;lastName;username;address;active` |
| Trainers | `src/main/resources/trainers-data.txt` | `id;firstName;lastName;username;specialization;active` |

The resource locations are configured in `src/main/resources/application.properties`:

```properties
storage.init.trainees-file=classpath:trainees-data.txt
storage.init.trainers-file=classpath:trainers-data.txt
```

IDs and usernames are part of the seed data. The initializer validates duplicate usernames across trainees and trainers, and duplicate IDs within each entity file.

## Username Generation

`UsernameGenerator` creates a base username from first and last name, then checks uniqueness through DAO methods instead of requiring callers to load all users into memory.

The DAOs expose a single existence check:

- `TraineeDao.existsByUsername(String username)`
- `TrainerDao.existsByUsername(String username)`

This keeps the storage lookup hidden behind the DAO layer and avoids pulling all data for username validation.

## Tests and JaCoCo Coverage

The project uses **JUnit 5**, **Spring Test**, and **JaCoCo**.

### Implemented Tests
- **`TraineeServiceTest`**: CRUD operations, username/password generation, duplicate handling.
- **`TrainerServiceTest`**: CRUD operations, duplicate handling with init data.
- **`TrainingServiceTest`**: Creation and selection logic.
- **`StorageInitializationTest`**: Verifies data loading from split trainee and trainer resource files.
- **`GymFacadeTest`**: End-to-end flow validation.

### FIRST Principles
- **Fast**: In-memory storage.
- **Independent**: Isolated context.
- **Repeatable**: Deterministic assertions.
- **Self-validating**: Automatic verification.
- **Timely**: Comprehensive coverage.

### Execution Commands
| Task | Command |
| :--- | :--- |
| **Run Tests** | `mvn clean test` |
| **Generate Report** | `mvn jacoco:report` |
| **Enforce Coverage (80%)** | `mvn jacoco:check` |

**Report Location**: `target/site/jacoco/index.html`
