# StockSmart

**Retail Inventory Management System** — B.Tech full-stack academic project.

Demonstrates **Angular**, **Spring Boot**, **Spring Data JPA**, **Hibernate**, **Spring Security**, **Oracle Database**, REST CRUD, inventory workflows, role-based auth, and KPI reporting.

> Scope and architecture are defined in [PROJECT.md](./PROJECT.md). Detailed design lives under [docs/](./docs/).

## Tech stack

| Layer | Technologies |
|-------|----------------|
| Frontend | Angular 19, TypeScript, Reactive Forms, HttpClient |
| Backend | Java 21 (source), Spring Boot 3.4, Spring Web, Spring Data JPA, Spring Security (from Phase 2), Lombok, Jakarta Validation |
| Database | Oracle Database (19c / 21c / 23ai Free) |

## Repository layout

```
StockSmart/
├── backend/          # Spring Boot application
├── frontend/         # Angular application
├── docs/             # Architecture, ER diagrams, API notes
├── PROJECT.md
├── TODO.md
└── CHANGELOG.md
```

## Getting started

### Oracle

1. Install Oracle 23ai Free / XE, or run a local Oracle instance.
2. Create a user/schema, for example `STOCKSMART`.
3. Override connection settings if needed:

```
SPRING_DATASOURCE_URL=jdbc:oracle:thin:@localhost:1521/FREEPDB1
SPRING_DATASOURCE_USERNAME=stocksmart
SPRING_DATASOURCE_PASSWORD=stocksmart
```

Default settings are in `backend/src/main/resources/application-dev.yml`. Hibernate `ddl-auto` is `none` until entities are added in later phases.

If Oracle is not running, the backend may still boot (Hikari initialization-fail-timeout is `-1`) so `/api/health` can be demonstrated. JPA operations will fail until the database is available.

### Backend

```
cd backend
mvnw.cmd spring-boot:run
```

If Maven is already on PATH: `mvn spring-boot:run`

API health: `http://localhost:8080/api/health`

### Frontend

Drive C: on this machine is often low on space. Use a cache on D: if `npm install` fails with `ENOSPC`:

```
cd frontend
$env:npm_config_cache = "D:\StockSmart\.npm-cache"
$env:TEMP = "D:\StockSmart\.tmp"
$env:TMP = "D:\StockSmart\.tmp"
npm install
npm start
```

App: `http://localhost:4200` (proxies `/api` to port 8080).

## Documentation

- [Project specification](./PROJECT.md)
- [Architecture](./docs/architecture.md)
- [Database design](./docs/database.md)
- [Implementation TODO](./TODO.md)
