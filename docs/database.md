# StockSmart Database Design Document

This document outlines the Oracle Database schema design for the StockSmart Retail Inventory Optimization & Management System. The design adheres strictly to the architectural decisions outlined in `CLAUDE.md`, focusing on a double-entry stock ledger, GS1/EPCIS compliance, and robust multi-location inventory control.

## Architectural Principles Enforced in Database Design
1. **Double-Entry Stock Ledger**: Inventory is never updated via in-place raw updates. Every movement creates an immutable `STOCK_TRANSACTIONS` record.
2. **Optimistic Concurrency Control**: `INVENTORY_BALANCE` uses a `VERSION` column to prevent race conditions during concurrent modifications.
3. **Immutability**: `STOCK_TRANSACTIONS` and `AUDIT_LOG` records are insert-only and never updated or deleted. Corrections are handled via compensatory transactions.
4. **Multi-Location Scoping**: All operations (inventory, scanning, transfers) are strictly scoped to a `LOCATION_ID`.
5. **Oracle Specifics**: 
   - Primary keys use `NUMBER` with sequences.
   - Identifier lengths are kept within reasonable boundaries (historically 30 chars, though modern Oracle supports up to 128, we use conservative concise naming).
   - Timestamp columns utilize `TIMESTAMP WITH TIME ZONE`.
   - Audit trail state snapshots are stored in `CLOB` as JSON.

---

## 1. User Access & Security Domain

### Entity Relationship Diagram
```mermaid
erDiagram
    USERS ||--o{ USER_ROLES : "has"
    ROLES ||--o{ USER_ROLES : "belongs to"
    ROLES ||--o{ ROLE_PERMISSIONS : "grants"
    PERMISSIONS ||--o{ ROLE_PERMISSIONS : "included in"

    USERS {
        NUMBER id PK
        VARCHAR2 username
        VARCHAR2 password_hash
        VARCHAR2 email
        VARCHAR2 first_name
        VARCHAR2 last_name
        NUMBER is_active
    }
    ROLES {
        NUMBER id PK
        VARCHAR2 name
        VARCHAR2 description
    }
    PERMISSIONS {
        NUMBER id PK
        VARCHAR2 name
        VARCHAR2 description
    }
    USER_ROLES {
        NUMBER user_id PK, FK
        NUMBER role_id PK, FK
    }
    ROLE_PERMISSIONS {
        NUMBER role_id PK, FK
        NUMBER permission_id PK, FK
    }
```

### Table Definitions
- **USERS**: Stores system users.
  - `id` (NUMBER, PK): Sequence-generated.
  - `username` (VARCHAR2(50), UNIQUE, NOT NULL).
  - `password_hash` (VARCHAR2(255), NOT NULL).
  - `email` (VARCHAR2(255), UNIQUE, NOT NULL).
  - `first_name` (VARCHAR2(100), NOT NULL), `last_name` (VARCHAR2(100), NOT NULL).
  - `is_active` (NUMBER(1), DEFAULT 1).
  - Standard audit columns: `created_at`, `created_by`, `updated_at`, `updated_by`.
- **ROLES**: System roles (ADMIN, INVENTORY_MANAGER, STORE_MANAGER, etc.).
  - `id` (NUMBER, PK).
  - `name` (VARCHAR2(50), UNIQUE, NOT NULL).
  - Standard audit columns.
- **PERMISSIONS**: Granular permissions.
  - `id` (NUMBER, PK).
  - `name` (VARCHAR2(100), UNIQUE, NOT NULL).
  - Standard audit columns.
- **ROLE_PERMISSIONS** / **USER_ROLES**: Join tables for many-to-many relationships.

---

## 2. Product Catalog Domain

### Entity Relationship Diagram
```mermaid
erDiagram
    CATEGORIES ||--o{ CATEGORIES : "parent"
    CATEGORIES ||--o{ PRODUCTS : "categorizes"
    BRANDS ||--o{ PRODUCTS : "owns"
    PRODUCTS ||--o{ PRODUCT_IDENTIFIERS : "identified by"

    PRODUCTS {
        NUMBER id PK
        VARCHAR2 sku
        VARCHAR2 name
        NUMBER category_id FK
        NUMBER brand_id FK
        NUMBER base_price
        VARCHAR2 status
    }
    CATEGORIES {
        NUMBER id PK
        VARCHAR2 name
        NUMBER parent_id FK
    }
    BRANDS {
        NUMBER id PK
        VARCHAR2 name
    }
    PRODUCT_IDENTIFIERS {
        NUMBER id PK
        NUMBER product_id FK
        VARCHAR2 identifier_type
        VARCHAR2 identifier_value
    }
```

### Table Definitions
- **PRODUCTS**: Product catalog.
  - `id` (NUMBER, PK).
  - `sku` (VARCHAR2(100), UNIQUE, NOT NULL).
  - `name` (VARCHAR2(255), NOT NULL).
  - `category_id` (NUMBER, FK to CATEGORIES, Nullable).
  - `brand_id` (NUMBER, FK to BRANDS, Nullable).
  - `base_price` (NUMBER(19,4), NOT NULL).
  - `status` (VARCHAR2(20), NOT NULL) - e.g., ACTIVE, INACTIVE, DISCONTINUED.
  - Standard audit columns.
- **CATEGORIES**: Hierarchical categories.
  - `id` (NUMBER, PK).
  - `name` (VARCHAR2(100), NOT NULL).
  - `parent_id` (NUMBER, FK to CATEGORIES.id).
  - Standard audit columns.
- **BRANDS**: Product brands.
  - `id` (NUMBER, PK), `name` (VARCHAR2(100), UNIQUE, NOT NULL).
- **PRODUCT_IDENTIFIERS**: Polymorphic identifier for GS1/EPCIS.
  - `id` (NUMBER, PK).
  - `product_id` (NUMBER, FK to PRODUCTS, NOT NULL).
  - `identifier_type` (VARCHAR2(20), NOT NULL) - EAN_13, UPC_A, GS1_128, SGTIN_96.
  - `identifier_value` (VARCHAR2(255), NOT NULL, Indexed).
  - Standard audit columns.

---

## 3. Supply Chain Domain

### Entity Relationship Diagram
```mermaid
erDiagram
    SUPPLIERS ||--o{ SUPPLIER_PRODUCTS : "supplies"
    PRODUCTS ||--o{ SUPPLIER_PRODUCTS : "supplied as"

    SUPPLIERS {
        NUMBER id PK
        VARCHAR2 name
        VARCHAR2 contact_name
        VARCHAR2 email
        VARCHAR2 phone
    }
    SUPPLIER_PRODUCTS {
        NUMBER id PK
        NUMBER supplier_id FK
        NUMBER product_id FK
        VARCHAR2 supplier_sku
        NUMBER unit_cost
        NUMBER lead_time_days
    }
```

### Table Definitions
- **SUPPLIERS**: Supplier directory.
  - `id` (NUMBER, PK), `name` (VARCHAR2(255), NOT NULL), `contact_name` (VARCHAR2(255)).
  - `email` (VARCHAR2(255)), `phone` (VARCHAR2(50)), `address` (VARCHAR2(500)).
- **SUPPLIER_PRODUCTS**: Which suppliers provide which products.
  - `id` (NUMBER, PK).
  - `supplier_id` (NUMBER, FK to SUPPLIERS, NOT NULL).
  - `product_id` (NUMBER, FK to PRODUCTS, NOT NULL).
  - `supplier_sku` (VARCHAR2(100)).
  - `unit_cost` (NUMBER(19,4), NOT NULL).
  - `lead_time_days` (NUMBER(4), NOT NULL).
  - Constraint: UNIQUE(supplier_id, product_id).

---

## 4. Location & Inventory Ledger Domain (Core Architecture)

### Entity Relationship Diagram
```mermaid
erDiagram
    LOCATIONS ||--o{ INVENTORY_BALANCE : "holds"
    LOCATIONS ||--o{ STOCK_TRANSACTIONS : "records at"
    PRODUCTS ||--o{ INVENTORY_BALANCE : "stocked as"
    PRODUCTS ||--o{ STOCK_TRANSACTIONS : "moved via"

    LOCATIONS {
        NUMBER id PK
        VARCHAR2 name
        VARCHAR2 location_type
    }
    INVENTORY_BALANCE {
        NUMBER id PK
        NUMBER product_id FK
        NUMBER location_id FK
        NUMBER quantity_on_hand
        NUMBER quantity_reserved
        NUMBER version
    }
    STOCK_TRANSACTIONS {
        NUMBER id PK
        NUMBER product_id FK
        NUMBER location_id FK
        VARCHAR2 transaction_type
        NUMBER quantity
        VARCHAR2 reference_id
        VARCHAR2 reference_type
        TIMESTAMP transaction_date
    }
```

### Table Definitions
- **LOCATIONS**: Stores and warehouses.
  - `id` (NUMBER, PK).
  - `name` (VARCHAR2(255), NOT NULL).
  - `location_type` (VARCHAR2(20), NOT NULL) - STORE, WAREHOUSE.
  - `manager_id` (NUMBER, FK to USERS), `is_active` (NUMBER(1)).
- **INVENTORY_BALANCE**: Current cached stock levels with optimistic concurrency.
  - `id` (NUMBER, PK).
  - `product_id` (NUMBER, FK, NOT NULL), `location_id` (NUMBER, FK, NOT NULL).
  - `quantity_on_hand` (NUMBER, NOT NULL, DEFAULT 0).
  - `quantity_reserved` (NUMBER, NOT NULL, DEFAULT 0).
  - `version` (NUMBER, NOT NULL, DEFAULT 0) - **Crucial for JPA @Version**.
  - Constraint: UNIQUE(product_id, location_id).
- **STOCK_TRANSACTIONS**: **Immutable** ledger entries for double-entry bookkeeping.
  - `id` (NUMBER, PK).
  - `product_id` (NUMBER, FK, NOT NULL).
  - `location_id` (NUMBER, FK, NOT NULL).
  - `transaction_type` (VARCHAR2(30), NOT NULL) - RECEIVE, ADJUST_IN, ADJUST_OUT, TRANSFER_OUT, TRANSFER_IN, SALE, RETURN, CORRECTION.
  - `quantity` (NUMBER, NOT NULL) - Positive for IN, Negative for OUT.
  - `reference_id` (VARCHAR2(100)) - Link to PO, Order, Transfer.
  - `reference_type` (VARCHAR2(50)) - PO, ORDER, TRANSFER, MANUAL.
  - `transaction_date` (TIMESTAMP WITH TIME ZONE, NOT NULL).
  - `user_id` (NUMBER, FK to USERS).
  - `notes` (VARCHAR2(500)).
  - *No update_at/updated_by columns needed as records are immutable.*

---

## 5. Stock Transfers Domain

### Entity Relationship Diagram
```mermaid
erDiagram
    LOCATIONS ||--o{ STOCK_TRANSFERS : "source / dest"
    STOCK_TRANSFERS ||--|{ STOCK_TRANSFER_ITEMS : "contains"
    PRODUCTS ||--o{ STOCK_TRANSFER_ITEMS : "transfers"

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
        NUMBER quantity_requested
        NUMBER quantity_shipped
        NUMBER quantity_received
    }
```

### Table Definitions
- **STOCK_TRANSFERS**: Transfer headers.
  - `id` (NUMBER, PK).
  - `source_location_id` (NUMBER, FK to LOCATIONS, NOT NULL).
  - `dest_location_id` (NUMBER, FK to LOCATIONS, NOT NULL).
  - `status` (VARCHAR2(30), NOT NULL) - PENDING, SHIPPED, RECEIVED, CANCELLED.
  - Date/User tracking for requested, shipped, and received actions.
- **STOCK_TRANSFER_ITEMS**: Transfer lines.
  - `id` (NUMBER, PK), `transfer_id` (NUMBER, FK, NOT NULL).
  - `product_id` (NUMBER, FK, NOT NULL).
  - `quantity_requested`, `quantity_shipped`, `quantity_received` (NUMBER).

---

## 6. Purchasing Domain (Purchase Orders)

### Entity Relationship Diagram
```mermaid
erDiagram
    SUPPLIERS ||--o{ PURCHASE_ORDERS : "receives"
    LOCATIONS ||--o{ PURCHASE_ORDERS : "destination"
    PURCHASE_ORDERS ||--|{ PURCHASE_ORDER_ITEMS : "contains"
    PRODUCTS ||--o{ PURCHASE_ORDER_ITEMS : "orders"

    PURCHASE_ORDERS {
        NUMBER id PK
        NUMBER supplier_id FK
        NUMBER dest_location_id FK
        VARCHAR2 status
    }
    PURCHASE_ORDER_ITEMS {
        NUMBER id PK
        NUMBER po_id FK
        NUMBER product_id FK
        NUMBER quantity_ordered
        NUMBER quantity_received
        NUMBER unit_cost
    }
```

### Table Definitions
- **PURCHASE_ORDERS**: Header.
  - `id` (NUMBER, PK).
  - `supplier_id` (NUMBER, FK), `destination_location_id` (NUMBER, FK).
  - `status` (VARCHAR2(30)) - DRAFT, SUBMITTED, PARTIAL, FULFILLED, CANCELLED.
  - `order_date`, `expected_date`.
- **PURCHASE_ORDER_ITEMS**: Lines.
  - `id` (NUMBER, PK), `po_id` (NUMBER, FK, NOT NULL), `product_id` (NUMBER, FK, NOT NULL).
  - `quantity_ordered` (NUMBER), `quantity_received` (NUMBER), `unit_cost` (NUMBER(19,4)).

---

## 7. Orders Domain (Sales)

### Entity Relationship Diagram
```mermaid
erDiagram
    LOCATIONS ||--o{ ORDERS : "fulfilled at"
    ORDERS ||--|{ ORDER_ITEMS : "contains"
    PRODUCTS ||--o{ ORDER_ITEMS : "sold"

    ORDERS {
        NUMBER id PK
        NUMBER location_id FK
        TIMESTAMP order_date
        VARCHAR2 status
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

### Table Definitions
- **ORDERS**: Customer sales order header.
  - `id` (NUMBER, PK), `location_id` (NUMBER, FK, NOT NULL).
  - `customer_id` (NUMBER, Nullable).
  - `order_date` (TIMESTAMP WITH TIME ZONE), `status` (VARCHAR2(30)).
  - `total_amount` (NUMBER(19,4)).
- **ORDER_ITEMS**: Sales lines.
  - `id` (NUMBER, PK), `order_id` (NUMBER, FK, NOT NULL), `product_id` (NUMBER, FK, NOT NULL).
  - `quantity` (NUMBER), `unit_price` (NUMBER(19,4)), `line_total` (NUMBER(19,4)).

---

## 8. Alerts & Reorder Rules Domain

### Entity Relationship Diagram
```mermaid
erDiagram
    PRODUCTS ||--o{ REORDER_RULES : "configured for"
    LOCATIONS ||--o{ REORDER_RULES : "applies at"
    PRODUCTS ||--o{ ALERTS : "triggers"
    LOCATIONS ||--o{ ALERTS : "alerts at"

    REORDER_RULES {
        NUMBER id PK
        NUMBER product_id FK
        NUMBER location_id FK
        NUMBER reorder_point
        NUMBER reorder_quantity
    }
    ALERTS {
        NUMBER id PK
        NUMBER location_id FK
        NUMBER product_id FK
        VARCHAR2 alert_type
        VARCHAR2 status
    }
```

### Table Definitions
- **REORDER_RULES**: Thresholds for automated alerts.
  - `id` (NUMBER, PK), `product_id` (NUMBER, FK), `location_id` (NUMBER, FK).
  - `reorder_point` (NUMBER), `reorder_quantity` (NUMBER), `is_active` (NUMBER(1)).
- **ALERTS**: System-generated alerts.
  - `id` (NUMBER, PK), `location_id` (NUMBER, FK), `product_id` (NUMBER, FK).
  - `alert_type` (VARCHAR2(30)) - LOW_STOCK, OUT_OF_STOCK.
  - `message` (VARCHAR2(500)).
  - `status` (VARCHAR2(30)) - UNREAD, ACKNOWLEDGED, RESOLVED.

---

## 9. System Audit Logging

### Entity Relationship Diagram
```mermaid
erDiagram
    USERS ||--o{ AUDIT_LOG : "performs"

    AUDIT_LOG {
        NUMBER id PK
        VARCHAR2 entity_name
        VARCHAR2 entity_id
        VARCHAR2 action
        NUMBER user_id FK
        TIMESTAMP action_timestamp
        VARCHAR2 client_ip
        CLOB old_state
        CLOB new_state
    }
```

### Table Definitions
- **AUDIT_LOG**: **Immutable** system-wide audit trail.
  - `id` (NUMBER, PK).
  - `entity_name` (VARCHAR2(100), NOT NULL).
  - `entity_id` (VARCHAR2(100), NOT NULL).
  - `action` (VARCHAR2(30), NOT NULL) - INSERT, UPDATE, DELETE.
  - `user_id` (NUMBER, FK to USERS).
  - `action_timestamp` (TIMESTAMP WITH TIME ZONE, NOT NULL).
  - `client_ip` (VARCHAR2(45)).
  - `old_state` (CLOB) - JSON snapshot.
  - `new_state` (CLOB) - JSON snapshot.

---

## 10. Complete Database ER Overview (Simplified)

```mermaid
erDiagram
    USERS ||--o{ USER_ROLES : has
    ROLES ||--o{ USER_ROLES : belongs_to
    ROLES ||--o{ ROLE_PERMISSIONS : grants
    PERMISSIONS ||--o{ ROLE_PERMISSIONS : included_in
    
    PRODUCTS ||--o{ PRODUCT_IDENTIFIERS : has_identifiers
    CATEGORIES ||--o{ PRODUCTS : categorizes
    BRANDS ||--o{ PRODUCTS : owns
    
    SUPPLIERS ||--o{ SUPPLIER_PRODUCTS : supplies
    PRODUCTS ||--o{ SUPPLIER_PRODUCTS : supplied_as
    
    LOCATIONS ||--o{ INVENTORY_BALANCE : holds
    PRODUCTS ||--o{ INVENTORY_BALANCE : stocked_as
    
    LOCATIONS ||--o{ STOCK_TRANSACTIONS : occurs_at
    PRODUCTS ||--o{ STOCK_TRANSACTIONS : moved_via
    USERS ||--o{ STOCK_TRANSACTIONS : executed_by
    
    LOCATIONS ||--o{ STOCK_TRANSFERS : source_or_dest
    STOCK_TRANSFERS ||--|{ STOCK_TRANSFER_ITEMS : contains
    PRODUCTS ||--o{ STOCK_TRANSFER_ITEMS : transferred
    
    SUPPLIERS ||--o{ PURCHASE_ORDERS : receives
    LOCATIONS ||--o{ PURCHASE_ORDERS : destination
    PURCHASE_ORDERS ||--|{ PURCHASE_ORDER_ITEMS : contains
    PRODUCTS ||--o{ PURCHASE_ORDER_ITEMS : ordered
    
    LOCATIONS ||--o{ ORDERS : fulfilled_at
    ORDERS ||--|{ ORDER_ITEMS : contains
    PRODUCTS ||--o{ ORDER_ITEMS : sold
```

## Standard Audit Columns Pattern
Except for immutable tables (`STOCK_TRANSACTIONS`, `AUDIT_LOG`), all entities include the following standard tracking columns:
- `created_at` (TIMESTAMP WITH TIME ZONE, NOT NULL)
- `created_by` (NUMBER, FK to USERS, NOT NULL)
- `updated_at` (TIMESTAMP WITH TIME ZONE, NOT NULL)
- `updated_by` (NUMBER, FK to USERS, NOT NULL)

## Indexing Strategy Notes
- **Primary Indexes**: Auto-generated by Oracle for all `PK` and `UNIQUE` constraints.
- **Foreign Key Indexes**: Every `FK` must have a corresponding index to prevent table locks and improve join performance.
- **Business Lookups**: Indexes required on `PRODUCTS.sku`, `PRODUCT_IDENTIFIERS.identifier_value`, and `STOCK_TRANSACTIONS.transaction_date`.
- **Location-Scoping Indexes**: Compound indexes on `(location_id, product_id)` across `INVENTORY_BALANCE`, `REORDER_RULES`, and `ALERTS` to optimize location-specific dashboard queries.
