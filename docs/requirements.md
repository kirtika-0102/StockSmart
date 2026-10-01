# StockSmart Requirements Document

## 1. Project overview

StockSmart is a **B.Tech full-stack academic project**: a multi-location retail inventory management system with Angular, Spring Boot, Oracle, REST APIs, authentication, inventory workflows, barcode lookup, and KPI dashboards.

## 2. Problem statement

Small retail operations need a single system to track products, stock by store/warehouse, suppliers, purchase orders, and sales — with clear roles for staff and managers. The project demonstrates how a layered web application solves this without enterprise complexity.

## 3. Objectives

- Implement CRUD and workflows across catalog, inventory, POs, orders, and transfers.
- Demonstrate **Spring Security** with JWT and **role-based** access.
- Persist data in **Oracle** via **JPA/Hibernate**.
- Provide a **demonstrable UI** and **KPI dashboard** computed from live data.
- Document architecture and flows with **Mermaid** diagrams aligned to implementation.

## 4. Scope

### In scope

- Products, categories, suppliers, locations
- Inventory quantity per location; stock in, out, adjustment, transfer
- Purchase orders (receive → increase stock)
- Sales orders (confirm → decrease stock)
- Reorder level and low-stock alerts
- Barcode on product; search by barcode (manual / wedge input)
- Users and roles: ADMIN, INVENTORY_MANAGER, STAFF
- Dashboard KPIs and basic reports

### Out of scope

- E-commerce storefront, payments, shipping carriers
- HR/payroll, full accounting ERP integration
- Real RFID hardware, GS1/EPCIS compliance
- Microservices, message queues, distributed caching
- Double-entry immutable ledger, optimistic locking, offline scan sync

## 5. Target users

- **Administrator** — users and configuration
- **Inventory manager** — stock, POs, transfers, reports
- **Staff** — lookup products, orders, basic inventory views

## 6. Functional requirements

### Product catalog

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-001 | CRUD products (SKU, name, price/cost, category, status) | Must |
| FR-002 | CRUD categories (optional parent) | Must |
| FR-003 | Store **barcode** on product; unique when present | Must |
| FR-004 | Search product by barcode or SKU | Must |

### Suppliers

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-005 | CRUD suppliers | Must |
| FR-006 | Link suppliers to products with unit cost | Should |

### Locations

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-007 | CRUD locations (store/warehouse) | Must |
| FR-008 | Scope inventory and orders to a location | Must |

### Inventory

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-009 | View quantity per product per location | Must |
| FR-010 | Stock in / stock out / adjustment with reason | Must |
| FR-011 | Set reorder level; show low/out of stock | Must |
| FR-012 | Optional history via inventory transaction records | Should |
| FR-013 | Transfers between locations | Must |

### Purchase orders

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-014 | Create PO with line items | Must |
| FR-015 | Receive PO → increase inventory at destination | Must |
| FR-016 | PO statuses: draft, submitted, partial, received, cancelled | Must |

### Sales orders

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-017 | Create order with line items | Must |
| FR-018 | Confirm/complete order → decrease inventory | Must |
| FR-019 | Reject order if insufficient stock | Must |

### Barcode

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-020 | Manual barcode entry on forms | Must |
| FR-021 | Fast input field for keyboard-wedge scanners | Should |
| FR-022 | Camera scan via browser | Could (future) |

### RFID

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-023 | Document future RFID integration; optional placeholder field | Must (doc only) |

### Alerts & dashboard

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-024 | Low-stock and out-of-stock alerts | Must |
| FR-025 | Dashboard KPIs (see kpi-reporting.md) | Must |
| FR-026 | Basic report views / export | Should |

### Security

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-027 | Login; JWT for API calls | Must |
| FR-028 | Roles ADMIN, INVENTORY_MANAGER, STAFF | Must |
| FR-029 | Admin user management | Must |
| FR-030 | Angular AuthInterceptor and route guards | Must |

### Audit

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-031 | created/updated timestamps on entities | Must |
| FR-032 | Optional createdBy on writes | Should |

## 7. Non-functional requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| NFR-001 | Layered monolithic Spring Boot backend | Must |
| NFR-002 | Angular standalone components, Reactive Forms | Must |
| NFR-003 | Oracle + JPA/Hibernate | Must |
| NFR-004 | Passwords hashed (BCrypt) | Must |
| NFR-005 | Responsive UI suitable for demo | Should |
| NFR-006 | Simple JSON error responses from API | Must |

**Removed from enterprise NFRs:** 99.9% accuracy SLA, 1000 concurrent users, Flyway mandatory, MapStruct mandatory, RFC 7807, optimistic locking, offline buffering, hexagonal architecture.

## 8. Business rules

- Inventory decreases must not allow negative quantity (unless team explicitly allows backorder — default: block).
- All stock changes go through **InventoryService** (single place for rules).
- Multi-location: transfers move quantity from source to destination atomically in one transaction.
- Role checks on sensitive APIs (user admin, adjustments, PO receive, etc.).

## 9. MVP (submission target)

Everything in **Must** rows above plus dashboard KPIs and one demo data set.

## 10. Future enhancements

See [PROJECT.md](../PROJECT.md): RFID hardware, GS1, immutable ledger, Flyway, MapStruct, RFC 7807, refresh tokens, granular permissions, offline scans, ML forecasting.
