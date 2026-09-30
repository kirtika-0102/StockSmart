# StockSmart Requirements Document

## 1. Project Overview
StockSmart is an enterprise-grade multi-location retail inventory optimization & management system designed to provide real-time visibility into inventory levels, streamline supply chain operations, and ensure accuracy through a robust double-entry stock ledger. It features barcode scanning, RFID-ready intake, purchase order processing, and analytics dashboards, catering to the complete inventory lifecycle of retail businesses.

## 2. Problem Statement
Retail businesses face continuous challenges with inventory accuracy, stockouts, and overstocking. Traditional inventory systems often suffer from race conditions, lacking proper audit trails for stock adjustments, and inadequate multi-location visibility. Furthermore, intermittent network connectivity in warehouses causes scanner data loss, and hardware like barcode and RFID scanners require seamless integration to reduce manual data entry errors.

## 3. Objectives
- Achieve 99.9% inventory accuracy across all locations.
- Eliminate data loss from intermittent network connections during stock intake and counting.
- Prevent concurrent update race conditions in stock balances.
- Ensure strict traceability and compliance with an immutable audit log and double-entry stock ledger.
- Provide a responsive, accessible user interface for fast operations across diverse devices.

## 4. Scope
### In Scope
- Multi-location (store, warehouse) inventory tracking.
- Product catalog, supplier, and order management.
- Stock ledger, adjustments, receives, and transfers.
- Offline-resilient barcode scanning support.
- User authentication, role-based access control, and audit logs.
- Reporting and KPI dashboards.

### Out of Scope
- E-commerce storefront or customer-facing application.
- Payroll and HR management.
- Deep accounting systems integration (beyond exporting data or API endpoints).
- Fleet routing or physical transportation management.

## 5. Target Users
- Retail Business Owners/Stakeholders
- Warehouse Operators
- Store Personnel
- Procurement and Supply Chain Teams
- System Administrators

## 6. Actors
- **System Administrator:** Manages users, roles, system configurations, and master data.
- **Inventory Manager:** Oversees inventory levels, analyzes KPIs, approves stock adjustments and transfers.
- **Store Manager:** Manages inventory within a specific retail location, creates local POs, and handles receiving.
- **Warehouse Staff:** Performs stock receiving, picking, packing, scanning, and cycle counting.
- **Procurement Manager:** Manages supplier relationships, creates purchase orders, and tracks supply deliveries.
- **Sales/Order Staff:** Initiates stock checkouts, handles customer orders (impacting inventory balances).

## 7. Functional Requirements

### Product Catalog Management
| ID | Requirement | Status |
|---|---|---|
| FR-001 | The system shall allow users to create, read, update, and soft-delete products. | CONFIRMED REQUIREMENT |
| FR-002 | The system shall support categorization of products into hierarchical categories. | CONFIRMED REQUIREMENT |
| FR-003 | The system shall allow users to define and assign brands to products. | CONFIRMED REQUIREMENT |
| FR-004 | The system shall support multiple units of measure (UoM) for products. | CONFIRMED REQUIREMENT |
| FR-005 | The system shall support variant products (e.g., size, color). | ASSUMPTION |

### Supplier Management
| ID | Requirement | Status |
|---|---|---|
| FR-006 | The system shall allow users to maintain a registry of suppliers with contact and banking details. | CONFIRMED REQUIREMENT |
| FR-007 | The system shall track lead times and minimum order quantities per supplier per product. | ASSUMPTION |
| FR-008 | The system shall provide supplier performance metrics (on-time delivery, defect rate). | OPEN QUESTION |

### Store/Location Management
| ID | Requirement | Status |
|---|---|---|
| FR-009 | The system shall support multiple physical and logical locations (stores, warehouses, transit). | CONFIRMED REQUIREMENT |
| FR-010 | The system shall allow assigning operating hours and location-specific settings. | ASSUMPTION |
| FR-011 | The system shall enforce multi-location scoping for all operations (every op tied to a LOCATION_ID). | CONFIRMED REQUIREMENT |

### Warehouse Management
| ID | Requirement | Status |
|---|---|---|
| FR-012 | The system shall support bin/aisle/shelf level location tracking within a warehouse. | ASSUMPTION |
| FR-013 | The system shall provide directed putaway and picking workflows. | OPEN QUESTION |

### Multi-location Inventory Tracking
| ID | Requirement | Status |
|---|---|---|
| FR-014 | The system shall maintain real-time inventory balances per product per location. | CONFIRMED REQUIREMENT |
| FR-015 | The system shall track inventory status (available, reserved, in-transit, damaged). | ASSUMPTION |

### Stock Receiving (Double-entry Ledger)
| ID | Requirement | Status |
|---|---|---|
| FR-016 | The system shall process stock intake against a Purchase Order or Transfer Order. | CONFIRMED REQUIREMENT |
| FR-017 | Every stock receive operation shall generate an immutable `STOCK_TRANSACTION` ledger entry. | CONFIRMED REQUIREMENT |
| FR-018 | The system shall prevent direct in-place updates to stock balances during receiving. | CONFIRMED REQUIREMENT |

### Stock Adjustments (Double-entry Ledger)
| ID | Requirement | Status |
|---|---|---|
| FR-019 | The system shall allow manual stock adjustments with reason codes (e.g., shrinkage, damage, cycle count variance). | CONFIRMED REQUIREMENT |
| FR-020 | All adjustments shall be recorded as compensatory/additive `STOCK_TRANSACTION` entries. | CONFIRMED REQUIREMENT |
| FR-021 | The system shall require Inventory Manager approval for adjustments exceeding a configured threshold. | ASSUMPTION |

### Stock Transfers between Locations
| ID | Requirement | Status |
|---|---|---|
| FR-022 | The system shall support creating transfer requests between locations. | CONFIRMED REQUIREMENT |
| FR-023 | The system shall track stock in-transit during inter-location transfers. | CONFIRMED REQUIREMENT |
| FR-024 | Stock transfers shall create corresponding outbound and inbound `STOCK_TRANSACTION` records. | CONFIRMED REQUIREMENT |

### Purchase Orders
| ID | Requirement | Status |
|---|---|---|
| FR-025 | The system shall allow Procurement Managers to create, approve, and send POs to suppliers. | CONFIRMED REQUIREMENT |
| FR-026 | The system shall track PO statuses (Draft, Sent, Partially Received, Completed, Canceled). | CONFIRMED REQUIREMENT |

### Order Processing
| ID | Requirement | Status |
|---|---|---|
| FR-027 | The system shall process customer sales orders to deduct inventory. | CONFIRMED REQUIREMENT |
| FR-028 | Sales orders shall reserve inventory upon creation and deduct upon fulfillment. | ASSUMPTION |

### Barcode Support (GS1 Standards)
| ID | Requirement | Status |
|---|---|---|
| FR-029 | The system shall support scanning GS1 standard barcodes (EAN-13, UPC-A, GS1-128). | CONFIRMED REQUIREMENT |
| FR-030 | The frontend shall integrate ZXing/BarcodeDetector API for WebRTC camera scanning. | CONFIRMED REQUIREMENT |
| FR-031 | The system shall support keyboard-wedge event listener services for hardware scanners. | CONFIRMED REQUIREMENT |
| FR-032 | The system shall parse GS1-128 application identifiers (batch, expiry, serial). | ASSUMPTION |

### RFID-ready Architecture (EPC Gen2)
| ID | Requirement | Status |
|---|---|---|
| FR-033 | The system architecture shall support EPC Gen2 (SGTIN-96) standards. | CONFIRMED REQUIREMENT |
| FR-034 | The system shall feature an EPCIS-compatible event ingestion pipeline. | CONFIRMED REQUIREMENT |
| FR-035 | The backend shall correctly parse SGTIN-96 bit shifts and map to product catalogs. | CONFIRMED REQUIREMENT |

### Low-stock Alerts & Reorder Management
| ID | Requirement | Status |
|---|---|---|
| FR-036 | The system shall allow setting minimum and maximum stock thresholds per product per location. | CONFIRMED REQUIREMENT |
| FR-037 | The system shall generate low-stock alerts when inventory falls below the minimum threshold. | CONFIRMED REQUIREMENT |
| FR-038 | The system shall automatically suggest reorder quantities based on max threshold and current stock. | CONFIRMED REQUIREMENT |

### KPI Dashboards & Reporting
| ID | Requirement | Status |
|---|---|---|
| FR-039 | The system shall provide a dashboard visualizing stock value, low-stock items, and pending POs. | CONFIRMED REQUIREMENT |
| FR-040 | The system shall generate inventory valuation reports (e.g., FIFO, Weighted Average). | ASSUMPTION |
| FR-041 | The system shall generate stock movement and stockout reports. | CONFIRMED REQUIREMENT |

### User & Role Management
| ID | Requirement | Status |
|---|---|---|
| FR-042 | The system shall allow administrators to manage user accounts (create, update, deactivate). | CONFIRMED REQUIREMENT |
| FR-043 | The system shall support role creation with granular permissions. | CONFIRMED REQUIREMENT |
| FR-044 | The system shall allow assigning users to one or multiple locations. | CONFIRMED REQUIREMENT |

### Authentication & Authorization
| ID | Requirement | Status |
|---|---|---|
| FR-045 | The system shall authenticate users via stateless JWT (JSON Web Tokens). | CONFIRMED REQUIREMENT |
| FR-046 | The system shall enforce Granular Role-Based Access Control (RBAC) at the API level. | CONFIRMED REQUIREMENT |
| FR-047 | The frontend shall use an AuthInterceptor to attach JWT to outgoing requests. | CONFIRMED REQUIREMENT |

### Audit/History Tracking
| ID | Requirement | Status |
|---|---|---|
| FR-048 | Every state-altering action shall record `created_by`, `created_at`, `updated_by`, `updated_at`. | CONFIRMED REQUIREMENT |
| FR-049 | Audit logs shall capture the client IP and previous/new state snapshot of the entity. | CONFIRMED REQUIREMENT |
| FR-050 | `AUDIT_LOG` records must be strictly immutable and never updated or deleted. | CONFIRMED REQUIREMENT |
| FR-051 | The system shall expose audit trails for administrative review in the UI. | CONFIRMED REQUIREMENT |

## 8. Non-Functional Requirements

| ID | Requirement | Status |
|---|---|---|
| NFR-001 | **Performance:** API endpoints must respond within 200ms under normal load for 95th percentile requests. | ASSUMPTION |
| NFR-002 | **Scalability:** The system shall support at least 1,000 concurrent active users. | ASSUMPTION |
| NFR-003 | **Security:** All passwords shall be hashed using bcrypt or Argon2. | ASSUMPTION |
| NFR-004 | **Security:** The system shall use Spring Security 6.x for authentication and authorization. | CONFIRMED REQUIREMENT |
| NFR-005 | **Reliability:** The system shall implement Optimistic Concurrency Control using JPA `@Version` on `INVENTORY_BALANCE` to prevent race conditions. | CONFIRMED REQUIREMENT |
| NFR-006 | **Usability:** The frontend must use responsive Angular standalone components and be fully operable on mobile scanning devices. | CONFIRMED REQUIREMENT |
| NFR-007 | **Maintainability:** The backend must strictly follow Hexagonal/Layered Architecture principles. | CONFIRMED REQUIREMENT |
| NFR-008 | **Maintainability:** JPA Entities must not use Lombok `@Data`, relying instead on explicit getters/setters and business-key `equals`/`hashCode`. | CONFIRMED REQUIREMENT |
| NFR-009 | **Maintainability:** MapStruct must be used for all Entity-to-DTO mapping; Entities must never leak to the REST layer. | CONFIRMED REQUIREMENT |
| NFR-010 | **Oracle Compatibility:** The backend must use `org.hibernate.dialect.OracleDialect` and be fully compatible with Oracle 19c/21c/23ai. | CONFIRMED REQUIREMENT |
| NFR-011 | **Database Migrations:** All database schema changes must be managed via Flyway. | CONFIRMED REQUIREMENT |
| NFR-012 | **Offline Resilience:** The scanning architecture must support client-side scan buffering. | CONFIRMED REQUIREMENT |
| NFR-013 | **Offline Resilience:** Scanner clients must utilize idempotency keys to tolerate intermittent network drops during sync. | CONFIRMED REQUIREMENT |
| NFR-014 | **Error Handling:** The API must return RFC 7807 Problem Details envelopes for all errors. | CONFIRMED REQUIREMENT |
| NFR-015 | **Transactions:** Read operations shall default to `@Transactional(readOnly=true)`; writes must use explicit `@Transactional(rollbackFor=Exception.class)`. | CONFIRMED REQUIREMENT |

## 9. Business Rules
- **Double-Entry Ledger:** Inventory is never mutated through raw in-place updates. Every change in stock creates an immutable `STOCK_TRANSACTION` ledger entry that adjusts location balances.
- **Multi-Location Scoping:** Every inventory, order, transfer, and scanning operation must be explicitly scoped to a `LOCATION_ID` (Store or Warehouse).
- **Ledger Immutability:** Records in `STOCK_TRANSACTION` and `AUDIT_LOG` must NEVER be updated or deleted. Corrections must be handled by compensatory reversal transactions.
- **Optimistic Concurrency:** Balance records (`INVENTORY_BALANCE`) utilize JPA `@Version` to prevent race conditions during concurrent scans and checkout/receiving operations.

## 10. Assumptions
- The primary deployment target is a centralized cloud or on-premise server, with stores accessing it via web browsers.
- Intermittent connectivity is mostly a concern for warehouse/store floor operations, hence the offline buffering requirement for scanners.
- Product variants (color, size) are handled as distinct SKUs (Stock Keeping Units).
- The system will need to generate printing labels for barcodes if products arrive unlabeled.
- Standard inventory valuation methods like FIFO or Weighted Average are required for reporting.

## 11. Constraints
- Must use Oracle Database (19c / 21c / 23ai Free).
- Must use Java 17/21 with Spring Boot 3.x for the backend.
- Must use Angular 17+ with TypeScript 5.x for the frontend.
- No direct balance overwrites; all changes must go through the `InventoryLedgerService`.
- Must adhere to Hexagonal Architecture, isolating Domain from Frameworks.

## 12. Dependencies
- **Backend:** Spring Boot, Spring Data JPA, Hibernate 6.x, Spring Security 6.x, MapStruct, Jakarta Validation, OpenAPI 3, Flyway, HikariCP, Lombok.
- **Frontend:** Angular, RxJS, Signals, Tailwind CSS / Angular Material, ZXing / BarcodeDetector API.
- **Infrastructure:** Docker Compose (for local dev), Oracle Database.

## 13. MVP Scope
- Product catalog and supplier management.
- Multi-location stock tracking (Stores & Warehouses).
- Double-entry stock ledger operations (Receiving, Adjustments, Transfers, Sales).
- Barcode scanning via camera or hardware wedge.
- User authentication and role-based access control.
- Core reporting (current stock, low stock, stock movements).
- Flyway migrations and MapStruct integrations.

## 14. Advanced/Future Scope
- Full RFID hardware integration (EPC Gen2 continuous reading portals).
- Advanced demand forecasting and analytics utilizing machine learning.
- Route planning for inter-warehouse fleet operations.
- Deep bidirectional integration with external accounting ERPs (e.g., SAP, Oracle NetSuite).
- B2B customer portals for wholesale ordering.
