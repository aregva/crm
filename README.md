## Tests and JaCoCo Coverage

The project uses **JUnit 5**, **Spring Test**, and **JaCoCo**.

### Implemented Tests
- **`TraineeServiceTest`**: CRUD operations, username/password generation, duplicate handling.
- **`TrainerServiceTest`**: CRUD operations, duplicate handling with init data.
- **`TrainingServiceTest`**: Creation and selection logic.
- **`StorageInitializationTest`**: Verifies data loading from `init-data.txt`.
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