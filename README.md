# CampusGate

CampusGate is a campus entrance management system built with Spring Boot, PostgreSQL, Redis, Flyway, JWT authentication, and a Vite + Solid frontend. The project models a real-world access-control workflow: a student or visitor arrives at a gate, their credential is validated, and the system records the entry event securely.

### Group Members and Documentation

| Name                 | ID           |
|----------------------|--------------|
| Peter Kinfe          | ATE/7749/15  |
| Abdisa Alemu         | ATE/2603/14  |
| Hailemichael Lijalem | ATE/1051/14  |
| Beamlak Tibebu       | ATE/3624/14  |

## Scenario Insight

Imagine a student named Nathan arriving on campus. He logs in to the system, his identity is verified, and when he reaches a gate, the security team can validate his access and record the entry. If access is denied or suspicious, the system can flag the event for review. That is the core flow this project is designed to support.

## What the project includes

- Backend API for authentication, user management, and gate scanning
- PostgreSQL persistence with Flyway migrations
- Redis support for caching/session-related functionality
- JWT-based security for protected endpoints
- A frontend built with Vite and Solid
- Docker Compose support for local development

## Repository structure

- [campusgate](campusgate) — Spring Boot backend
- [frontend](frontend) — Vite + Solid frontend
- [.github/workflows/ci-cd.yml](.github/workflows/ci-cd.yml) — CI pipeline for build and test

## Prerequisites

Before running the app locally, make sure you have:

- Java 21
- Maven or the Maven wrapper included in the backend folder
- Node.js and npm
- Docker Desktop (for PostgreSQL and Redis)

## Backend setup

### 1. Go to the backend folder

```bash
cd campusgate
```

### 2. Create your environment file

Copy the sample environment file and update the values if needed:

```bash
cp .env.example .env
```

Example values:

```env
DB_URL=jdbc:postgresql://localhost:5433/campusgate
DB_NAME=campusgate
DB_USER=campus_admin
DB_PASS=campus_pass
DB_PORT=5433
REDIS_HOST=localhost
REDIS_PORT=6379
JWT_SECRET=replace-this-with-a-strong-secret
JWT_EXPIRATION=86400000
```

### 3. Start PostgreSQL and Redis with Docker Compose

```bash
docker compose up -d db redis
```

### 4. Run the backend

```bash
./mvnw spring-boot:run
```

The backend will start on:

- http://localhost:8080

### 5. Useful backend commands

```bash
./mvnw clean verify
./mvnw test
```

## Frontend setup

### 1. Install frontend dependencies

```bash
cd frontend
npm install
```

### 2. Start the frontend development server

```bash
npm run dev
```

The frontend will be available at:

- http://localhost:5173

## Run everything with Docker

From the backend folder, you can build and run the full stack with Docker Compose:

```bash
cd campusgate
docker compose up --build
```

This will start:

- the Spring Boot app
- PostgreSQL
- Redis

## API overview

The backend exposes REST endpoints under the following base paths:

- /api/auth — login, registration, logout
- /api/users — user lookup and account management
- /api/scan — access scanning and validation

Example flow:

1. Register or log in a user.
2. Authenticate with the returned JWT token.
3. Use the token to call protected endpoints such as scan operations.

## Database and migrations

Flyway migrations are stored in:

- [campusgate/src/main/resources/db/migration](campusgate/src/main/resources/db/migration)

These migrations create the database schema for users, gates, access credentials, entry logs, and incident reports.

## CI/CD

The repository includes a GitHub Actions workflow that:

- builds the backend with Maven
- runs tests
- starts PostgreSQL and Redis
- builds a Docker image

## Troubleshooting

If the database connection fails, try resetting the local Docker volumes:

```bash
cd campusgate
docker compose down -v
docker compose up -d db redis
```

If you still see authentication issues, confirm that the values in your environment file match the PostgreSQL container credentials.
