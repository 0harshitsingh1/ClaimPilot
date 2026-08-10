# ExpenseFlow Backend

ExpenseFlow Backend scaffolding built with Java 17 and Spring Boot 3.4.2.

## Technical Stack

- Java 17
- Spring Boot 3.4.2
- Maven
- Spring Web, Data JPA, Security, Validation, Lombok, PostgreSQL Driver

## Folder Structure

```
com.expenseflow.backend
├── config
├── controller
├── service
├── repository
├── entity
├── dto
├── security
└── exception
```

## Running the Application

1. Ensure PostgreSQL is created:
   ```sql
   CREATE DATABASE expenseflow_db;
   ```

2. Export environment variables:
   ```bash
   export DB_URL="jdbc:postgresql://localhost:5432/expenseflow_db"
   export DB_USERNAME="postgres"
   export DB_PASSWORD="your_password"
   ```

3. Build & Run:
   ```bash
   mvn clean compile
   mvn spring-boot:run
   ```

4. Health Check:
   ```http
   GET http://localhost:8080/api/v1/health
   ```
