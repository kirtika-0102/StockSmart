# StockSmart Testing Guide (Academic & Demo)

Practical testing scope and manual verification checklist for the B.Tech project presentation, viva, and continuous verification.

---

## 1. Testing Goals

1. **Role-Based Access Control (RBAC):**
   - Validate that `STAFF` is restricted from administrative operations (`/users`, master data deletions).
   - Validate that `INVENTORY_MANAGER` can handle catalog, inventory adjustments, purchase orders, and transfers.
   - Validate that `ADMIN` has full access across all modules.
2. **Business Invariants & Rules:**
   - **No negative stock:** Prevent sales orders or stock-outs exceeding available inventory.
   - **Transfer integrity:** Decrement source location stock and increment destination location stock atomically.
   - **Purchase receipt:** Increment target location balance and log `PO_RECEIVE` transaction.
   - **Alert generation:** Trigger `LOW_STOCK` when quantity <= reorder level, and `OUT_OF_STOCK` when quantity == 0.
3. **Master Data Integrity:**
   - SKU uniqueness across products.
   - Barcode lookup resolution.
   - Unique product-location inventory pairs.

---

## 2. Automated Test Suites

### Backend Tests (JUnit 5 + MockMvc)
Located under `backend/src/test/java/com/stocksmart/`:

| Test Suite | Scope & Coverage |
|------------|------------------|
| `AuthFlowTest` | Login with valid/invalid credentials, registration, `/api/auth/me` profile resolution. |
| `UserAdminApiTest` | Admin user CRUD, duplicate checks, soft deactivation, role assignment. |
| `CategoryApiTest` | Category hierarchy, name uniqueness, delete constraints. |
| `ProductApiTest` | Product creation, SKU uniqueness, barcode lookup (`/api/products/barcode/{barcode}`). |
| `LocationApiTest` | Store vs. Warehouse location management, uniqueness constraints. |
| `SupplierApiTest` | Supplier CRUD and contact information validation. |
| `SupplierProductApiTest` | Supplier product catalog linking, cost tracking, duplicate prevention. |
| `InventoryApiTest` | Stock-in, stock-out (success & rejection on insufficient stock), adjustment, barcode search. |
| `WorkflowApiTest` | Purchase order receipt, sales order fulfillment, inter-location stock transfer. |
| `AlertApiTest` | Alert retrieval, status transitions (ACKNOWLEDGE, RESOLVE), manual reorder check scan. |
| `DashboardApiTest` | KPI summary aggregation, valuation calculations, location distribution. |

### Frontend Build & Validation
- **Framework:** Angular 19 (Standalone Architecture)
- **Production Build:** `npm run build` generates optimized distribution bundles without errors or warnings.
- **Route Guards:** Functional `authGuard` verifies active session; `roleGuard` validates `ADMIN` or `INVENTORY_MANAGER` permissions before activating routes.
- **Interceptors:** `AuthInterceptor` injects Bearer JWT; `HttpErrorInterceptor` handles 401/403/500 globally.

---

## 3. Demo Seed Data

On startup, `DataInitializer` seeds:
- **Roles:** `ADMIN`, `INVENTORY_MANAGER`, `STAFF`
- **Users:**
  - `admin` / `Admin@123` (Full system access)
  - `manager` / `Manager@123` (Inventory & workflow management)
  - `staff` / `Staff@123` (POS orders, stock inquiry, barcode scanning)
- **Categories:** `Electronics`, `Groceries`, `Stationery`, `Hardware & Tools`
- **Locations:** `Central Warehouse` (Warehouse), `Downtown Retail Store` (Store)
- **Suppliers:** `Apex Electronics Ltd`, `Metro Wholesale Distribution`
- **Products:**
  - `Wireless Optical Mouse` (`ELEC-MOU-01`, Barcode `8901234567890`)
  - `Mechanical Gaming Keyboard` (`ELEC-KEY-02`, Barcode `8901234567891`)
  - `USB-C Fast Charging Cable 2m` (`ELEC-CBL-03`, Barcode `8901234567894`)
  - `A5 Ruled Hardcover Notebook` (`STAT-NBK-01`, Barcode `8901234567892`)
  - `Organic Green Tea (50 Bags)` (`GROC-TEA-01`, Barcode `8901234567893`)
  - `Precision Screwdriver Set 32-in-1` (`HARD-SCR-01`, Barcode `8901234567895`)
- **Initial Inventory:** Includes healthy balances at Central Warehouse, plus 1 low-stock item and 1 out-of-stock item at Downtown Retail Store to demonstrate alert management immediately.

---

## 4. Comprehensive Manual Viva / Demo Checklist

| Step | Action / Scenario | Test Steps | Expected Result | Verified |
|------|-------------------|------------|-----------------|:--------:|
| **1. Auth** | Admin Login | Navigate to `/login`, enter `admin` / `Admin@123` | Redirected to `/` (Dashboard), JWT saved in `sessionStorage`, user profile shown in header | [x] |
| **2. RBAC** | Staff Menu Restriction | Logout, login as `staff` / `Staff@123` | Sidebar hides `Users` and administrative controls. Direct access to `/users` redirects away | [x] |
| **3. Master** | Add Product | Navigate to `/products`, click `Add Product`, fill name, SKU, price, category | Product appears in table and is immediately available for inventory movements | [x] |
| **4. Scan** | Barcode Lookup | Navigate to `/inventory`, type `8901234567890` in Barcode Scanner input and press enter | Instant lookup resolves "Wireless Optical Mouse" with total stock & breakdown by warehouse and store | [x] |
| **5. Stock** | Stock In Action | In `/inventory`, click `Stock In` tab, select product, warehouse, quantity `50` | Warehouse balance increments by 50, audit log records a `STOCK_IN` transaction | [x] |
| **6. Stock** | Stock Out Validation | In `/inventory`, attempt to stock out `9999` units of any product | Operation rejected with clear error: "Insufficient stock available" (HTTP 400) | [x] |
| **7. Adjust**| Physical Inventory Count | In `/inventory`, click `Adjustment` tab, set physical quantity to `35` with reason "Annual count" | Stock quantity adjusts to 35, delta computed and logged as `ADJUSTMENT` | [x] |
| **8. PO** | Receive Purchase Order | Navigate to `/purchase-orders`, click `Receive` on a submitted PO | Status changes to `RECEIVED`, inventory at destination warehouse automatically increases | [x] |
| **9. Sales** | Fulfill Customer Order | Navigate to `/orders`, click `New Order`, add items, then click `Confirm Order` | Order status changes to `COMPLETED`, inventory decrements immediately | [x] |
| **10. Move** | Inter-Location Transfer | Navigate to `/stock-transfers`, create transfer from Warehouse to Retail Store, click `Complete` | Warehouse stock decreases, Store stock increases by exact transfer quantity | [x] |
| **11. Alert**| Low Stock Alert Scan | Navigate to `/alerts`, view active alerts for low stock items, or click `Run Reorder Scan` | Active low-stock and out-of-stock items listed with options to `Acknowledge` or `Resolve` | [x] |
| **12. Report**| Inventory Valuation Report| Navigate to `/reports`, filter by Category or Location | Real-time valuation calculations (Total Units × Unit Price = Total Value) displayed with summary cards | [x] |
| **13. KPI** | Executive Dashboard | Navigate to `/` (Dashboard) | Summary cards (Total Products, Stock Units, Valuation, Active Alerts), distribution chart, and quick launchpad visible | [x] |

---

## 5. Requirements Traceability Matrix (B.Tech Syllabus)

| Module | Requirement ID | Implementation Artifacts | Verification Method |
|--------|----------------|--------------------------|---------------------|
| Authentication & RBAC | FR-01, FR-02 | `SecurityConfig`, `JwtService`, `roleGuard` | `AuthFlowTest`, Manual Login Test |
| User Administration | FR-03 | `UserController`, `users.component.ts` | `UserAdminApiTest`, Admin UI |
| Category & Product Master | FR-04, FR-05 | `CategoryController`, `ProductController`, `products.component.ts` | `CategoryApiTest`, `ProductApiTest` |
| Supplier & Catalog Linking | FR-06 | `SupplierController`, `SupplierProductController` | `SupplierApiTest`, `SupplierProductApiTest` |
| Barcode & Inventory Core | FR-07, FR-08 | `InventoryController`, `inventory.component.ts` | `InventoryApiTest`, Barcode Search Widget |
| Purchase & Sales Workflows | FR-09, FR-10 | `PurchaseOrderController`, `OrderController` | `WorkflowApiTest`, PO & Order UI |
| Stock Transfers | FR-11 | `StockTransferController`, `stock-transfers.component.ts` | `WorkflowApiTest`, Transfer UI |
| Alert Management | FR-12 | `AlertController`, `alerts.component.ts` | `AlertApiTest`, Alert Center UI |
| Valuation & KPIs | FR-13, FR-14 | `DashboardController`, `ReportController`, `home.component.ts` | `DashboardApiTest`, Dashboard UI |
