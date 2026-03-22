# Soccer Management System

A **DDD modular monolith** Spring Boot 3 application for managing soccer data (teams, leagues, stadiums, matches) with seamless integration of two external APIs: **football-data.org** for competitions and **API-Football** for venues.

---

## Table of Contents

- [Overview](#overview)
- [Modules](#modules)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Quick Start (Local, H2 Database)](#quick-start-local-h2-database)
- [Run with Docker (PostgreSQL)](#run-with-docker-postgresql)
- [Import Workflow](#import-workflow)
- [API Endpoints](#api-endpoints)
- [Swagger & Documentation](#swagger--documentation)
- [H2 Console](#h2-console)
- [Exception Handling](#exception-handling)
- [Testing](#testing)
- [Common Gradle Tasks](#common-gradle-tasks)
- [Environment & Configuration](#environment--configuration)
- [Tips for Development](#tips-for-development)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)

---

## Overview

This is a **Spring Boot 3.3.5** application using **Java 17** and **Gradle** (with wrapper). It follows **Domain-Driven Design (DDD)** principles with a modular monolith structure. Each module handles a distinct domain: **league**, **team**, **location**, and **match**.

### Key Features

✅ **DDD Architecture** — Clean separation of concerns with api, application, domain, and infrastructure layers  
✅ **Import Features** — Import leagues, teams, and matches from football-data.org; import stadiums from API-Football  
✅ **Duplicate Protection** — Prevents re-importing duplicate records  
✅ **Exception Handling** — Clean HTTP status mapping (400, 404, 409, 500)  
✅ **HATEOAS & Swagger** — Self-documenting APIs with Spring Doc OpenAPI  
✅ **Transaction Safety** — All imports wrapped in `@Transactional`  
✅ **Flexible Profiles** — Local (H2) and Docker (PostgreSQL) configurations

---

## Modules

### `league`
**Responsibility:** Manage soccer leagues/competitions  
**Layers:** api, application, domain, infrastructure  
**Key Features:**
- CRUD operations for leagues
- Import leagues from football-data.org (`POST /api/leagues/import/competition/{code}`)
- Duplicate protection by league name

### `team`
**Responsibility:** Manage soccer teams  
**Layers:** api, application, domain, infrastructure  
**Key Features:**
- CRUD operations for teams
- Import teams from football-data.org competitions (`POST /api/teams/import/competition/{code}`)
- Team-League relationship
- Duplicate protection by team name

### `location`
**Responsibility:** Manage stadiums/venues  
**Layers:** api, application, domain, infrastructure  
**Key Features:**
- CRUD operations for stadiums
- Import stadiums from API-Football (`POST /api/stadiums/import/venue/{venueId}`)
- Stadium details: name, city, country, capacity
- Duplicate protection by stadium name

### `match`
**Responsibility:** Manage matches and scheduling  
**Layers:** api, application, domain, infrastructure  
**Key Features:**
- CRUD operations for matches
- Import matches from football-data.org (`POST /api/matches/import/competition/{code}?stadiumId={uuid}`)
- Duplicate detection by (league, homeTeam, awayTeam)
- Missing team/league tracking
- Match details: status (SCHEDULED, COMPLETED, etc.)

### `shared`
**Responsibility:** Cross-cutting concerns  
**Key Features:**
- Global exception handler (maps custom exceptions to HTTP status codes)
- Configuration for external APIs (football-data.org, API-Football)
- Base client implementations for REST calls

---

## Tech Stack

- **Language:** Java 17
- **Framework:** Spring Boot 3.3.5
- **Build:** Gradle 8.x (with wrapper)
- **Database:** H2 (local) / PostgreSQL (Docker)
- **Migrations:** Flyway
- **Documentation:** SpringDoc OpenAPI (Swagger)
- **APIs:** HATEOAS, Spring Data JPA
- **Testing:** JUnit 5, Spring Boot Test
- **Container:** Docker & Docker Compose (optional)

---

## Project Structure

```
soccer-management-system/
├─ src/main/java/com/example/soccermanagement/
│  ├─ league/                          # League module
│  │  ├─ api/                          # REST controllers & DTOs
│  │  ├─ application/                  # Business logic & services
│  │  ├─ domain/                       # Domain aggregate (League)
│  │  └─ infrastructure/               # Persistence & integration
│  ├─ team/                            # Team module (similar structure)
│  ├─ location/                        # Location/Stadium module
│  ├─ match/                           # Match module
│  └─ shared/                          # Shared config & exception handling
│     ├─ api/                          # GlobalExceptionHandler
│     ├─ config/                       # API properties & configuration
│     ├─ domain/                       # Shared domain (DomainException)
│     └─ infrastructure/               # External API clients
├─ src/main/resources/
│  ├─ application.yml                  # Main configuration
│  ├─ application-local.yml            # Local profile (H2)
│  ├─ application-docker.yml           # Docker profile (PostgreSQL)
│  └─ db/migration/                    # Flyway migration scripts
├─ src/test/java/                      # Unit & integration tests
├─ docker-compose.yml                  # Run with PostgreSQL
├─ Dockerfile                          # Container image
├─ build.gradle                        # Gradle build config
├─ gradlew / gradlew.bat               # Gradle wrapper (no local install needed)
├─ settings.gradle                     # Gradle settings
└─ README.md                           # This file
```

---

## Prerequisites

- **JDK 17+** (required for Spring Boot 3.3.5)
- **Git**
- **Docker & Docker Compose** (only if you want to run with PostgreSQL)
- You **do not** need a local Gradle install — use the wrapper: `./gradlew` (or `.\gradlew.bat` on Windows)

---

## Quick Start (Local, H2 Database)

From the project root:

### 1. Clone or Open the Repository
```bash
git clone <repo-url>
cd soccer-management-system
```

### 2. Build the Project
```bash
./gradlew clean build
```
(On Windows: `.\gradlew.bat clean build`)

### 3. Run the Application
```bash
./gradlew bootRun
```

The application starts on **http://localhost:8080** with the **local** profile (H2 in-memory database).

### 4. Access Swagger UI
```
http://localhost:8080/swagger-ui/index.html
```

### 5. Access H2 Console (optional)
```
http://localhost:8080/h2-console
```
- JDBC URL: `jdbc:h2:mem:testdb`
- User: `sa`
- Password: (leave blank)

---

## Run with Docker (PostgreSQL)

From the project root:

### 1. Build and Start Services
```bash
docker compose up --build
```

This starts:
- **Spring Boot App** on port 8080
- **PostgreSQL** on port 5432

### 2. Stop Services
```bash
docker compose down
```

### 3. View Logs
```bash
docker compose logs -f app
```

---

## Import Workflow

The system integrates with **two external APIs** for importing data:

### Step 1: Import League (from football-data.org)
```bash
POST http://localhost:8080/api/leagues/import/competition/PL
```
**Response:** 201 Created with League details  
**Used for:** Fetching competition name and metadata

### Step 2: Import Teams (from football-data.org)
```bash
POST http://localhost:8080/api/teams/import/competition/PL
```
**Response:** 201 Created with import summary (imported: X, skipped: Y)  
**Used for:** Teams participating in the competition

### Step 3: Create or Import Stadium (from API-Football)
Option A: Create manually
```bash
POST http://localhost:8080/api/stadiums
Body: { "name": "Anfield" }
```

Option B: Import from API-Football
```bash
POST http://localhost:8080/api/stadiums/import/venue/549
```
**Response:** 201 Created with Stadium details

### Step 4: Import Matches (from football-data.org)
```bash
POST http://localhost:8080/api/matches/import/competition/PL?stadiumId=<uuid>
```
**Response:** 201 Created with import summary (imported: X, skipped: Y, missingTeams: Z, missingLeague: W)

---

## API Endpoints

### League Module
```
GET    /api/leagues                                   # List all leagues
GET    /api/leagues/{id}                              # Get one league
POST   /api/leagues                                   # Create league (manual)
POST   /api/leagues/import/competition/{code}         # Import from football-data.org
PUT    /api/leagues/{id}                              # Update league
DELETE /api/leagues/{id}                              # Delete league
```

### Team Module
```
GET    /api/teams                                     # List all teams
GET    /api/teams/{id}                                # Get one team
POST   /api/teams                                     # Create team (manual)
POST   /api/teams/import/competition/{code}           # Import from football-data.org
PUT    /api/teams/{id}                                # Update team
DELETE /api/teams/{id}                                # Delete team
```

### Location Module (Stadiums)
```
GET    /api/stadiums                                  # List all stadiums
GET    /api/stadiums/{id}                             # Get one stadium
POST   /api/stadiums                                  # Create stadium (manual)
POST   /api/stadiums/import/venue/{venueId}           # Import from API-Football
PUT    /api/stadiums/{id}                             # Update stadium
DELETE /api/stadiums/{id}                             # Delete stadium
```

### Match Module
```
GET    /api/matches                                   # List all matches
GET    /api/matches/{id}                              # Get one match
GET    /api/matches/{id}/details                      # Get match with team/league details
POST   /api/matches                                   # Create match (manual)
POST   /api/matches/import/competition/{code}         # Import from football-data.org
DELETE /api/matches/{id}                              # Delete match
```

---

## Swagger & Documentation

Access the interactive Swagger UI at:
```
http://localhost:8080/swagger-ui/index.html
```

All endpoints are self-documenting with request/response examples, parameter descriptions, and error codes.

---

## H2 Console

When running locally with the **local** profile, access the H2 web console:
```
http://localhost:8080/h2-console
```

**Credentials:**
- JDBC URL: `jdbc:h2:mem:testdb`
- User: `sa`
- Password: (leave blank)

---

## Exception Handling

All custom exceptions are mapped to HTTP status codes via the **GlobalExceptionHandler**:

| Exception | HTTP Status | Example |
|-----------|-------------|---------|
| `LeagueNotFoundException` | 404 | League not found for code: PL |
| `TeamNotFoundException` | 404 | Team not found |
| `StadiumNotFoundException` | 404 | Stadium not found |
| `MatchNotFoundException` | 404 | Match not found |
| `LeagueConflictException` | 409 | League already exists: Premier League |
| `TeamConflictException` | 409 | Team already exists: Manchester United |
| `StadiumConflictException` | 409 | Stadium already exists: Anfield |
| `MatchConflictException` | 409 | Match already exists |
| `DomainException` | 400 | Home team and away team must be different |
| `MethodArgumentNotValidException` | 400 | Validation failed |
| Generic `Exception` | 500 | Unexpected error |

**Response Format:**
```json
{
  "timestamp": "2026-03-22T10:30:45.123456",
  "status": 404,
  "error": "Not Found",
  "message": "League not found for code: INVALID",
  "path": "/api/leagues/import/competition/INVALID",
  "details": []
}
```

---

## Testing

### Run All Tests
```bash
./gradlew test
```

### Run Tests for a Specific Module
```bash
./gradlew :league:test
./gradlew :team:test
./gradlew :match:test
./gradlew :location:test
```

### Run Tests with Coverage (if configured)
```bash
./gradlew test jacocoTestReport
```

---

## Common Gradle Tasks

```bash
# Build everything
./gradlew clean build

# Run the application
./gradlew bootRun

# Run tests
./gradlew test

# Check code quality
./gradlew check

# Build Docker image
./gradlew bootBuildImage

# View available tasks
./gradlew tasks
```

---

## Environment & Configuration

### Profiles
- **local** (default): H2 in-memory database
- **docker**: PostgreSQL in Docker

### Key Configuration Properties

**application.yml:**
```yaml
spring:
  profiles:
    active: local          # Change to 'docker' for PostgreSQL
  jpa:
    hibernate:
      ddl-auto: validate  # Flyway manages schema
  flyway:
    enabled: true         # Enable database migrations

football-data:           # football-data.org API
  base-url: https://api.football-data.org/v4
  api-token: ${FOOTBALL_DATA_TOKEN:your-token-here}

api-football:            # API-Football API
  base-url: https://v3.football.api-sports.io
  api-key: ${API_FOOTBALL_KEY:your-key-here}
```

### Environment Variables
Set these for external API access:
```bash
FOOTBALL_DATA_TOKEN=your-football-data-token
API_FOOTBALL_KEY=your-api-football-key
```

---

## Tips for Development

1. **Start with a fresh database:**
   ```bash
   rm -rf ~/.h2/test* (on Linux/Mac)
   ```
   Then run the application fresh.

2. **Use Swagger to test endpoints** — it's faster than Postman for quick checks.

3. **Import in order:**
   1. League
   2. Team
   3. Stadium (optional if using manual creation)
   4. Match

4. **Check API tokens** — If import endpoints return 401 or 403, verify your external API tokens are valid.

5. **Watch the logs** — The app logs all HTTP calls to external APIs:
   ```
   DEBUG com.example.soccermanagement...
   ```

6. **Use Spring Boot Actuator** for health checks:
   ```
   http://localhost:8080/actuator/health
   ```

---

## Troubleshooting

### Port 8080 Already in Use
```bash
# Change in application.yml
server:
  port: 8081
```

### H2 Console Not Accessible
Ensure the **local** profile is active (check logs for "The following profiles are active: local").

### Import Endpoints Return 404
1. Verify the external API token is set (`FOOTBALL_DATA_TOKEN`, `API_FOOTBALL_KEY`)
2. Check if the resource exists in the external API (e.g., code "PL" for Premier League)
3. Review logs for REST client errors

### Duplicate Import Error (409 Conflict)
This is expected on the second import of the same resource. The system prevents duplicates by name.

### PostgreSQL Connection Failed (Docker)
```bash
# Ensure Docker Compose is running
docker compose up -d

# Check logs
docker compose logs postgres
```

### Gradle Build Out of Memory
Add to `gradle.properties`:
```
org.gradle.jvmargs=-Xmx2g
```

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-change`
3. Commit with conventional commits: `git commit -m "feat: add X"`
4. Push: `git push origin feature/my-change`
5. Open a Pull Request

**Commit Message Format:**
```
feat: add new feature
fix: resolve bug
docs: update documentation
refactor: improve code quality
test: add tests
```

---

## License

This project is for educational purposes. Feel free to use and modify as needed.

---

## Author

**Aarush Patel** — 2026

For questions or issues, please open an issue on GitHub or contact the maintainers.
