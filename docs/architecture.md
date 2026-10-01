# StockSmart Architecture Document

## 1. Architectural Style

StockSmart uses a **simple monolithic layered architecture** suitable for a B.Tech demonstration project:

**Angular → REST API → Controller → Service → Repository → JPA/Hibernate → Oracle**

There is no microservice split, no hexagonal ports/adapters layer, and no event bus.

## 2. Major Layers

### Presentation — Angular SPA

- Standalone components, Reactive Forms, HttpClient
- Pages: login, dashboard, master data, inventory workflows, reports
- JWT attached via `AuthInterceptor`; routes protected by guards

### Presentation — REST controllers

- Spring `@RestController` endpoints
- Jakarta Validation on request DTOs
- Returns response DTOs (not entities)

### Service layer

- Business rules: stock checks, order confirmation, PO receiving, transfers
- Transaction boundaries (`@Transactional`)
- Manual DTO ↔ entity mapping

### Persistence layer

- Spring Data JPA repositories
- JPA entities mapped to Oracle tables

## 3. Error Handling

- `@RestControllerAdvice` maps exceptions to HTTP status and a **simple JSON** body (`message`, `status`, optional `errors[]`)
- No requirement for RFC 7807 Problem Details

## 4. Validation

- **Backend:** Jakarta Validation on DTOs
- **Frontend:** Reactive Form validators

## 5. Logging

- SLF4J / Logback for development and debugging

## 6. Configuration

- Spring profiles: `dev`, (optional) `prod`
- Externalize datasource URL, username, password, JWT secret via environment or `application-dev.yml`

## 7. Security

- Stateless **JWT** after login
- **Role-based** access: `ADMIN`, `INVENTORY_MANAGER`, `STAFF`
- `@PreAuthorize` on sensitive endpoints

## 8. Audit trail (lightweight)

- Standard fields on entities: `createdAt`, `updatedAt`, optional `createdBy` / `updatedBy` via JPA auditing
- Optional `InventoryTransaction` rows for stock movement history
- No separate immutable audit log with full JSON snapshots (future enhancement)

## 9. Inventory design

- **`Inventory`:** one row per product per location (`quantity`, `reorderLevel`)
- Changes via service methods; optional **`InventoryTransaction`** records for reporting
- **Not used:** double-entry ledger, compensating transactions, `@Version` optimistic locking

## 10. Barcode & RFID

- **Barcode:** stored on `Product`; lookup by barcode API
- **RFID:** documented as future hardware integration only

## Diagrams

### 1. High-Level System Architecture

```mermaid
flowchart LR
    Client["Angular SPA"] <-->|REST JSON| API["Spring Boot"]
    API <-->|JPA| DB[("Oracle Database")]
```

### 2. Layered Architecture

```mermaid
flowchart TD
    subgraph Presentation
        A1["Angular Frontend"]
        A2["REST Controllers"]
    end
    subgraph Application
        B1["Services"]
        B2["DTOs"]
    end
    subgraph Persistence
        C1["JPA Entities"]
        C2["Spring Data Repositories"]
    end
    A1 --> A2
    A2 --> B1
    B1 --> B2
    B1 --> C2
    C2 --> C1
    C1 --> DB[("Oracle DB")]
```

### 3. Component Diagram

```mermaid
flowchart TD
    subgraph StockSmart
        UI["Angular UI"]
        Auth["JWT Security"]
        Inv["Inventory Service"]
        Cat["Catalog & Suppliers"]
        PO["Purchase Orders"]
        SO["Sales Orders"]
        KPI["Dashboard / KPIs"]
        Data["JPA Repositories"]
    end
    UI --> Auth
    UI --> Inv
    UI --> Cat
    UI --> PO
    UI --> SO
    UI --> KPI
    Inv --> Data
    Cat --> Data
    PO --> Inv
    SO --> Inv
    KPI --> Data
```

### 4. Request Flow (example: stock adjustment)

```mermaid
sequenceDiagram
    participant Client as Angular
    participant Controller as REST Controller
    participant Service as InventoryService
    participant Repo as InventoryRepository
    participant DB as Oracle

    Client->>Controller: POST /api/inventory/adjust
    Controller->>Service: adjustStock(dto)
    Service->>Repo: findByProductAndLocation
    Repo->>DB: SELECT
    DB-->>Repo: Inventory row
    Service->>Service: Update quantity + optional transaction row
    Service->>Repo: save(inventory)
    Repo->>DB: UPDATE
    Service-->>Controller: Response DTO
    Controller-->>Client: 200 OK
```

### 5. Security Flow

```mermaid
sequenceDiagram
    participant User
    participant Angular as Angular
    participant Auth as AuthController
    participant Sec as Spring Security
    participant DB as Oracle

    User->>Angular: Credentials
    Angular->>Auth: POST /api/auth/login
    Auth->>Sec: Authenticate
    Sec->>DB: Load user and roles
    DB-->>Sec: User
    Sec-->>Auth: OK
    Auth-->>Angular: JWT
    Angular->>Sec: API call + Bearer token
    Sec-->>Angular: Authorized response
```

### 6. Inventory Data Model (conceptual)

```mermaid
erDiagram
    LOCATION ||--o{ INVENTORY : holds
    PRODUCT ||--o{ INVENTORY : stocked_as
    PRODUCT ||--o{ INVENTORY_TRANSACTION : optional_history
    LOCATION ||--o{ INVENTORY_TRANSACTION : at

    INVENTORY {
        Long id PK
        Long product_id FK
        Long location_id FK
        Integer quantity
        Integer reorder_level
    }

    INVENTORY_TRANSACTION {
        Long id PK
        Long product_id FK
        Long location_id FK
        String transaction_type
        Integer quantity_change
        String reference
        DateTime created_at
    }
```
