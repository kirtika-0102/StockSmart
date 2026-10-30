# Frontend Architecture — StockSmart

Angular **17+** SPA for the B.Tech project. Standalone components, simple structure, no mandatory state library.

---

## 1. Application shell

- **AppComponent** — router outlet, layout
- **Layout:** sidebar navigation, top bar (user, logout), content area
- **Lazy routes** optional; flat routes acceptable for project size

```mermaid
flowchart TD
    App[AppComponent] --> Login[/login]
    App --> Main[ShellComponent]
    Main --> Home[/]
    Main --> Categories[/categories]
    Main --> Users[/users ADMIN]
    Main --> Future[Products inventory orders later]
```

---

## 2. Routes (target)

| Path | Page | Roles |
|------|------|-------|
| `/login` | Login | public |
| `/dashboard` | KPI dashboard | all authenticated |
| `/products` | Product CRUD | manager, admin |
| `/categories` | Categories (**implemented**) | ADMIN, INVENTORY_MANAGER |
| `/suppliers` | Suppliers | manager, admin |
| `/locations` | Locations | manager, admin |
| `/inventory` | Stock levels & actions | manager, staff |
| `/transfers` | Stock transfers | manager |
| `/purchase-orders` | POs | manager |
| `/orders` | Sales orders | staff+ |
| `/alerts` | Low stock | all |
| `/reports` | Reports | manager, admin |
| `/users` | User admin | admin |

---

## 3. Authentication

- **AuthService** — login, logout, `currentUser$`, token storage
- **AuthInterceptor** — attach JWT
- **authGuard** — redirect to login
- **roleGuard** — read `data.roles` on route config (implemented; `/users` requires `ADMIN`)
- **Users page** — list, create, edit, deactivate via `UserService` (`/api/users`)
- **Categories page** — list, create, edit, delete via `CategoryService` (`/api/categories`); `ADMIN` and `INVENTORY_MANAGER`

---

## 4. HTTP & errors

- **HttpClient** services per domain (`ProductService`, `InventoryService`, …)
- **HttpErrorInterceptor** — show toast/snackbar from API `message` field
- Simple error JSON from backend (no RFC 7807 requirement)

---

## 5. Forms

- **ReactiveFormsModule** for all business forms
- Validators mirror backend (@NotBlank, min, etc.)

---

## 6. Feature components (suggested)

| Area | Components |
|------|------------|
| Products | list, form, barcode search |
| Inventory | table by location, adjust dialog, stock in/out |
| Orders | list, form, confirm button |
| Dashboard | kpi cards, charts |
| Shared | data table, confirm dialog, loading spinner |

Smart vs presentational split is **recommended** but not mandatory — keep readable for viva.

---

## 7. Barcode UX

- `BarcodeInputComponent` — autofocus, `(scan)` output event
- Product list filter by barcode
- Optional: listen for rapid keypress + Enter (wedge)

**Not in scope:** offline IndexedDB queue, idempotency sync

---

## 8. Styling

- Clean responsive CSS/SCSS or Angular Material
- Consistent spacing, readable tables for demo

---

## 9. Testing (optional)

- Unit tests for services and guards with `HttpClientTestingModule`
- One e2e happy path (login → dashboard) if time permits

See [testing.md](./testing.md).

---

## Diagram: data flow

```mermaid
sequenceDiagram
    participant C as Component
    participant S as ProductService
    participant H as HttpClient
    participant API as Spring Boot

    C->>S: loadProducts()
    S->>H: GET /api/products
    H->>API: + JWT
    API-->>C: ProductResponse[]
```

---

## Future enhancements

- NgRx store, PWA offline scans, RFC 7807 error parser, location context guard, camera scanner module
