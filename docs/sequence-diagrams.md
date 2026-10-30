# StockSmart Sequence Diagrams

Key interactions for the **layered monolith** design (Controller → Service → Repository → Oracle).

---

## 1. Login

```mermaid
sequenceDiagram
    participant U as User
    participant A as Angular
    participant C as AuthController
    participant S as AuthService
    participant R as UserRepository

    U->>A: Submit login
    A->>C: POST /api/auth/login
    C->>S: authenticate
    S->>R: findByUsername
    R-->>S: User + roles
    S-->>C: JWT
    C-->>A: accessToken JWT
    A-->>U: Store sessionStorage, navigate home
```

Logout is **client-only** (clear `access_token` / `auth_user`). There is no `POST /api/auth/refresh` or `POST /api/auth/logout`.

---

## 2. Admin user management

Implemented: `UserController` (`/api/users`) is **ADMIN-only**. Role is set on create/update (one role per user). `DELETE` deactivates the account; it does not hard-delete the row.

```mermaid
sequenceDiagram
    participant A as Angular Users page
    participant C as UserController
    participant S as UserService
    participant R as UserRepository

    A->>C: GET /api/users (Bearer JWT)
    C->>S: list()
    S->>R: findAll
    R-->>S: users
    S-->>C: UserResponse list
    C-->>A: 200

    A->>C: POST /api/users
    C->>S: create(dto)
    S->>S: unique username/email, BCrypt, assign Role
    S->>R: save
    R-->>S: User
    S-->>C: UserResponse
    C-->>A: 201 Created
```

---

## 3. Category CRUD

Implemented: `CategoryController` (`/api/categories`). Read: any authenticated role. Write: `ADMIN` or `INVENTORY_MANAGER`.

```mermaid
sequenceDiagram
    participant A as Angular Categories page
    participant C as CategoryController
    participant S as CategoryService
    participant R as CategoryRepository

    A->>C: GET /api/categories
    C->>S: list()
    S->>R: findAllByOrderByNameAsc
    R-->>S: categories
    S-->>C: CategoryResponse list
    C-->>A: 200

    A->>C: POST /api/categories
    C->>S: create(dto)
    S->>S: unique name, optional parent
    S->>R: save
    C-->>A: 201 Created
```

---

## 4. Create product

```mermaid
sequenceDiagram
    participant A as Angular
    participant C as ProductController
    participant S as ProductService
    participant R as ProductRepository

    A->>C: POST /api/products (DTO)
    C->>S: create(dto)
    S->>S: map DTO to entity
    S->>R: save
    R-->>S: Product
    S-->>C: response DTO
    C-->>A: 201 Created
```

---

## 5. Receive purchase order

```mermaid
sequenceDiagram
    participant A as Angular
    participant C as PurchaseOrderController
    participant PO as PurchaseOrderService
    participant Inv as InventoryService
    participant DB as Oracle

    A->>C: POST .../receive
    C->>PO: receive(id, lines)
    loop each line
        PO->>Inv: stockIn(product, location, qty, PO ref)
        Inv->>DB: update INVENTORY
        opt history
            Inv->>DB: insert INVENTORY_TRANSACTION
        end
    end
    PO->>DB: update PO status
    PO-->>A: 200 OK
```

---

## 6. Confirm sales order

```mermaid
sequenceDiagram
    participant A as Angular
    participant C as OrderController
    participant O as OrderService
    participant Inv as InventoryService

    A->>C: POST .../confirm
    C->>O: confirm(orderId)
    O->>Inv: validate and stockOut lines
    alt insufficient stock
        Inv-->>O: exception
        O-->>A: 400
    else ok
        O->>O: status COMPLETED
        O-->>A: 200
    end
```

---

## 7. Stock transfer

```mermaid
sequenceDiagram
    participant C as TransferController
    participant T as TransferService
    participant Inv as InventoryService

    C->>T: completeTransfer(id)
    T->>Inv: transferOut(source, items)
    T->>Inv: transferIn(dest, items)
    Inv-->>T: ok
    T-->>C: response
```

---

## 8. Barcode lookup

```mermaid
sequenceDiagram
    participant A as Angular
    participant C as ProductController
    participant S as ProductService
    participant R as ProductRepository

    A->>C: GET /api/products/by-barcode/{code}
    C->>S: findByBarcode
    S->>R: findByBarcode
    R-->>S: Product
    S-->>A: ProductResponse
```

---

## 9. Dashboard KPI

```mermaid
sequenceDiagram
    participant A as Angular
    participant C as DashboardController
    participant S as DashboardService
    participant R as Repositories

    A->>C: GET /api/dashboard/summary
    C->>S: aggregate()
    S->>R: count/sum queries
    R-->>S: metrics
    S-->>A: KpiResponse
```

---

## Removed from enterprise sequence doc

- InventoryLedgerService, MapStruct mapper step, OptimisticLockException retry, GS1 parse step, EPCIS ingestion, RFC 7807 envelope details
