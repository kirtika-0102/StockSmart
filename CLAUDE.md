# CLAUDE.md — StockSmart Project Guidelines

## Project Identity

**StockSmart — Retail Inventory Management System (B.Tech academic project)**  
Multi-location inventory, products, suppliers, purchase orders, sales orders, transfers, barcode lookup, KPI dashboard, and role-based security.

This is a **demonstrable college project**, not a production enterprise platform. Prefer clarity and working features over enterprise patterns.

Canonical scope: [PROJECT.md](./PROJECT.md).

---

## Technology Stack

### Frontend

- **Framework:** Angular (standalone), TypeScript
- **Forms & HTTP:** Reactive Forms, HttpClient
- **UI:** Clean responsive layout
- **Barcode:** Manual entry + search; optional keyboard-wedge friendly input

### Backend

- **Framework:** Java 17+, Spring Boot 3.x
- **Persistence:** Spring Data JPA, Hibernate
- **Security:** Spring Security, JWT (simple access token) — from Phase 2
- **Utilities:** Lombok, Jakarta Validation
- **DTO mapping:** Manual mapping (MapStruct not required)

### Database

- **Engine:** Oracle Database
- **Schema:** JPA entities; Flyway is **not** required
- **Dialect:** `org.hibernate.dialect.OracleDialect`

### Architecture

**Monolithic layered:**

`Controller → Service → Repository → Entity → Oracle`

**Do not use:** microservices, hexagonal ports/adapters, CQRS, Kafka, Redis, double-entry ledger, optimistic `@Version` on inventory.

---

## Conventions

### Java

1. Do not expose JPA entities from REST controllers — use DTOs.
2. `@Transactional(readOnly = true)` on service classes by default; `@Transactional` on write methods.
3. Inventory quantity changes only through inventory service methods.
4. Global `@RestControllerAdvice` with simple JSON error body.
5. Roles: `ADMIN`, `INVENTORY_MANAGER`, `STAFF`.

### Angular

1. Standalone components.
2. Reactive Forms for business forms.
3. `AuthInterceptor` for JWT; functional route guards.
4. Unsubscribe via `takeUntilDestroyed()` or `async` pipe.

---

## Documentation

- **PROJECT.md** defines scope and entities.
- **docs/** must match **actual** code.
- **Future enhancements:** RFID hardware, GS1, immutable ledger, Flyway, offline scans.
