# Soccer Management System

Spring Boot microservices project for managing leagues, teams, stadiums, and matches.

This repo is set up for the Project Milestone 2 Part 1 demo:
- 5 services
- API Gateway on `8080`
- Match Service as orchestrator/data aggregator
- Local run with H2
- Docker run with PostgreSQL
- Local seed data only
- No external API keys required for the demo

---

## Services

- `api-gateway` on `8080`
- `match-service` on `8081`
- `league-service` on `8082`
- `team-service` on `8083`
- `stadium-service` on `8084`

### Architecture Notes

- `match-service` is the orchestrator/data aggregator
- `match-service` uses HATEOAS, ports/interfaces, and ACL-style service adapters
- services communicate over HTTP/REST
- services do **not** access each other's databases directly
- each non-gateway service owns its own database
- local demo mode uses packaged JSON seed data

---

## Build From Root

From the project root:

```powershell
.\gradlew.bat clean build
```

This builds the full multi-project Gradle setup from the root.

You can also build all bootable JARs explicitly with:

```powershell
.\gradlew.bat clean buildAllJars
```

---

## Run Locally (IntelliJ + H2)

Run these applications in this order:

1. `LeagueServiceApplication`
2. `TeamServiceApplication`
3. `StadiumServiceApplication`
4. `MatchServiceApplication`
5. `GatewayApplication`

### Local Runtime Notes

- local profile is the default
- local mode uses H2 in-memory databases
- gateway routes to `localhost` service ports
- no PostgreSQL is needed
- no external API keys are needed

### Local URLs

- Gateway: `http://localhost:8080`
- Match Service: `http://localhost:8081`
- League Service: `http://localhost:8082`
- Team Service: `http://localhost:8083`
- Stadium Service: `http://localhost:8084`

---

## Run With Docker (PostgreSQL)

From the project root:

```powershell
docker compose down
docker compose up --build
```

This starts 9 containers:

- 5 service containers
- 4 PostgreSQL containers

### Docker Runtime Notes

- non-gateway services run with the `docker` profile
- `api-gateway` also runs with the `docker` profile
- gateway routes to Docker service hostnames, not `localhost`
- PostgreSQL is used for the 4 non-gateway services
- local seed data is still used for demo/test flows
- no external API keys are required

### Docker Ports

- Gateway: `8080`
- Match Service: `8081`
- League Service: `8082`
- Team Service: `8083`
- Stadium Service: `8084`
- Match DB: `5433`
- League DB: `5434`
- Team DB: `5435`
- Stadium DB: `5436`

---

## Swagger URLs

Use these URLs in local or Docker runs:

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8081/swagger-ui.html`
- `http://localhost:8082/swagger-ui.html`
- `http://localhost:8083/swagger-ui.html`
- `http://localhost:8084/swagger-ui.html`

---

## H2 Console

When running locally, each non-gateway service uses H2.

Service-local H2 console paths are enabled in the service configs:

- `http://localhost:8081/h2-console`
- `http://localhost:8082/h2-console`
- `http://localhost:8083/h2-console`
- `http://localhost:8084/h2-console`

Typical local credentials:

- User: `sa`
- Password: blank

---

## API Summary

### Gateway Routes

Gateway forwards these collection and nested paths:

- `/matches` and `/api/matches/**`
- `/leagues` and `/api/leagues/**`
- `/teams` and `/api/teams/**`
- `/stadiums` and `/api/stadiums/**`

### Core REST Endpoints

#### League Service

```text
GET    /api/leagues
GET    /api/leagues/{id}
POST   /api/leagues
POST   /api/leagues/bulk
POST   /api/leagues/import/local
PUT    /api/leagues/{id}
DELETE /api/leagues/{id}
```

#### Team Service

```text
GET    /api/teams
GET    /api/teams/{id}
POST   /api/teams
POST   /api/teams/bulk
POST   /api/teams/import/local
PUT    /api/teams/{id}
DELETE /api/teams/{id}
```

#### Stadium Service

```text
GET    /api/stadiums
GET    /api/stadiums/{id}
POST   /api/stadiums
POST   /api/stadiums/bulk
POST   /api/stadiums/import/local
PUT    /api/stadiums/{id}
DELETE /api/stadiums/{id}
```

#### Match Service

```text
GET    /api/matches
GET    /api/matches/{id}
GET    /api/matches/{id}/details
POST   /api/matches
POST   /api/matches/bulk
POST   /api/matches/import/local
PUT    /api/matches/{id}
DELETE /api/matches/{id}
```

---

## Postman Demo Order

Use:

- `postman/soccer-microservices-no-external-api.postman_collection.json`

Run the folders in this order:

1. `00 Ping Services`
2. `01 Bulk Create`
3. `02 Gateway Routed GETs`
4. `03 Get All`
5. `04 Get One`
6. `05 Update One`
7. `06 Delete One`
8. `07 Negative Tests`

### What the Collection Covers

- positive tests
- negative tests
- `GET`, `POST`, `PUT`, `DELETE`
- expected HTTP status codes
- gateway checks
- direct service checks
- local seed/demo-safe flows

---

## Demo Checklist Notes

This repo currently includes:

- root multi-project Gradle build
- Dockerfiles for all 5 services
- `docker-compose.yml` for 9 containers
- service-to-service communication inside Docker
- Swagger on all 5 services
- `@ControllerAdvice` in all services
- REST controllers in all services
- H2 locally
- PostgreSQL in Docker

---

## Verification Commands

### Root Build

```powershell
.\gradlew.bat clean build
```

### Compose Config

```powershell
docker compose config
```

### Full Docker Run

```powershell
docker compose down
docker compose up --build
```

---

## No External API Keys

For the milestone demo:

- do not use `FOOTBALL_DATA_API_TOKEN`
- do not use `API_FOOTBALL_API_KEY`
- use local packaged seed data only

The Docker setup and README flow assume a no-external-key demo path.

---

## Troubleshooting

### Root build fails

Use:

```powershell
.\gradlew.bat clean build
```

from the repo root, not from an individual service folder.

### Docker stack does not start

Rebuild from scratch:

```powershell
docker compose down
docker compose up --build
```

### Gateway route returns 404 in Docker

Make sure you are using the gateway on `8080` and that the stack has fully started. The gateway uses the `docker` profile in Compose and routes to container hostnames.

### Swagger does not open

Try the service-specific URL directly:

- `http://localhost:8081/swagger-ui.html`
- `http://localhost:8082/swagger-ui.html`
- `http://localhost:8083/swagger-ui.html`
- `http://localhost:8084/swagger-ui.html`

---

## Author

**Aarush Patel**  
2026
