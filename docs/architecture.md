# StockSmart Architecture Document

## 1. Architectural Style
StockSmart follows a **Hexagonal / Layered Architecture**. The primary objective is to enforce strict separation of concerns, isolating core domain logic from external concerns like the presentation layer and data access layer.

## 2. Major Layers

### Presentation Layer (Angular SPA & REST Controllers)
- **Angular SPA:** Uses Standalone Components, Reactive Forms, and Signals/RxJS for state management. It is divided into Smart (container) and Presentational (UI) components.
- **REST API:** Spring Boot `@RestController`s serve as the primary entry point for client requests, validating incoming payload using Jakarta Validation and mapping them to DTOs.

### Application Layer (Services, DTOs, Mappers)
- Acts as the orchestrator for business use cases.
- Employs **MapStruct** to convert between Domain Entities and request/response DTOs (ensuring JPA Entities are never exposed in the REST layer).
- Manages transaction boundaries (e.g., `@Transactional`).

### Domain Layer (Entities, Business Rules, Ledger Engine)
- The heart of the system.
- Contains the core **Double-Entry Stock Ledger Engine**, which ensures that inventory is never mutated in place.
- Defines JPA Entities with no Lombok `@Data` annotations to prevent infinite recursion and lazy-loading issues. Uses explicit `@Getter`, `@Setter`, and business keys for `equals`/`hashCode`.

### Infrastructure Layer (Repositories, Oracle Adapters, External Integrators)
- **Repositories:** Spring Data JPA interfaces for database access.
- **Database:** Oracle Database (19c/21c/23ai) using Flyway for schema migrations.
- **Integrations:** Handles integration with GS1/EPCIS barcode and RFID scanners.

## 3. Communication Between Layers
- **Dependency Direction:** Dependencies point inwards. Presentation depends on Application; Application depends on Domain and Infrastructure abstractions.
- **Interfaces:** Services typically implement interfaces defined in the domain layer, promoting loose coupling.

## 4. Error Handling
- Follows **RFC 7807 Problem Details** for HTTP APIs.
- Global exception handling is implemented via `@RestControllerAdvice`, converting business exceptions into standard JSON envelopes.

## 5. Validation
- **Backend:** Jakarta Validation (`hibernate-validator`) on incoming DTOs.
- **Frontend:** Angular Reactive Forms with strictly-typed models.

## 6. Logging
- Uses SLF4J and Logback for structured logging.
- Audit logs capture `created_by`, `updated_by`, IP, and state changes.

## 7. Configuration
- Externalized configuration using Spring Profiles (`dev`, `test`, `prod`).
- Application properties and secrets are managed outside the codebase.

## 8. Security Boundaries
- **Authentication:** Stateless JWT Authentication via Spring Security 6.x.
- **Authorization:** Granular RBAC (Role-Based Access Control).
- API endpoints are protected; tokens are centrally handled in Angular via `AuthInterceptor`.

## 9. Auditability
- **Ledger Immutability:** `STOCK_TRANSACTION` and `AUDIT_LOG` records are NEVER updated or deleted.
- Every state-altering action records timestamp, user, client IP, and snapshots.

## 10. Cross-Cutting Concerns
- **Transaction Management:** `@Transactional(readOnly=true)` by default. Write operations explicitly use `@Transactional(rollbackFor=Exception.class)`.
- **Exception Translation:** Handled by Spring Data and global advice.
- **Caching:** Can be applied at the service layer for read-heavy operations like location lookups.

## 11. Integration Points
- **Barcode & RFID:** Keyboard-wedge event listener service, ZXing/BarcodeDetector for WebRTC camera scanning. RFID support via EPCIS-compatible event ingestion (SGTIN-96).
- **Offline Resilience:** Client-side buffering and idempotency keys to tolerate intermittent network drops for scanners.

## 12. Double-Entry Stock Ledger Architecture
Inventory balances are not mutated via raw updates.
Every change generates an immutable `STOCK_TRANSACTION` that credits/debits stock. The `InventoryLedgerService` writes audit records and ledger transactions before adjusting cached `INVENTORY_BALANCE` rows.

## 13. Optimistic Concurrency Control
- `INVENTORY_BALANCE` entity uses JPA `@Version` to prevent race conditions during concurrent scans and checkout/receiving operations.

## Diagrams

### 1. High-Level System Architecture
```mermaid
flowchart LR
    Client["Angular SPA (Browser / Scanner)"] <-->|REST API / JSON| API["Spring Boot Backend"]
    API <-->|JPA / Hibernate| DB[("Oracle Database (19c/21c/23ai)")]
```

### 2. Layered Architecture
```mermaid
flowchart TD
    subgraph Presentation
    A1["Angular Frontend"]
    A2["Spring REST Controllers"]
    end
    
    subgraph Application
    B1["Business Services"]
    B2["DTOs & MapStruct"]
    end
    
    subgraph Domain
    C1["JPA Entities"]
    C2["Ledger Engine"]
    C3["Business Rules"]
    end
    
    subgraph Infrastructure
    D1["Spring Data Repositories"]
    D2["Flyway Migrations"]
    D3["External Integrations"]
    end
    
    Presentation --> Application
    Application --> Domain
    Application --> Infrastructure
    Infrastructure --> DB[("Oracle DB")]
```

### 3. Component Diagram
```mermaid
flowchart TD
    subgraph "StockSmart Application"
        UI["Angular UI Modules"]
        Auth["Security & JWT"]
        Ledger["Double-Entry Ledger"]
        Inventory["Inventory Management"]
        Scanning["Barcode / RFID Ingestion"]
        Data["Persistence Layer"]
    end
    UI --> Auth
    UI --> Inventory
    UI --> Scanning
    Inventory --> Ledger
    Inventory --> Data
    Ledger --> Data
    Scanning --> Inventory
```

### 4. Request Flow Diagram
```mermaid
sequenceDiagram
    participant Client as Angular Client
    participant Controller as REST Controller
    participant Mapper as MapStruct
    participant Service as Business Service
    participant Repo as Repository
    participant DB as Oracle Database

    Client->>Controller: POST /api/inventory/adjust (DTO)
    Controller->>Service: handleAdjustment(DTO)
    Service->>Mapper: toEntity(DTO)
    Mapper-->>Service: Entity
    Service->>Repo: save(STOCK_TRANSACTION)
    Repo->>DB: INSERT INTO stock_transaction
    Service->>Repo: find(INVENTORY_BALANCE)
    Repo->>DB: SELECT ... FROM inventory_balance
    DB-->>Repo: Balance Record
    Service->>Repo: save(INVENTORY_BALANCE)
    Repo->>DB: UPDATE inventory_balance (Check @Version)
    Service->>Mapper: toDto(Entity)
    Mapper-->>Service: Response DTO
    Service-->>Controller: Response DTO
    Controller-->>Client: 200 OK (JSON)
```

### 5. Security Architecture Diagram
```mermaid
sequenceDiagram
    participant User as User / Scanner
    participant Angular as Angular Client
    participant AuthAPI as Auth Controller
    participant SpringSec as Spring Security
    participant DB as Oracle DB

    User->>Angular: Enter Credentials
    Angular->>AuthAPI: POST /api/auth/login
    AuthAPI->>SpringSec: Authenticate
    SpringSec->>DB: Fetch User & Roles
    DB-->>SpringSec: User Details
    SpringSec-->>AuthAPI: Auth Success
    AuthAPI-->>Angular: JWT Token
    Angular->>Angular: Store Token in AuthInterceptor
    User->>Angular: Access Protected Resource
    Angular->>SpringSec: Request + JWT (Authorization: Bearer)
    SpringSec->>SpringSec: Validate Token Signature & Claims
    SpringSec-->>Angular: Authorized Response
```

### 6. Double-Entry Ledger Architecture
```mermaid
erDiagram
    LOCATION ||--o{ INVENTORY_BALANCE : "has"
    PRODUCT ||--o{ INVENTORY_BALANCE : "has"
    INVENTORY_BALANCE ||--o{ STOCK_TRANSACTION : "adjusted by"
    
    INVENTORY_BALANCE {
        Long id PK
        Long location_id FK
        Long product_id FK
        Integer quantity
        Integer version "JPA @Version"
    }
    
    STOCK_TRANSACTION {
        Long id PK
        Long location_id FK
        Long product_id FK
        Integer quantity_change
        String transaction_type
        Timestamp created_at "Immutable"
        String created_by "Immutable"
    }
```
