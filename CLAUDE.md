# CLAUDE.md — StockSmart Project Guidelines

## Project Identity

**StockSmart — Retail Inventory Management System (B.Tech academic project)**  
Multi-location inventory, products, suppliers, purchase orders, sales orders, transfers, barcode lookup, KPI dashboard, and role-based security.

This is a **demonstrable college project**, not a production enterprise platform. Prefer clarity and working features over enterprise patterns.

---

## Technology Stack

### Frontend

- **Framework:** Angular (v17+), TypeScript 5.x
- **Forms & HTTP:** Reactive Forms, HttpClient
- **UI:** Clean responsive layout (Angular Material or simple CSS/SCSS — keep consistent)
- **Barcode:** Manual entry + search; optional keyboard-wedge friendly input field

### Backend

- **Framework:** Java 17+, Spring Boot 3.x
- **Persistence:** Spring Data JPA, Hibernate 6.x
- **Security:** Spring Security 6.x, JWT (simple access token)
- **Utilities:** Lombok, Jakarta Validation
- **DTO mapping:** Manual mapping in services or controllers (MapStruct optional, not required)

### Database

- **Engine:** Oracle Database (19c / 21c / 23ai Free)
- **Schema:** JPA entities + `ddl-auto` for development **or** documented SQL setup script
- **Dialect:** `org.hibernate.dialect.OracleDialect`

### Architecture

**Monolithic layered:**

`Controller → Service → Repository → Entity → Oracle`

Packages per feature: `controller`, `service`, `repository`, `entity`, `dto`, plus shared `config`, `exception`, `security`.

**Do not use:** microservices, hexagonal ports/adapters, CQRS, Kafka, Redis, double-entry ledger, optimistic `@Version` on inventory (unless explicitly added later).

---

## Build, Test, and Run Commands

### Backend (`/backend` when present)

- **Build:** `./mvnw clean package -DskipTests`
- **Run:** `./mvnw spring-boot:run` (profile `dev`)
- **Test:** `./mvnw test`

### Frontend (`/frontend` when present)

- **Install:** `npm install`
- **Run:** `ng serve --port 4200`
- **Build:** `npm run build`
- **Test:** `npm test`

---

## Conventions

### Java

1. Do not expose JPA entities from REST controllers — use DTOs.
2. `@Transactional(readOnly = true)` on service classes by default; `@Transactional` on write methods.
3. Inventory quantity changes only through inventory service methods (stock in, out, adjust, transfer, PO receive, order confirm).
4. Global `@RestControllerAdvice` with simple JSON error body (message, status, optional field errors).
5. Roles: `ADMIN`, `INVENTORY_MANAGER`, `STAFF`.

### Angular

1. Standalone components.
2. Reactive Forms for business forms.
3. `AuthInterceptor` for JWT; functional route guards for auth and roles.
4. Unsubscribe via `takeUntilDestroyed()` or `async` pipe.

---

## Git Workflow

- **Branches:** `feature/<module>-<description>`, `docs/<description>`
- **Commits:** Conventional Commits, e.g. `feat(inventory): add stock adjustment API`

---

## Documentation

- **PROJECT.md** defines scope and entities.
- **docs/** must match **actual** code; simplify enterprise wording when updating diagrams.
- **Future enhancements:** RFID hardware, GS1, immutable ledger, Flyway, offline scans — see PROJECT.md.
