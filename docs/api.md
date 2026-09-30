# StockSmart REST API Documentation

**Base Path:** `/api/v1`

## Common Response Envelopes

### Success Response
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation successful",
  "timestamp": "2026-09-30T10:00:00Z"
}
```

### Paginated Success Response
```json
{
  "success": true,
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  },
  "message": "Data retrieved successfully",
  "timestamp": "2026-09-30T10:00:00Z"
}
```

### Error Response (RFC 7807 Problem Details + Envelope)
```json
{
  "success": false,
  "error": {
    "type": "https://api.stocksmart.com/errors/validation",
    "title": "Validation Failed",
    "status": 400,
    "detail": "Invalid input provided.",
    "instance": "/api/v1/products",
    "fieldErrors": [
      {
        "field": "sku",
        "message": "SKU must be unique"
      }
    ]
  },
  "message": "Validation Failed",
  "timestamp": "2026-09-30T10:00:00Z"
}
```

---

## 1. Authentication

### `POST /auth/login`
- **Purpose**: Authenticate user and issue JWT.
- **Auth Required**: No
- **Roles**: None
- **Request Body**:
  ```json
  {
    "username": "admin",
    "password": "password123"
  }
  ```
- **Success (200)**: Returns `accessToken` and `refreshToken`.
- **Errors**: 400, 401.

### `POST /auth/register`
- **Purpose**: Register a new user (admin approval may be required).
- **Auth Required**: No
- **Roles**: None
- **Request Body**: User details (username, email, password, firstName, lastName).
- **Success (201)**: Returns registered user (without password).
- **Errors**: 400, 409 (Conflict).

### `POST /auth/refresh`
- **Purpose**: Get a new access token using a refresh token.
- **Auth Required**: No
- **Request Body**: `{"refreshToken": "..."}`
- **Success (200)**: Returns new `accessToken`.
- **Errors**: 400, 401.

### `POST /auth/logout`
- **Purpose**: Invalidate current tokens.
- **Auth Required**: Yes
- **Success (200)**: Empty data.
- **Errors**: 401.

---

## 2. Users

### `GET /users`
- **Purpose**: List users with pagination and filtering.
- **Auth Required**: Yes
- **Roles**: `USER_READ`, `ADMIN`
- **Query Params**: `page`, `size`, `sort`, `search`
- **Success (200)**: Paginated users.
- **Errors**: 401, 403.

### `GET /users/{id}`
- **Purpose**: Get user details.
- **Auth Required**: Yes
- **Roles**: `USER_READ`, `ADMIN`
- **Path Params**: `id` (Long)
- **Success (200)**: User details.
- **Errors**: 401, 403, 404.

### `POST /users`
- **Purpose**: Create a user (Admin).
- **Auth Required**: Yes
- **Roles**: `USER_WRITE`, `ADMIN`
- **Request Body**: User data.
- **Success (201)**: Created user.
- **Errors**: 400, 401, 403, 409.

### `PUT /users/{id}`
- **Purpose**: Update user details.
- **Auth Required**: Yes
- **Roles**: `USER_WRITE`, `ADMIN`
- **Path Params**: `id` (Long)
- **Request Body**: User update data.
- **Success (200)**: Updated user.
- **Errors**: 400, 401, 403, 404.

### `DELETE /users/{id}`
- **Purpose**: Deactivate a user.
- **Auth Required**: Yes
- **Roles**: `ADMIN`
- **Path Params**: `id` (Long)
- **Success (204)**: No Content.
- **Errors**: 401, 403, 404.

### `POST /users/{id}/roles`
- **Purpose**: Assign roles to a user.
- **Auth Required**: Yes
- **Roles**: `ADMIN`
- **Path Params**: `id` (Long)
- **Request Body**: `{"roleIds": [1, 2]}`
- **Success (200)**: Updated user roles.
- **Errors**: 400, 401, 403, 404.

### `POST /users/{id}/reset-password`
- **Purpose**: Reset user password.
- **Auth Required**: Yes
- **Roles**: `ADMIN`
- **Path Params**: `id` (Long)
- **Request Body**: `{"newPassword": "..."}`
- **Success (200)**: Success message.
- **Errors**: 400, 401, 403, 404.

---

## 3. Roles

### `GET /roles`
- **Purpose**: List roles.
- **Auth Required**: Yes
- **Roles**: `ROLE_READ`, `ADMIN`
- **Success (200)**: List of roles.
- **Errors**: 401, 403.

### `GET /roles/{id}`
- **Purpose**: Get role details.
- **Auth Required**: Yes
- **Roles**: `ROLE_READ`, `ADMIN`
- **Path Params**: `id` (Long)
- **Success (200)**: Role details.
- **Errors**: 401, 403, 404.

### `POST /roles`
- **Purpose**: Create a role.
- **Auth Required**: Yes
- **Roles**: `ADMIN`
- **Request Body**: `{"name": "MANAGER", "description": "..."}`
- **Success (201)**: Created role.
- **Errors**: 400, 401, 403, 409.

### `PUT /roles/{id}`
- **Purpose**: Update a role.
- **Auth Required**: Yes
- **Roles**: `ADMIN`
- **Path Params**: `id` (Long)
- **Request Body**: Updates to role.
- **Success (200)**: Updated role.
- **Errors**: 400, 401, 403, 404.

### `DELETE /roles/{id}`
- **Purpose**: Delete a role.
- **Auth Required**: Yes
- **Roles**: `ADMIN`
- **Path Params**: `id` (Long)
- **Success (204)**: No Content.
- **Errors**: 401, 403, 404, 409 (if assigned to users).

### `POST /roles/{id}/permissions`
- **Purpose**: Assign permissions to a role.
- **Auth Required**: Yes
- **Roles**: `ADMIN`
- **Path Params**: `id` (Long)
- **Request Body**: `{"permissionIds": [1, 2, 3]}`
- **Success (200)**: Updated role permissions.
- **Errors**: 400, 401, 403, 404.

---

## 4. Products

### `GET /products`
- **Purpose**: List products with search, filter, pagination.
- **Auth Required**: Yes
- **Roles**: `PRODUCT_READ`
- **Query Params**: `page`, `size`, `sort`, `search`, `categoryId`, `brandId`
- **Success (200)**: Paginated products.
- **Errors**: 401, 403.

### `GET /products/{id}`
- **Purpose**: Get product details.
- **Auth Required**: Yes
- **Roles**: `PRODUCT_READ`
- **Path Params**: `id` (Long)
- **Success (200)**: Product details.
- **Errors**: 401, 403, 404.

### `GET /products/barcode/{barcode}`
- **Purpose**: Lookup product by barcode.
- **Auth Required**: Yes
- **Roles**: `PRODUCT_READ`
- **Path Params**: `barcode` (String)
- **Success (200)**: Product details.
- **Errors**: 401, 403, 404.

### `POST /products`
- **Purpose**: Create a product.
- **Auth Required**: Yes
- **Roles**: `PRODUCT_WRITE`
- **Request Body**: Product details (name, sku, description, categoryId, etc.)
- **Success (201)**: Created product.
- **Errors**: 400, 401, 403, 409 (Duplicate SKU).

### `PUT /products/{id}`
- **Purpose**: Update a product.
- **Auth Required**: Yes
- **Roles**: `PRODUCT_WRITE`
- **Path Params**: `id` (Long)
- **Request Body**: Product updates.
- **Success (200)**: Updated product.
- **Errors**: 400, 401, 403, 404, 409.

### `DELETE /products/{id}`
- **Purpose**: Delete (soft-delete/deactivate) a product.
- **Auth Required**: Yes
- **Roles**: `PRODUCT_WRITE`
- **Path Params**: `id` (Long)
- **Success (204)**: No Content.
- **Errors**: 401, 403, 404.

---

## 5. Categories

### `GET /categories`
- **Purpose**: List categories (including hierarchy).
- **Auth Required**: Yes
- **Roles**: `PRODUCT_READ`
- **Success (200)**: List of categories.
- **Errors**: 401, 403.

### `GET /categories/{id}`
- **Purpose**: Get category details.
- **Auth Required**: Yes
- **Roles**: `PRODUCT_READ`
- **Path Params**: `id` (Long)
- **Success (200)**: Category details.
- **Errors**: 401, 403, 404.

### `POST /categories`
- **Purpose**: Create a category.
- **Auth Required**: Yes
- **Roles**: `PRODUCT_WRITE`
- **Request Body**: `{"name": "...", "parentId": 1}`
- **Success (201)**: Created category.
- **Errors**: 400, 401, 403.

### `PUT /categories/{id}`
- **Purpose**: Update a category.
- **Auth Required**: Yes
- **Roles**: `PRODUCT_WRITE`
- **Path Params**: `id` (Long)
- **Request Body**: Category updates.
- **Success (200)**: Updated category.
- **Errors**: 400, 401, 403, 404.

### `DELETE /categories/{id}`
- **Purpose**: Delete a category.
- **Auth Required**: Yes
- **Roles**: `PRODUCT_WRITE`
- **Path Params**: `id` (Long)
- **Success (204)**: No Content.
- **Errors**: 401, 403, 404, 409 (If has products).

---

## 6. Brands

### `GET /brands`
- **Purpose**: List brands.
- **Auth Required**: Yes
- **Roles**: `PRODUCT_READ`
- **Success (200)**: List of brands.

### `GET /brands/{id}`
- **Purpose**: Get brand details.
- **Auth Required**: Yes
- **Roles**: `PRODUCT_READ`
- **Path Params**: `id` (Long)
- **Success (200)**: Brand details.

### `POST /brands`
- **Purpose**: Create a brand.
- **Auth Required**: Yes
- **Roles**: `PRODUCT_WRITE`
- **Request Body**: `{"name": "...", "description": "..."}`
- **Success (201)**: Created brand.

### `PUT /brands/{id}`
- **Purpose**: Update a brand.
- **Auth Required**: Yes
- **Roles**: `PRODUCT_WRITE`
- **Path Params**: `id` (Long)
- **Request Body**: Updates.
- **Success (200)**: Updated brand.

### `DELETE /brands/{id}`
- **Purpose**: Delete a brand.
- **Auth Required**: Yes
- **Roles**: `PRODUCT_WRITE`
- **Path Params**: `id` (Long)
- **Success (204)**: No Content.

---

## 7. Suppliers

### `GET /suppliers`
- **Purpose**: List suppliers.
- **Auth Required**: Yes
- **Roles**: `SUPPLIER_READ`
- **Success (200)**: List of suppliers (paginated).

### `GET /suppliers/{id}`
- **Purpose**: Get supplier details.
- **Auth Required**: Yes
- **Roles**: `SUPPLIER_READ`
- **Path Params**: `id` (Long)
- **Success (200)**: Supplier details.

### `POST /suppliers`
- **Purpose**: Create a supplier.
- **Auth Required**: Yes
- **Roles**: `SUPPLIER_WRITE`
- **Request Body**: Supplier info.
- **Success (201)**: Created supplier.

### `PUT /suppliers/{id}`
- **Purpose**: Update a supplier.
- **Auth Required**: Yes
- **Roles**: `SUPPLIER_WRITE`
- **Path Params**: `id` (Long)
- **Success (200)**: Updated supplier.

### `DELETE /suppliers/{id}`
- **Purpose**: Delete a supplier.
- **Auth Required**: Yes
- **Roles**: `SUPPLIER_WRITE`
- **Path Params**: `id` (Long)
- **Success (204)**: No Content.

### `GET /suppliers/{id}/products`
- **Purpose**: Get products provided by supplier.
- **Auth Required**: Yes
- **Roles**: `SUPPLIER_READ`
- **Path Params**: `id` (Long)
- **Success (200)**: List of products.

### `POST /suppliers/{id}/products`
- **Purpose**: Assign products to a supplier.
- **Auth Required**: Yes
- **Roles**: `SUPPLIER_WRITE`
- **Path Params**: `id` (Long)
- **Request Body**: `{"productIds": [1, 2]}`
- **Success (200)**: Updated list.

---

## 8. Locations

### `GET /locations`
- **Purpose**: List locations (stores, warehouses).
- **Auth Required**: Yes
- **Roles**: `LOCATION_READ`
- **Success (200)**: List of locations.

### `GET /locations/{id}`
- **Purpose**: Get location details.
- **Auth Required**: Yes
- **Roles**: `LOCATION_READ`
- **Path Params**: `id` (Long)
- **Success (200)**: Location details.

### `POST /locations`
- **Purpose**: Create a location.
- **Auth Required**: Yes
- **Roles**: `LOCATION_WRITE`
- **Request Body**: Location info (type, address).
- **Success (201)**: Created location.

### `PUT /locations/{id}`
- **Purpose**: Update a location.
- **Auth Required**: Yes
- **Roles**: `LOCATION_WRITE`
- **Path Params**: `id` (Long)
- **Success (200)**: Updated location.

### `DELETE /locations/{id}`
- **Purpose**: Delete a location.
- **Auth Required**: Yes
- **Roles**: `LOCATION_WRITE`
- **Path Params**: `id` (Long)
- **Success (204)**: No Content.

---

## 9. Inventory

### `GET /inventory/locations/{locationId}/products/{productId}`
- **Purpose**: Get current stock balance for a product at a location.
- **Auth Required**: Yes
- **Roles**: `INVENTORY_READ`
- **Path Params**: `locationId`, `productId`
- **Success (200)**: Inventory balance.

### `GET /inventory/locations/{locationId}`
- **Purpose**: List inventory balances for a location.
- **Auth Required**: Yes
- **Roles**: `INVENTORY_READ`
- **Path Params**: `locationId`
- **Query Params**: `page`, `size`
- **Success (200)**: Paginated balances.

### `POST /inventory/receive`
- **Purpose**: Receive stock (creates ledger entry).
- **Auth Required**: Yes
- **Roles**: `INVENTORY_WRITE`
- **Request Body**: `{"locationId": 1, "productId": 100, "quantity": 50, "reason": "PO Receive", "referenceId": "PO-123"}`
- **Success (201)**: Ledger transaction created, balance updated.
- **Notes**: Must be scoped to location. Updates `INVENTORY_BALANCE` via optimistic locking.

### `POST /inventory/adjust`
- **Purpose**: Adjust stock (cycle count correction, damage).
- **Auth Required**: Yes
- **Roles**: `INVENTORY_WRITE`
- **Request Body**: `{"locationId": 1, "productId": 100, "quantity": -5, "reason": "Damage"}`
- **Success (201)**: Ledger transaction created.

### `GET /inventory/transactions`
- **Purpose**: Query stock ledger transactions.
- **Auth Required**: Yes
- **Roles**: `INVENTORY_READ`
- **Query Params**: `locationId`, `productId`, `fromDate`, `toDate`, `page`, `size`
- **Success (200)**: Paginated ledger transactions.

---

## 10. Stock Transfers

### `GET /transfers`
- **Purpose**: List transfers.
- **Auth Required**: Yes
- **Roles**: `TRANSFER_READ`
- **Query Params**: `status`, `fromLocationId`, `toLocationId`, `page`, `size`
- **Success (200)**: Paginated transfers.

### `GET /transfers/{id}`
- **Purpose**: Get transfer details.
- **Auth Required**: Yes
- **Roles**: `TRANSFER_READ`
- **Path Params**: `id` (Long)
- **Success (200)**: Transfer details.

### `POST /transfers`
- **Purpose**: Create a transfer request.
- **Auth Required**: Yes
- **Roles**: `TRANSFER_WRITE`
- **Request Body**: Transfer details (from/to, items).
- **Success (201)**: Created transfer.

### `POST /transfers/{id}/approve`
- **Purpose**: Approve a transfer.
- **Auth Required**: Yes
- **Roles**: `TRANSFER_APPROVE`
- **Path Params**: `id` (Long)
- **Success (200)**: Transfer status updated to APPROVED.

### `POST /transfers/{id}/ship`
- **Purpose**: Ship a transfer (deducts stock from source location).
- **Auth Required**: Yes
- **Roles**: `TRANSFER_WRITE`
- **Path Params**: `id` (Long)
- **Success (200)**: Stock deducted, status SHIPPED.

### `POST /transfers/{id}/receive`
- **Purpose**: Receive a transfer (adds stock to destination location).
- **Auth Required**: Yes
- **Roles**: `TRANSFER_WRITE`
- **Path Params**: `id` (Long)
- **Success (200)**: Stock added, status COMPLETED.

### `POST /transfers/{id}/cancel`
- **Purpose**: Cancel a transfer.
- **Auth Required**: Yes
- **Roles**: `TRANSFER_WRITE`
- **Path Params**: `id` (Long)
- **Success (200)**: Status CANCELLED (if eligible).

---

## 11. Purchase Orders

### `GET /purchase-orders`
- **Purpose**: List POs.
- **Auth Required**: Yes
- **Roles**: `PO_READ`
- **Query Params**: `status`, `supplierId`, `locationId`, `page`, `size`
- **Success (200)**: Paginated POs.

### `GET /purchase-orders/{id}`
- **Purpose**: Get PO details.
- **Auth Required**: Yes
- **Roles**: `PO_READ`
- **Path Params**: `id` (Long)
- **Success (200)**: PO details.

### `POST /purchase-orders`
- **Purpose**: Create a PO.
- **Auth Required**: Yes
- **Roles**: `PO_WRITE`
- **Request Body**: PO details (supplier, location, items).
- **Success (201)**: Created PO.

### `POST /purchase-orders/{id}/approve`
- **Purpose**: Approve a PO.
- **Auth Required**: Yes
- **Roles**: `PO_APPROVE`
- **Path Params**: `id` (Long)
- **Success (200)**: PO approved.

### `POST /purchase-orders/{id}/send`
- **Purpose**: Mark PO as sent to supplier.
- **Auth Required**: Yes
- **Roles**: `PO_WRITE`
- **Path Params**: `id` (Long)
- **Success (200)**: PO status SENT.

### `POST /purchase-orders/{id}/receive`
- **Purpose**: Receive items against a PO.
- **Auth Required**: Yes
- **Roles**: `PO_RECEIVE`
- **Path Params**: `id` (Long)
- **Request Body**: Received quantities for items.
- **Success (200)**: Inventory updated, PO status PARTIAL or COMPLETED.

### `POST /purchase-orders/{id}/cancel`
- **Purpose**: Cancel a PO.
- **Auth Required**: Yes
- **Roles**: `PO_WRITE`
- **Path Params**: `id` (Long)
- **Success (200)**: Status CANCELLED.

---

## 12. Orders (Sales)

### `GET /orders`
- **Purpose**: List sales orders.
- **Auth Required**: Yes
- **Roles**: `ORDER_READ`
- **Query Params**: `status`, `locationId`, `page`, `size`
- **Success (200)**: Paginated orders.

### `GET /orders/{id}`
- **Purpose**: Get order details.
- **Auth Required**: Yes
- **Roles**: `ORDER_READ`
- **Path Params**: `id` (Long)
- **Success (200)**: Order details.

### `POST /orders`
- **Purpose**: Create a sales order.
- **Auth Required**: Yes
- **Roles**: `ORDER_WRITE`
- **Request Body**: Order details (items, location, customer).
- **Success (201)**: Created order.

### `POST /orders/{id}/process`
- **Purpose**: Mark order as processing (allocate stock).
- **Auth Required**: Yes
- **Roles**: `ORDER_WRITE`
- **Path Params**: `id` (Long)
- **Success (200)**: Order status PROCESSING.

### `POST /orders/{id}/complete`
- **Purpose**: Complete order (deduct stock).
- **Auth Required**: Yes
- **Roles**: `ORDER_WRITE`
- **Path Params**: `id` (Long)
- **Success (200)**: Inventory updated, status COMPLETED.

### `POST /orders/{id}/cancel`
- **Purpose**: Cancel order (release allocated stock).
- **Auth Required**: Yes
- **Roles**: `ORDER_WRITE`
- **Path Params**: `id` (Long)
- **Success (200)**: Status CANCELLED.

---

## 13. Barcode

### `GET /barcodes/{barcode}`
- **Purpose**: Lookup barcode across products.
- **Auth Required**: Yes
- **Roles**: `PRODUCT_READ`
- **Path Params**: `barcode` (String)
- **Success (200)**: Returns matched entity (Product).
- **Notes**: GS1 compliance support (EAN-13, UPC-A, GS1-128).

### `POST /barcodes`
- **Purpose**: Register a new barcode to a product.
- **Auth Required**: Yes
- **Roles**: `PRODUCT_WRITE`
- **Request Body**: `{"productId": 1, "barcode": "...", "type": "EAN_13"}`
- **Success (201)**: Barcode registered.

---

## 14. RFID

### `GET /rfid/tags/{epc}`
- **Purpose**: Lookup RFID tag (EPC Gen2/SGTIN-96).
- **Auth Required**: Yes
- **Roles**: `INVENTORY_READ`
- **Path Params**: `epc` (String)
- **Success (200)**: RFID tag details, linked product.

### `POST /rfid/register`
- **Purpose**: Register an RFID tag to a product instance.
- **Auth Required**: Yes
- **Roles**: `INVENTORY_WRITE`
- **Request Body**: `{"epc": "...", "productId": 1}`
- **Success (201)**: Tag registered.

### `POST /rfid/intake`
- **Purpose**: Bulk scan intake via RFID portal (EPCIS compatible).
- **Auth Required**: Yes
- **Roles**: `INVENTORY_WRITE`
- **Request Body**: `{"locationId": 1, "tags": ["...", "..."], "idempotencyKey": "uuid"}`
- **Success (200)**: Processes valid tags, updates inventory.
- **Notes**: Supports idempotency keys for offline resilience.

---

## 15. Alerts

### `GET /alerts`
- **Purpose**: List system alerts (e.g., low stock).
- **Auth Required**: Yes
- **Roles**: `ALERT_READ`
- **Query Params**: `status`, `page`, `size`
- **Success (200)**: Paginated alerts.

### `POST /alerts/{id}/acknowledge`
- **Purpose**: Mark an alert as acknowledged.
- **Auth Required**: Yes
- **Roles**: `ALERT_WRITE`
- **Path Params**: `id` (Long)
- **Success (200)**: Alert updated.

### `GET /alerts/config`
- **Purpose**: Get alert threshold configurations.
- **Auth Required**: Yes
- **Roles**: `ADMIN`
- **Success (200)**: Alert configs.

### `PUT /alerts/config`
- **Purpose**: Update alert thresholds (e.g., low stock levels).
- **Auth Required**: Yes
- **Roles**: `ADMIN`
- **Request Body**: Config updates.
- **Success (200)**: Updated configs.

---

## 16. Dashboard/KPIs

### `GET /dashboard/summary`
- **Purpose**: Get general dashboard KPIs (total stock, active POs).
- **Auth Required**: Yes
- **Roles**: `DASHBOARD_READ`
- **Query Params**: `locationId`
- **Success (200)**: KPI metrics.

### `GET /dashboard/low-stock`
- **Purpose**: Get top low stock items for dashboard.
- **Auth Required**: Yes
- **Roles**: `DASHBOARD_READ`
- **Query Params**: `locationId`, `limit`
- **Success (200)**: List of low stock products.

### `GET /dashboard/recent-activity`
- **Purpose**: Get recent ledger activities.
- **Auth Required**: Yes
- **Roles**: `DASHBOARD_READ`
- **Query Params**: `limit`
- **Success (200)**: Recent activities.

---

## 17. Reports

### `POST /reports/generate`
- **Purpose**: Request report generation (async/sync).
- **Auth Required**: Yes
- **Roles**: `REPORT_READ`
- **Request Body**: Report type (INVENTORY_VALUATION, SALES_SUMMARY), parameters, outputFormat (PDF, CSV, EXCEL).
- **Success (200)**: Report generation queued or data returned.

### `GET /reports/download/{id}`
- **Purpose**: Download a generated report file.
- **Auth Required**: Yes
- **Roles**: `REPORT_READ`
- **Path Params**: `id` (String)
- **Success (200)**: File stream (application/pdf, text/csv, etc.).

---

## 18. Audit

### `GET /audit/logs`
- **Purpose**: Query system audit logs.
- **Auth Required**: Yes
- **Roles**: `ADMIN`
- **Query Params**: `entityType`, `entityId`, `action`, `userId`, `fromDate`, `toDate`, `page`, `size`
- **Success (200)**: Paginated audit logs.
- **Notes**: Adheres to the strict audit trail requirement (created_by, updated_by, IP, previous/new state).
