# StockSmart Database Design Document

Oracle schema design for the **B.Tech StockSmart** project. The model is **normalized** and aligned with [PROJECT.md](../PROJECT.md).

## Design principles (college level)

1. **Inventory quantity** is stored on `INVENTORY` and updated by application services (stock in/out, adjust, transfer, PO receive, order confirm).
2. Optional **`INVENTORY_TRANSACTION`** rows provide movement **history** for demos and reports — not an immutable accounting ledger.
3. **Multi-location:** operations reference `LOCATION_ID` where applicable.
4. **Security:** users and roles; **no** separate permissions table for MVP.
5. **Barcode:** column on `PRODUCT` (unique when set).
6. **Schema creation:** JPA `ddl-auto=update` in development **or** a provided SQL script — Flyway is **not** required.

Oracle notes:

- Primary keys: `NUMBER` with sequences (or IDENTITY where supported).
- Timestamps: `TIMESTAMP` or `TIMESTAMP WITH TIME ZONE`.
- Use `@SequenceGenerator` in JPA entities as needed.

---

## 1. User & role domain

```mermaid
erDiagram
    USERS ||--o{ USER_ROLES : has
    ROLES ||--o{ USER_ROLES : assigned

    USERS {
        NUMBER id PK
        VARCHAR2 username UK
        VARCHAR2 password_hash
        VARCHAR2 email
        VARCHAR2 first_name
        VARCHAR2 last_name
        NUMBER is_active
    }
    ROLES {
        NUMBER id PK
        VARCHAR2 name UK
    }
    USER_ROLES {
        NUMBER user_id PK_FK
        NUMBER role_id PK_FK
    }
```

**Roles (seed data):** `ADMIN`, `INVENTORY_MANAGER`, `STAFF`.

Standard audit columns on mutable tables: `created_at`, `updated_at`, optional `created_by`, `updated_by`.

---

## 2. Product catalog

```mermaid
erDiagram
    CATEGORIES ||--o{ CATEGORIES : parent
    CATEGORIES ||--o{ PRODUCTS : categorizes

    CATEGORIES {
        NUMBER id PK
        VARCHAR2 name
        NUMBER parent_id FK
    }
    PRODUCTS {
        NUMBER id PK
        VARCHAR2 sku UK
        VARCHAR2 name
        NUMBER category_id FK
        VARCHAR2 barcode UK
        NUMBER unit_price
        NUMBER cost_price
        NUMBER default_reorder_level
        VARCHAR2 status
    }
```

- **Brand** table omitted for simplicity (optional future add-on).
- **RFID:** optional `rfid_tag` column on `PRODUCT` as placeholder only — no hardware integration.

---

## 3. Suppliers

```mermaid
erDiagram
    SUPPLIERS ||--o{ SUPPLIER_PRODUCTS : supplies
    PRODUCTS ||--o{ SUPPLIER_PRODUCTS : linked

    SUPPLIERS {
        NUMBER id PK
        VARCHAR2 name
        VARCHAR2 contact_name
        VARCHAR2 email
        VARCHAR2 phone
        VARCHAR2 address
    }
    SUPPLIER_PRODUCTS {
        NUMBER id PK
        NUMBER supplier_id FK
        NUMBER product_id FK
        NUMBER unit_cost
    }
```

Unique constraint on `(supplier_id, product_id)`.

---

## 4. Locations & inventory

```mermaid
erDiagram
    LOCATIONS ||--o{ INVENTORY : holds
    PRODUCTS ||--o{ INVENTORY : stocked
    PRODUCTS ||--o{ INVENTORY_TRANSACTIONS : moves
    LOCATIONS ||--o{ INVENTORY_TRANSACTIONS : at

    LOCATIONS {
        NUMBER id PK
        VARCHAR2 name
        VARCHAR2 location_type
    }
    INVENTORY {
        NUMBER id PK
        NUMBER product_id FK
        NUMBER location_id FK
        NUMBER quantity
        NUMBER reorder_level
    }
    INVENTORY_TRANSACTIONS {
        NUMBER id PK
        NUMBER product_id FK
        NUMBER location_id FK
        VARCHAR2 transaction_type
        NUMBER quantity_change
        VARCHAR2 reference_type
        VARCHAR2 reference_id
        TIMESTAMP created_at
        NUMBER user_id FK
    }
```

- **Unique** `(product_id, location_id)` on `INVENTORY`.
- **transaction_type** examples: `STOCK_IN`, `STOCK_OUT`, `ADJUSTMENT`, `TRANSFER_OUT`, `TRANSFER_IN`, `PO_RECEIVE`, `ORDER_FULFILL`.
- No `version` column for optimistic locking in MVP.

---

## 5. Stock transfers

```mermaid
erDiagram
    STOCK_TRANSFERS ||--|{ STOCK_TRANSFER_ITEMS : contains
    LOCATIONS ||--o{ STOCK_TRANSFERS : source_dest
    PRODUCTS ||--o{ STOCK_TRANSFER_ITEMS : lines

    STOCK_TRANSFERS {
        NUMBER id PK
        NUMBER source_location_id FK
        NUMBER dest_location_id FK
        VARCHAR2 status
    }
    STOCK_TRANSFER_ITEMS {
        NUMBER id PK
        NUMBER transfer_id FK
        NUMBER product_id FK
        NUMBER quantity
    }
```

**Status:** `PENDING`, `COMPLETED`, `CANCELLED` (adjust if implementation uses ship/receive steps).

---

## 6. Purchase orders

```mermaid
erDiagram
    SUPPLIERS ||--o{ PURCHASE_ORDERS : from
    LOCATIONS ||--o{ PURCHASE_ORDERS : destination
    PURCHASE_ORDERS ||--|{ PURCHASE_ORDER_ITEMS : lines

    PURCHASE_ORDERS {
        NUMBER id PK
        NUMBER supplier_id FK
        NUMBER location_id FK
        VARCHAR2 status
        DATE order_date
    }
    PURCHASE_ORDER_ITEMS {
        NUMBER id PK
        NUMBER purchase_order_id FK
        NUMBER product_id FK
        NUMBER quantity_ordered
        NUMBER quantity_received
        NUMBER unit_cost
    }
```

**Status:** `DRAFT`, `SUBMITTED`, `PARTIAL`, `RECEIVED`, `CANCELLED`.

Receiving increases `INVENTORY.quantity` at `location_id`.

---

## 7. Sales orders

```mermaid
erDiagram
    LOCATIONS ||--o{ ORDERS : at
    ORDERS ||--|{ ORDER_ITEMS : lines

    ORDERS {
        NUMBER id PK
        NUMBER location_id FK
        VARCHAR2 status
        TIMESTAMP order_date
        NUMBER total_amount
    }
    ORDER_ITEMS {
        NUMBER id PK
        NUMBER order_id FK
        NUMBER product_id FK
        NUMBER quantity
        NUMBER unit_price
    }
```

**Status:** `PENDING`, `CONFIRMED`, `COMPLETED`, `CANCELLED`. Confirm/complete reduces stock.

---

## 8. Alerts

```mermaid
erDiagram
    PRODUCTS ||--o{ ALERTS : about
    LOCATIONS ||--o{ ALERTS : at

    ALERTS {
        NUMBER id PK
        NUMBER product_id FK
        NUMBER location_id FK
        VARCHAR2 alert_type
        VARCHAR2 message
        VARCHAR2 status
    }
```

**alert_type:** `LOW_STOCK`, `OUT_OF_STOCK`. May be generated by scheduled job or on inventory update.

---

## 9. Complete overview

```mermaid
erDiagram
    USERS ||--o{ USER_ROLES : has
    ROLES ||--o{ USER_ROLES : has

    CATEGORIES ||--o{ PRODUCTS : categorizes
    SUPPLIERS ||--o{ SUPPLIER_PRODUCTS : supplies
    PRODUCTS ||--o{ SUPPLIER_PRODUCTS : linked

    LOCATIONS ||--o{ INVENTORY : holds
    PRODUCTS ||--o{ INVENTORY : stocked
    PRODUCTS ||--o{ INVENTORY_TRANSACTIONS : history
    LOCATIONS ||--o{ INVENTORY_TRANSACTIONS : at

    STOCK_TRANSFERS ||--|{ STOCK_TRANSFER_ITEMS : items
    PURCHASE_ORDERS ||--|{ PURCHASE_ORDER_ITEMS : items
    ORDERS ||--|{ ORDER_ITEMS : items

    PRODUCTS ||--o{ ALERTS : triggers
    LOCATIONS ||--o{ ALERTS : at
```

---

## Indexing (recommended)

- `PRODUCTS(sku)`, `PRODUCTS(barcode)`
- `INVENTORY(location_id, product_id)` unique
- Foreign key columns indexed for join performance

---

## Removed from enterprise design

| Removed | Reason |
|---------|--------|
| `PERMISSIONS`, `ROLE_PERMISSIONS` | Simple role-based security |
| `PRODUCT_IDENTIFIERS` | Barcode on product |
| `INVENTORY_BALANCE.version` | No optimistic locking MVP |
| Immutable `STOCK_TRANSACTIONS` ledger rules | Simplified inventory + optional history |
| `AUDIT_LOG` with CLOB snapshots | JPA audit fields sufficient for project |

See **Future enhancements** in [PROJECT.md](../PROJECT.md).
