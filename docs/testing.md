# StockSmart Testing Guide (Academic)

Practical testing scope for the B.Tech project — enough to show quality awareness without enterprise coverage gates.

---

## 1. Goals

- Verify **inventory rules** (no negative stock, transfer moves quantity correctly)
- Verify **security** (401 without token, 403 for wrong role)
- Verify **CRUD** on at least one master entity (e.g. product)

---

## 2. Backend tests

### Unit tests (JUnit 5 + Mockito)

- `InventoryServiceTest` — stock in/out, adjust, insufficient stock exception
- `ProductServiceTest` — barcode uniqueness
- `OrderServiceTest` — confirm order reduces inventory

### Integration tests (optional)

- `@SpringBootTest` + `@AutoConfigureMockMvc` for login + one protected endpoint
- Testcontainers Oracle **optional** — H2 with Oracle mode or dev Oracle acceptable for college demo

---

## 3. Frontend tests

- Service tests with `HttpClientTestingModule`
- Guard test: unauthenticated user redirected
- One component test for reactive form validation

---

## 4. Manual demo checklist

| # | Scenario | Expected |
|---|----------|----------|
| 1 | Login as ADMIN | Dashboard loads |
| 2 | STAFF cannot open /users | 403 or redirect |
| 3 | Create product with barcode | Saved, searchable |
| 4 | Receive PO | Inventory increases |
| 5 | Confirm order | Inventory decreases |
| 6 | Transfer | Source down, dest up |
| 7 | Quantity below reorder | Alert or dashboard KPI |

---

## 5. Traceability

Map manual cases to requirements FR IDs in [requirements.md](./requirements.md) for project report appendix.

---

## Removed from enterprise testing doc

- 80% coverage gates, ledger immutability test suite, GS1 parser tests, optimistic lock retry tests, RFC 7807 contract tests, Flyway test migrations

---

## Future

- Testcontainers Oracle, CI pipeline, e2e Cypress/Playwright
