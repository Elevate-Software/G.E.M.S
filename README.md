# G.E.M.S — CampusGate

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-green.svg)](https://spring.io/projects/spring-boot)
[![Vite + Solid](https://img.shields.io/badge/Frontend-Solid.js%20%2B%20Vite-blue.svg)](https://www.solidjs.com/)
[![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions%20%7C%20Jenkins-informational.svg)](https://github.com/Elevate-Software/G.E.M.S)

CampusGate (Gate Entry Management System) is an access-control platform designed for campus security. It manages user credentials, validates gate entries, logs access events, and enforces role-based access control.

---

## Tech Stack

| Layer | Technologies |
|---|---|
| **Backend** | Spring Boot 3, Spring Security (JWT), Spring Data JPA, Flyway |
| **Databases** | PostgreSQL (persistent storage), Redis (token blacklisting & cache) |
| **Frontend** | Solid.js, Vite, Vanilla CSS, SPA routing |
| **Testing** | JUnit 5, Mockito, Spring Boot Test, Selenium WebDriver, JaCoCo |
| **CI / CD** | GitHub Actions, Jenkins (`Jenkinsfile`), Docker & Docker Compose |

---

## Quickstart

### Option 1: Run Full Stack with Docker Compose

From the `campusgate/` folder:

```bash
cd campusgate
docker compose up --build
```

- **Backend API**: `http://localhost:8080`
- **PostgreSQL**: `localhost:5433`
- **Redis**: `localhost:6379`

---

### Option 2: Run Locally for Development

#### 1. Start Database & Cache
```bash
cd campusgate
docker compose up -d db redis
```

#### 2. Run Backend
```bash
cd campusgate
cp .env.example .env    # Configure local variables if needed
./mvnw spring-boot:run
```

#### 3. Run Frontend
```bash
cd frontend
npm install
npm run dev
```
Frontend runs at `http://localhost:5173`.

---

## Testing Pyramid

### 1. Unit & Integration Tests
Runs business logic and API integration tests against an in-memory/isolated database:
```bash
cd campusgate
./mvnw clean test
```

### 2. JaCoCo Code Coverage
Generates an HTML coverage report:
```bash
cd campusgate
./mvnw clean verify
# Report generated at: target/site/jacoco/index.html
```

### 3. Selenium E2E System Tests
End-to-end user flows using the **Page Object Model** (`RegisterPage`, `LoginPage`, `DashboardPage`):
```bash
# Run headless (default, CI-friendly)
./mvnw test -Dtest=CampusGateSystemTest

# Run with visible Chrome browser UI
./mvnw test -Dtest=CampusGateSystemTest -Dheadless=false
```

---

## CI / CD Pipelines

### GitHub Actions
Automated workflow on `push` and `pull_request` to `main`:
- Spins up PostgreSQL 15 and Redis services
- Compiles with JDK 21 and runs full test verification
- Publishes JaCoCo coverage summaries and artifacts
- Performs container build dry runs

### Jenkins
Pipeline-as-Code defined in [`Jenkinsfile`](Jenkinsfile):
- **Stages**: Checkout → Unit & Integration Tests → JaCoCo Coverage Report → Selenium E2E Tests → Docker Image Build
- Spin up the local Jenkins instance:
  ```bash
  cd jenkins
  docker compose -f docker-compose.jenkins.yml up -d
  ```

---

## License
Elevate Software. All rights reserved.
