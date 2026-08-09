# TaskFlow

A REST API for managing **tasks, comments, and watchers**, built with Java 17 and Spring Boot.

## Overview

**TaskFlow** is a backend application designed to provide a secure and scalable API for task management.

It uses Spring Data JPA for persistence, PostgreSQL as the database, JWT-based authentication with Spring Security, and Redis for caching/session-related functionality. MapStruct and Lombok are used to keep the codebase clean and reduce boilerplate.

The project also includes an OpenAPI/Swagger interface for exploring and testing the API.

---

## Features

* Task management
* Comments on tasks
* Task watchers
* JWT-based authentication and authorization
* PostgreSQL database integration
* Redis integration
* DTO mapping with MapStruct
* Reduced boilerplate with Lombok
* RESTful API architecture
* OpenAPI/Swagger documentation

---

## Tech Stack

| Technology      | Purpose                        |
| --------------- | ------------------------------ |
| Java 17         | Programming language           |
| Spring Boot 4.x | Application framework          |
| Spring Web      | REST API                       |
| Spring Data JPA | Database access                |
| PostgreSQL      | Relational database            |
| Spring Security | Authentication & authorization |
| JWT             | Token-based authentication     |
| Redis           | Caching/session support        |
| MapStruct       | DTO mapping                    |
| Lombok          | Boilerplate reduction          |
| Maven           | Build & dependency management  |

---

## Project Structure

```text
src/
└── main/
    ├── java/
    │   └── com/
    │       └── minal/
    │           └── taskflow/
    │               ├── controllers/
    │               ├── services/
    │               ├── repositories/
    │               ├── dto/
    │               └── filter/
    │                   └── JWTRequestFilter.java
    │
    └── resources/
        └── application.properties
```

The main application layers are organized as:

* **Controllers** — REST API endpoints
* **Services** — Business logic
* **Repositories** — Database access
* **DTOs** — Request/response objects
* **Security** — JWT authentication and request filtering

Base package:

```text
com.minal.taskflow
```

---

## Getting Started

### Prerequisites

Make sure the following are installed and available:

* JDK 17
* PostgreSQL
* Redis
* Git

Maven does not need to be installed separately if you use the included Maven wrapper.

---

### 1. Clone the repository

```bash
git clone <repository-url>
cd TaskFlow
```

### 2. Configure the environment

TaskFlow reads configuration from:

```text
src/main/resources/application.properties
```

The application also supports an optional `.env` file in the project root.

Create:

```text
.env
```

with values similar to:

```properties
DB_URL=jdbc:postgresql://localhost:5432/taskflowdb
DB_USERNAME=taskflow
DB_PASSWORD=secret

JWT_SECRET_KEY=change_me_to_a_strong_secret
JWT_EXPIRATION_TIME=3600000

REDIS_HOST=localhost
REDIS_PORT=6379
```
---

## Running the Application

### Development

Start the application using the Maven wrapper:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

### Using the packaged JAR

Build the project first:

```bash
./mvnw -DskipTests clean package
```

Then run:

```bash
java -jar target/TaskFlow-0.0.1-SNAPSHOT.jar
```

Once the application starts, the API will be available at:

```text
http://localhost:8080/v1/api
```

---

## API Documentation

TaskFlow provides an interactive Swagger UI for exploring the available endpoints.

Open:

```text
http://localhost:8080/docs
```

The application uses the following context path:

```text
/v1/api
```

---

## Testing

Run the test suite with:

```bash
./mvnw test
```

To build the project without running tests:

```bash
./mvnw -DskipTests clean package
```

---

## Configuration

The main application configuration is located at:

```text
src/main/resources/application.properties
```

The following environment variables are used by the application:

| Variable              | Description                 | Example                                       |
| --------------------- | --------------------------- | --------------------------------------------- |
| `DB_URL`              | PostgreSQL JDBC URL         | `jdbc:postgresql://localhost:5432/taskflowdb` |
| `DB_USERNAME`         | Database username           | `taskflow`                                    |
| `DB_PASSWORD`         | Database password           | `secret`                                      |
| `JWT_SECRET_KEY`      | Secret used for JWT signing | `your-secret`                                 |
| `JWT_EXPIRATION_TIME` | JWT expiration time         | `3600000`                                     |
| `REDIS_HOST`          | Redis host                  | `localhost`                                   |
| `REDIS_PORT`          | Redis port                  | `6379`                                        |

---

## Database

TaskFlow uses PostgreSQL through Spring Data JPA.

The current configuration uses:

```properties
spring.jpa.hibernate.ddl-auto=update
```

This is convenient during development because Hibernate can automatically update the database schema.

For production environments, consider using a dedicated database migration tool such as **Flyway** or **Liquibase** instead of relying on `ddl-auto=update`.

---

## Security

Authentication is implemented using **Spring Security and JWT**.

JWT-related configuration is provided through:

```text
JWT_SECRET_KEY
JWT_EXPIRATION_TIME
```

The JWT request filter can be found at:

```text
src/main/java/com/minal/taskflow/filter/JWTRequestFilter.java
```

Keep the JWT secret private and use a strong, randomly generated value in production.

---

## Useful Commands

```bash
# Build
./mvnw -DskipTests clean package

# Run application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run packaged application
java -jar target/TaskFlow-0.0.1-SNAPSHOT.jar
```

---

## Development

When working on the project, the main areas of the codebase are:

```text
Controllers
└── src/main/java/com/minal/taskflow/controllers

Services
└── src/main/java/com/minal/taskflow/services

Repositories
└── src/main/java/com/minal/taskflow/repositories

DTOs
└── src/main/java/com/minal/taskflow/dto

Security Filter
└── src/main/java/com/minal/taskflow/filter/JWTRequestFilter.java
```

---

## Project Status

TaskFlow is currently under active development.
