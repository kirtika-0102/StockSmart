# Security Architecture — StockSmart

College-level security for the B.Tech project: **JWT authentication** and **simple role-based authorization**.

---

## 1. Authentication

- Users stored in Oracle with BCrypt **password hash**.
- **Login:** `POST /api/auth/login` with username/password.
- **Response:** JWT access token (include username and roles in claims or load roles server-side).
- **Logout:** Client discards token (optional server deny-list not required for MVP).

```mermaid
sequenceDiagram
    participant User
    participant Angular as Angular
    participant API as AuthController
    participant DB as Oracle

    User->>Angular: username / password
    Angular->>API: POST /api/auth/login
    API->>DB: find user, verify password
    DB-->>API: user + roles
    API-->>Angular: JWT
    Angular->>Angular: store token (sessionStorage or memory)
```

### Token storage (Angular)

- **sessionStorage** or in-memory service for access token (document choice in README).
- Send `Authorization: Bearer <token>` on each API call via **AuthInterceptor**.

### Refresh tokens

**Not required for MVP.** Optional future enhancement.

---

## 2. Authorization

### Roles

| Role | Typical access |
|------|----------------|
| `ADMIN` | All modules + user management |
| `INVENTORY_MANAGER` | Inventory, POs, transfers, suppliers, reports |
| `STAFF` | View inventory/products, create/process orders, barcode lookup |

### Enforcement

- Spring Security `@PreAuthorize("hasRole('ADMIN')")` or `hasAnyRole(...)` on controllers/services.
- Angular **route guards** hide menu items and block routes by role.

No separate `PERMISSIONS` table or `PRODUCT_READ`-style authorities required.

```mermaid
sequenceDiagram
    participant Client as Angular
    participant Filter as JWT Filter
    participant API as Controller

    Client->>Filter: Request + Bearer JWT
    Filter->>Filter: Validate signature & expiry
    Filter->>API: Authenticated principal with roles
    alt has role
        API-->>Client: 200 OK
    else missing role
        API-->>Client: 403 Forbidden
    end
```

---

## 3. Password handling

- BCrypt encoding (strength 10+).
- Minimum length validation (e.g. 8 characters) on create/reset.
- Generic message on login failure: "Invalid credentials".

Password reset via email is **out of scope** unless added as extra credit.

---

## 4. API protection

```mermaid
flowchart LR
    Request --> CORS
    CORS --> JWT[JWT Filter]
    JWT --> Auth{Authenticated?}
    Auth -->|No| E401[401]
    Auth -->|Yes| Authz{Authorized?}
    Authz -->|No| E403[403]
    Authz -->|Yes| Controller
```

- CORS allowed for `http://localhost:4200` in development.
- Public endpoints: login, health (optional).

---

## 5. Input validation

| Layer | Mechanism |
|-------|-----------|
| Angular | Reactive Form validators |
| API | Jakarta Validation on DTOs |
| DB | Parameterized JPA queries |

---

## 6. Error responses

Simple JSON (not RFC 7807 required):

```json
{
  "status": 401,
  "message": "Invalid credentials"
}
```

Do not expose stack traces to clients in demo/production profile.

---

## 7. CORS (development)

| Setting | Value |
|---------|--------|
| Allowed origins | `http://localhost:4200` |
| Methods | GET, POST, PUT, PATCH, DELETE, OPTIONS |
| Headers | Authorization, Content-Type |

---

## 8. Configuration secrets

Use environment variables or `application-dev.yml` (not committed with real passwords):

- `SPRING_DATASOURCE_URL`, username, password
- `JWT_SECRET`, `JWT_EXPIRATION_MS`

---

## 9. Audit

- JPA **`@CreatedDate` / `@LastModifiedDate`** on entities.
- Optional **`InventoryTransaction`** for who changed stock.
- Full **AUDIT_LOG** with JSON snapshots → future enhancement.

---

## 10. Security overview diagram

```mermaid
flowchart TD
    Client["Angular"]
    subgraph SpringBoot["Spring Boot"]
        Filter["JWT Filter"]
        Ctrl["Controllers"]
        Svc["Services @PreAuthorize"]
    end
    DB[("Oracle")]

    Client -->|HTTPS optional locally| Filter
    Filter --> Ctrl
    Ctrl --> Svc
    Svc --> DB
```

---

## Future enhancements

- Refresh token rotation, httpOnly cookies + CSRF
- Granular permissions matrix
- Rate limiting, WAF, secrets vault
- Separate Flyway DB user, immutable audit log
