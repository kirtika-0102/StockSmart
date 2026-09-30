# CLAUDE.md — StockSmart Project Guidelines

## Project Identity
**StockSmart — Retail Inventory Optimization & Management System**  
Enterprise-grade multi-location retail inventory management, stock ledger, purchasing, transfer, barcode scanning, RFID-ready intake, and analytics system.

---

## Technology Stack

### Frontend
- **Framework:** Angular (v17+)
- **Language:** TypeScript 5.x
- **Markup & Styling:** HTML5, CSS3 / SCSS
- **State Management & Reactivity:** RxJS, Signals / Store services
- **UI Components:** Modular, accessible components (Angular Material / Tailwind CSS design tokens)
- **Scanning Integration:** ZXing / BarcodeDetector API for WebRTC camera scanning, Keyboard-wedge event listener service

### Backend
- **Framework:** Java 17 / 21 LTS, Spring Boot 3.x
- **Data Persistence:** Spring Data JPA, Hibernate 6.x
- **Security:** Spring Security 6.x (Stateless JWT Authentication, Granular RBAC)
- **Boilerplate Reduction:** Project Lombok
- **Mapping:** MapStruct
- **Validation:** Jakarta Validation (`hibernate-validator`)
- **API Documentation:** OpenAPI 3 / Springdoc-OpenAPI

### Database
- **Engine:** Oracle Database (19c / 21c / 23ai Free)
- **Schema Management:** Flyway database migrations
- **Connection Pooling:** HikariCP
- **Dialect:** `org.hibernate.dialect.OracleDialect`

### Architecture Principles
- **Hexagonal / Layered Architecture:** Strict separation between Presentation (REST Controllers), Application (Services/DTOs), Domain (Entities, Rules, Ledger Engine), and Infrastructure (Repositories, Oracle Adapters, External Integrators).
- **Double-Entry Stock Ledger:** Inventory is never mutated through raw in-place updates. Every change in stock creates an immutable `STOCK_TRANSACTION` ledger entry that adjusts location balances.
- **Optimistic Concurrency Control:** Balance records (`INVENTORY_BALANCE`) utilize JPA `@Version` to prevent race conditions during concurrent scans and checkout/receiving operations.
- **GS1 & EPCIS Compliance:** Barcodes follow GS1 standards (EAN-13, UPC-A, GS1-128); RFID tags follow EPC Gen2 (SGTIN-96) standards with an EPCIS-compatible event ingestion pipeline.

---

## Build, Test, and Run Commands

### Backend (Spring Boot)
Located in `/backend` (when scaffolding commences):
- **Build without tests:** `./mvnw clean package -DskipTests`
- **Run local dev server:** `./mvnw spring-boot:run` (Active profile: `dev`)
- **Run all unit tests:** `./mvnw test`
- **Run integration tests:** `./mvnw verify -Pintegration-tests`
- **Flyway migration check:** `./mvnw flyway:info`

### Frontend (Angular)
Located in `/frontend` (when scaffolding commences):
- **Install dependencies:** `npm install`
- **Run dev server:** `npm start` or `ng serve --port 4200`
- **Build production bundle:** `npm run build -- --configuration production`
- **Run unit tests:** `npm test -- --watch=false --browsers=ChromeHeadless`
- **Lint code:** `npm run lint`

### Full Stack Docker Orchestration
- **Spin up local Oracle DB & services:** `docker compose -f docker-compose.dev.yml up -d`
- **View container logs:** `docker compose -f docker-compose.dev.yml logs -f`
- **Tear down environment:** `docker compose -f docker-compose.dev.yml down -v`

---

## Code Quality & Engineering Conventions

### Java Backend Conventions
1. **No Lombok `@Data` on JPA Entities:** Always use `@Getter`, `@Setter`, `@ToString(exclude = {...})`, and explicit `equals`/`hashCode` based on business key (or surrogate ID if persisted) to prevent infinite recursion and lazy-loading hazards.
2. **DTO Invariant:** Never expose JPA Entities directly in REST Controllers. Always map via MapStruct to request/response DTOs.
3. **Transaction Boundaries:** Mark business service methods with `@Transactional(readOnly = true)` by default at class level; mark write operations with explicit `@Transactional(rollbackFor = Exception.class)`.
4. **RFC 7807 Problem Details:** Global exception handling using `@RestControllerAdvice` returning `ProblemDetail` or custom RFC 7807 compliant JSON envelopes.
5. **No Direct Balance Overwrites:** Stock adjustments, receives, sales, and transfers must invoke the `InventoryLedgerService` which writes audit records and ledger transactions before adjusting cached balances.

### Angular Frontend Conventions
1. **Standalone Components:** Adopt Angular standalone components, directives, and pipes.
2. **Reactive Forms:** All business forms (Product, PO, Transfer, Adjustment) must use strictly-typed `ReactiveFormsModule`.
3. **Smart & Presentational (Dumb) Components:** Separate container components (data fetching, state dispatching) from presentational components (`@Input()`, `@Output()`, pure UI).
4. **Unsubscribe Safety:** Utilize `takeUntilDestroyed()`, `AsyncPipe`, or Signals to prevent memory leaks from dangling Observable subscriptions.
5. **Route Guards & Interceptors:** Centralize token authorization in `AuthInterceptor`, error handling in `HttpErrorInterceptor`, and route protection in functional `CanActivateFn` guards.

---

## Git Workflow & Commit Guidelines

- **Branch naming:** `feature/<module>-<description>`, `bugfix/<ticket>-<description>`, `docs/<description>`
- **Commit messages:** Follow Conventional Commits:
  - `feat(inventory): implement double-entry stock ledger transaction engine`
  - `fix(rfid): resolve SGTIN-96 bit shift parsing error`
  - `docs(api): document stock transfer approval endpoints`
  - `test(po): add testcontainers integration test for receiving workflow`

---

## Architectural Rules & Hard Invariants
1. **Ledger Immutability:** Records in `STOCK_TRANSACTION` and `AUDIT_LOG` must NEVER be updated or deleted. Corrections must be handled by compensatory reversal transactions.
2. **Multi-Location Scoping:** Every inventory, order, transfer, and scanning operation must be explicitly scoped to a `LOCATION_ID` (Store or Warehouse).
3. **Audit Trail:** Every state-altering action must record `created_by`, `created_at`, `updated_by`, `updated_at`, client IP, and previous/new state snapshot.
4. **Offline Resilience for Scanners:** The scanning architecture must support client-side scan buffering and idempotency keys to tolerate intermittent network drops.
