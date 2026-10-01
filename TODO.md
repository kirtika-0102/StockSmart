# StockSmart — Implementation TODO

Track progress for the B.Tech project. See [PROJECT.md](./PROJECT.md) for scope.

**Status:** Phase 0 documentation alignment complete. **Do not treat business modules as implemented.** Next: Phase 1 scaffolding (Spring Boot + Angular foundation only).

---

## Phase 0 — Documentation alignment ✅

- [x] Simplify architecture docs (monolith, no ledger/CQRS)
- [x] Update PROJECT.md, README, CLAUDE.md, CHANGELOG
- [x] Align Mermaid diagrams with simplified design
- [ ] Review `docs/api.md` endpoint list against implemented controllers (during Phase 2+)

---

## Phase 1 — Project scaffolding

### Backend

- [ ] Create Spring Boot project under `backend/`
- [ ] Packages: `config`, `exception`, `security`, feature modules
- [ ] Oracle datasource + JPA (`OracleDialect`, `ddl-auto=update` or documented SQL script)
- [ ] Global exception handler (simple JSON errors)
- [ ] OpenAPI/Springdoc optional

### Frontend

- [ ] Create Angular app under `frontend/` (standalone, routing)
- [ ] Core layout: sidebar, header, auth shell
- [ ] `AuthInterceptor`, `HttpErrorInterceptor`, route guards
- [ ] Environment files for API base URL

### DevOps (minimal)

- [ ] Document Oracle connection steps in README
- [ ] Optional: `docker-compose` for Oracle only

---

## Phase 2 — Security & users

- [ ] Entities: `User`, `Role`, `UserRole` (or role enum)
- [ ] BCrypt password encoding
- [ ] JWT login endpoint
- [ ] Seed roles: ADMIN, INVENTORY_MANAGER, STAFF
- [ ] Admin APIs + Angular **Users** page (ADMIN only)
- [ ] Login page + token storage strategy (document in security doc)

---

## Phase 3 — Master data

- [ ] **Categories** — CRUD
- [ ] **Products** — CRUD, barcode field, link category
- [ ] **Locations** — CRUD (STORE / WAREHOUSE)
- [ ] **Suppliers** — CRUD
- [ ] **SupplierProduct** — link supplier to products
- [ ] Angular pages: Categories, Products, Suppliers, Locations

---

## Phase 4 — Inventory core

- [ ] Entity: `Inventory` (product + location + quantity + reorder level)
- [ ] Optional: `InventoryTransaction` for history
- [ ] Stock In / Stock Out / Adjustment services
- [ ] Validate sufficient stock on decrease
- [ ] Barcode lookup API + product search UI
- [ ] Angular **Inventory** page (levels + actions)

---

## Phase 5 — Workflows

- [ ] **Purchase orders** — create, list, receive → increase inventory
- [ ] **Orders** — create, confirm → decrease inventory
- [ ] **Stock transfers** — create, complete → move quantity between locations
- [ ] Angular pages: Purchase Orders, Orders, Stock Transfers

---

## Phase 6 — Alerts & KPIs

- [ ] Reorder level checks → **Alerts** (computed or persisted)
- [ ] Dashboard KPI queries (see `docs/kpi-reporting.md`)
- [ ] Angular Dashboard + Alerts + basic Reports views

---

## Phase 7 — Polish & demo

- [ ] Role-based menu visibility
- [ ] Form validation (client + server)
- [ ] Sample seed data script for viva/demo
- [ ] Update diagrams if implementation differs slightly
- [ ] Manual test checklist (`docs/testing.md`)
- [ ] Project report / viva talking points from docs

---

## Future (not required for submission)

- [ ] Camera barcode scanning (ZXing)
- [ ] RFID hardware integration
- [ ] Flyway migrations, MapStruct, RFC 7807
- [ ] Double-entry ledger, audit CLOB snapshots, optimistic locking
- [ ] Refresh tokens, granular permissions, offline scan buffer
