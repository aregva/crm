# Gym CRM Hibernate

CRM application built with Spring Core, Hibernate, and an embedded H2 database. The domain follows the provided relational model: `users`, `trainee`, `trainer`, `training`, `training_type`, and the trainee-trainer many-to-many join table.

## Persistence

Hibernate is configured in `HibernateConfig` with Spring transaction management enabled in `AppConfig`.

The default database is embedded H2:

```properties
db.url=jdbc:h2:mem:gymcrm;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false
hibernate.hbm2ddl.auto=create-drop
```

`TrainingType` is stored as a reference table and seeded with constant values: `FITNESS`, `YOGA`, `CARDIO`, `CROSSFIT`, and `STRENGTH`.

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

## Supported Operations

- Create trainer and trainee profiles with generated username/password.
- Authenticate trainee and trainer credentials.
- Select trainer and trainee profiles by username.
- Change trainee and trainer passwords.
- Update trainer and trainee profiles with required field validation.
- Activate/deactivate profiles as non-idempotent actions.
- Hard delete trainee profiles with cascade deletion of trainings.
- Add trainings linked by FK to trainee, trainer, and training type.
- Get trainee and trainer training lists by criteria.
- Get trainers not assigned to a trainee.
- Replace a trainee's assigned trainers list.

All mutating service operations are transactional where database state changes are involved. Authenticated operation variants are exposed through `GymFacade`; legacy ID-based methods remain for compatibility with earlier module tests.

## Tests and JaCoCo Coverage

The project uses **JUnit 5**, **Spring Test**, and **JaCoCo**.

### Implemented Tests
- **`TraineeServiceTest`**: CRUD operations, username/password generation, duplicate handling, authenticated updates, password validation, activation/deactivation, hard delete by username, and trainer assignment paths.
- **`TrainerServiceTest`**: CRUD operations, duplicate handling with init data, required field validation, training type validation, authenticated updates, password changes, and activation/deactivation paths.
- **`TrainingServiceTest`**: Creation and selection logic, required field validation, invalid FK/reference handling, authenticated access failures, and criteria-based training list queries.
- **`StorageInitializationTest`**: Verifies data loading from split trainee and trainer resource files.
- **`GymFacadeTest`**: End-to-end flow validation.
- **`HibernateProfileFlowTest`**: Authenticated Hibernate profile, password, activation, training criteria, and trainer assignment flows.

### FIRST Principles
- **Fast**: Embedded H2 database with Hibernate `create-drop` schema generation.
- **Independent**: Isolated context.
- **Repeatable**: Deterministic assertions.
- **Self-validating**: Automatic verification.
- **Timely**: Comprehensive coverage.

### Execution Commands
| Task | Command |
| :--- | :--- |
| **Run Tests** | `./mvnw test` |
| **Generate Report** | `./mvnw jacoco:report` |
| **Enforce Coverage (80%)** | `./mvnw jacoco:check` |

**Report Location**: `target/site/jacoco/index.html`

Java 17 must be available on `PATH`, or `JAVA_HOME` must point to a valid JDK, before running Maven commands.
