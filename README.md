# Gym CRM (Spring Boot Edition)

A comprehensive CRM backend system for a Gym management ecosystem, built using **Java 17, Spring Boot, Spring Web (REST), Hibernate ORM 6, and an embedded H2 database**.

The architecture follows a layered design:

$$\text{Controller} \longrightarrow \text{Facade} \longrightarrow \text{Service} \longrightarrow \text{DAO (Hibernate Mapping)}$$

---

## 🚀 Migration Note (Spring Boot)

This project was migrated from **Spring Core (manual configuration)** to **Spring Boot**.

Key changes:
- Replaced `AnnotationConfigApplicationContext` with **Spring Boot auto-configured context**
- Introduced `@SpringBootApplication`
- Enabled **auto-configuration of DataSource and Hibernate**
- Environment-based configuration via `application-{profile}.properties`
- Test support via `@SpringBootTest` and `@ActiveProfiles("local")`

---

## 🏗️ Relational Domain Model

The data access layer manages a relational schema utilizing an atomic structure where core profile metadata is decoupled from specialized roles:

* **`users`**: Houses shared authentication and credential metadata (`id`, `firstName`, `lastName`, `username`, `password`, `isActive`)
* **`trainee`**: Extends a `users` reference with trainee metrics (`dateOfBirth`, `address`)
* **`trainer`**: Extends a `users` reference with technical specializations (`specializationType`)
* **`training`**: Connective transactional ledger linking a Trainee, a Trainer, and a specific `training_type`
* **`training_type`**: Immutable catalog tracking available gym training genres (e.g., YOGA, FITNESS)
* **`trainee_trainer`**: Many-to-many join relationship structure linking trainees to trainers

---

## 🔒 Business & Authentication Rules

1. **API Scope**: All REST routes are contained under `/api/**`
2. **Authentication**: All endpoints except registration require authentication via `RestAuthenticationService`
3. **Role Mutability Constraint**: A user can act as either Trainer or Trainee, never both
4. **Username Immutability**: Usernames are generated at registration and cannot be changed
5. **Training Ledger**: Trainings are append-only records (no update/delete via REST)
6. **Hard Cascade Removal**: Deleting a trainee removes all related training and associations

---

## 🌐 REST API Endpoints

### 1. Trainee Registration
* **Route**: `POST /api/trainees`
* **Access**: Public

#### Request Body
```json
{
  "firstName": "John",
  "lastName": "Smith",
  "dateOfBirth": "2000-01-01",
  "address": "Yerevan"
}
Response
{
  "username": "john.smith",
  "password": "generatedPassword"
}
2. Trainer Registration
Route: POST /api/trainers
Access: Public
Request Body
{
"firstName": "Jane",
"lastName": "Doe",
"specialization": "YOGA"
}
Response
{
"username": "jane.doe",
"password": "generatedPassword"
}
🧪 Testing

After migration to Spring Boot:

Unit tests now run under Spring Boot context when needed
Integration tests use @SpringBootTest
Profile-based testing uses @ActiveProfiles("local")
Embedded H2 database is used for all test executions

Example:

@SpringBootTest
@ActiveProfiles("local")
class GymFacadeTest {
}
⚙️ Configuration
application-local.properties
db.url=jdbc:h2:mem:gymdb
db.username=sa
db.password=
db.driver=org.h2.Driver
🏁 Summary of Migration Benefits
Simplified configuration via Spring Boot auto-configuration
Profile-based environments (local, test, prod)
Cleaner test setup with Spring Boot test support
Reduced boilerplate (no manual context creation)
Better scalability for REST and future microservices migration### 3. Login

* **Route**: `GET /api/auth/login`
* **Access**: Basic Auth Authenticated

#### Response

* **`200 OK`** if Basic authentication credentials are valid.
* **`401 Unauthorized`** if credentials fail validation.

---

### 4. Change Password

* **Route**: `PUT /api/auth/password`
* **Access**: Basic Auth Authenticated

#### Request Body

```json
{
  "username": "john.smith",
  "oldPassword": "old123",
  "newPassword": "new123"
}

```

#### Response Body (`200 OK`)

---

### 5. Get Trainee Profile

* **Route**: `GET /api/trainees/{username}`
* **Access**: Basic Auth Authenticated

#### Response Body (`200 OK`)

```json
{
  "firstName": "John",
  "lastName": "Smith",
  "dateOfBirth": "2000-01-01",
  "address": "Yerevan",
  "active": true,
  "trainers": [
    {
      "username": "jane.doe",
      "firstName": "Jane",
      "lastName": "Doe",
      "specialization": "YOGA"
    }
  ]
}

```

---

### 6. Update Trainee Profile

* **Route**: `PUT /api/trainees/{username}`
* **Access**: Basic Auth Authenticated

#### Request Body

```json
{
  "firstName": "John",
  "lastName": "Smith",
  "dateOfBirth": "2000-01-01",
  "address": "Yerevan",
  "active": true
}

```

#### Response Body (`200 OK`)

```json
{
  "username": "john.smith",
  "firstName": "John",
  "lastName": "Smith",
  "dateOfBirth": "2000-01-01",
  "address": "Yerevan",
  "active": true,
  "trainers": []
}

```

---

### 7. Delete Trainee Profile

* **Route**: `DELETE /api/trainees/{username}`
* **Access**: Basic Auth Authenticated

#### Response Status (`200 OK`)

---

### 8. Get Trainer Profile

* **Route**: `GET /api/trainers/{username}`
* **Access**: Basic Auth Authenticated

#### Response Body (`200 OK`)

```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "specialization": "YOGA",
  "active": true,
  "trainees": []
}

```

---

### 9. Update Trainer Profile

* **Route**: `PUT /api/trainers/{username}`
* **Access**: Basic Auth Authenticated

#### Request Body

```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "active": true
}

```

#### Response Body (`200 OK`)

```json
{
  "username": "jane.doe",
  "firstName": "Jane",
  "lastName": "Doe",
  "specialization": "YOGA",
  "active": true,
  "trainees": []
}

```

---

### 10. Get Unassigned Active Trainers

* **Route**: `GET /api/trainees/{username}/available-trainers`
* **Access**: Basic Auth Authenticated

#### Response Body (`200 OK`)

```json
[
  {
    "username": "jane.doe",
    "firstName": "Jane",
    "lastName": "Doe",
    "specialization": "YOGA"
  }
]

```

---

### 11. Update Trainee Trainer List

* **Route**: `PUT /api/trainees/{username}/trainers`
* **Access**: Basic Auth Authenticated

#### Request Body

```json
{
  "trainers": ["jane.doe"]
}

```

#### Response Body (`200 OK`)

```json
{
  "trainers": [
    {
      "username": "jane.doe",
      "firstName": "Jane",
      "lastName": "Doe",
      "specialization": "YOGA"
    }
  ]
}

```

---

### 12. Get Trainee Trainings List

* **Route**: `GET /api/trainings/trainee/{username}`
* **Access**: Basic Auth Authenticated
* **Query Parameters (Optional)**:
* `fromDate` (ISO format `YYYY-MM-DD`)
* `toDate` (ISO format `YYYY-MM-DD`)
* `trainerName` (String filter)
* `trainingType` (String matching code)



#### Response Body (`200 OK`)

```json
[
  {
    "trainingName": "Gym",
    "trainingDate": "2026-01-01",
    "trainingType": "FITNESS",
    "trainingDuration": 60,
    "trainerName": "Jane Doe"
  }
]

```

---

### 13. Get Trainer Trainings List

* **Route**: `GET /api/trainings/trainer/{username}`
* **Access**: Basic Auth Authenticated
* **Query Parameters (Optional)**:
* `fromDate`
* `toDate`
* `traineeName`



#### Response Body (`200 OK`)

```json
[
  {
    "trainingName": "Gym",
    "trainingDate": "2026-01-01",
    "trainingType": "FITNESS",
    "trainingDuration": 60,
    "traineeName": "John Smith"
  }
]

```

---

### 14. Add Training

* **Route**: `POST /api/trainings`
* **Access**: Basic Auth Authenticated

#### Request Body

```json
{
  "traineeUsername": "john.smith",
  "trainerUsername": "jane.doe",
  "trainingName": "Gym",
  "trainingDate": "2026-01-01",
  "trainingDuration": 60
}

```

#### Response Status (`200 OK`)

---

### 15. Activate / Deactivate Trainee

* **Route**: `PATCH /api/trainees/{username}/status`
* **Access**: Basic Auth Authenticated

#### Request Body

```json
{
  "active": true
}

```

#### Response

* **`200 OK`**: Status altered successfully.
* **`409 Conflict`**: State modification request matches existing db status value (Non-idempotent tracking rule validation).

---

### 16. Activate / Deactivate Trainer

* **Route**: `PATCH /api/trainers/{username}/status`
* **Access**: Basic Auth Authenticated

#### Request Body

```json
{
  "active": false
}

```

#### Response

* **`200 OK`**: Status modified.
* **`409 Conflict`**: If the target trainer entity is already in the matching state.

---

### 17. Get Training Types

* **Route**: `GET /api/training-types`
* **Access**: Basic Auth Authenticated

#### Response Body (`200 OK`)

```json
[
  {
    "trainingType": "YOGA",
    "trainingTypeId": 1
  },
  {
    "trainingType": "FITNESS",
    "trainingTypeId": 2
  }
]

```

---

## 🛠️ Tech Stack Matrix

* **Core Runtime Language**: Java 17
* **Inversion of Control & REST Routing Layer**: Spring Framework Core / Spring MVC (6.x)
* **Data Mapping & Transactions Engine**: Hibernate ORM Core (6.5.2.Final) / Spring ORM
* **Local In-Memory Persistence Layer**: H2 Database (2.2.224)
* **Data Verification Engine**: Hibernate Validator (8.0.1.Final) & Jakarta Validation API
* **Serialization & Marshalling Suite**: Jackson Databind & JavaTimeModules (2.17.2)
* **Performance Diagnostics & Tracing**: Micrometer Observations API
* **Engine Test Suites**: JUnit Jupiter Engine 5.10 & Mockito Framework
* **REST Execution Testing**: Spring-Test Suite using custom standalone `MockMvc` contexts

---

## 🧪 System Testing Strategy

1. **Core Domain Unit Tests**: Covers business rule validation algorithms across isolated Service-layer and architectural Facade components with mocked data providers.
2. **REST Integration Testing**: Managed strictly via MockMvc configurations validating status contracts, authentication context parsing, response payloads, and entity payload validation hooks.
3. **Transactional Database Verifications**: Leveraging automatic text storage initialization script loaders (`trainees-data.txt`, `trainers-data.txt`) targeted at clean transactional contexts within an isolated H2 environment to ensure side-effect free, repeatable test executions.
