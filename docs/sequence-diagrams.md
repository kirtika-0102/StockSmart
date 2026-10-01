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
    C-->>A: token
    A-->>U: Navigate dashboard
```

---

## 2. Create product

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

## 3. Receive purchase order

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

## 4. Confirm sales order

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

## 5. Stock transfer

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

## 6. Barcode lookup

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

## 7. Dashboard KPI

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
