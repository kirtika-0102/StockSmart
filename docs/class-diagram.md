# StockSmart Domain Class Diagrams

Conceptual models aligned with [database.md](./database.md) and the college-level inventory design.

## 1. User / auth

```mermaid
classDiagram
    class User {
        +Long id
        +String username
        +String email
        +String passwordHash
        +Boolean active
    }
    class Role {
        +Long id
        +String name
    }
    User "*" -- "*" Role : userRoles
```

Roles: `ADMIN`, `INVENTORY_MANAGER`, `STAFF`. No separate Permission entity in MVP.

---

## 2. Product catalog

```mermaid
classDiagram
    class Category {
        +Long id
        +String name
        +Long parentId
    }
    class Product {
        +Long id
        +String sku
        +String name
        +String barcode
        +BigDecimal unitPrice
        +BigDecimal costPrice
        +Integer defaultReorderLevel
        +String status
        +Long categoryId
    }
    Category "1" -- "*" Product
    Category "0..1" -- "*" Category : parent
```

---

## 3. Location & inventory

```mermaid
classDiagram
    class Location {
        +Long id
        +String name
        +String locationType
    }
    class Inventory {
        +Long id
        +Long productId
        +Long locationId
        +Integer quantity
        +Integer reorderLevel
    }
    class InventoryTransaction {
        +Long id
        +Long productId
        +Long locationId
        +String transactionType
        +Integer quantityChange
        +String referenceType
        +String referenceId
        +LocalDateTime createdAt
    }
    Location "1" -- "*" Inventory
    Product "1" -- "*" Inventory
    Product "1" -- "*" InventoryTransaction
    Location "1" -- "*" InventoryTransaction
```

Stock updates go through **InventoryService**; transactions optional for history.

---

## 4. Suppliers

```mermaid
classDiagram
    class Supplier {
        +Long id
        +String name
        +String email
        +String phone
    }
    class SupplierProduct {
        +Long id
        +Long supplierId
        +Long productId
        +BigDecimal unitCost
    }
    Supplier "1" -- "*" SupplierProduct
    Product "1" -- "*" SupplierProduct
```

---

## 5. Stock transfer

```mermaid
classDiagram
    class StockTransfer {
        +Long id
        +Long sourceLocationId
        +Long destLocationId
        +String status
    }
    class StockTransferItem {
        +Long id
        +Long transferId
        +Long productId
        +Integer quantity
    }
    StockTransfer "1" -- "*" StockTransferItem
```

---

## 6. Purchase order

```mermaid
classDiagram
    class PurchaseOrder {
        +Long id
        +Long supplierId
        +Long locationId
        +String status
    }
    class PurchaseOrderItem {
        +Long id
        +Long purchaseOrderId
        +Long productId
        +Integer quantityOrdered
        +Integer quantityReceived
        +BigDecimal unitCost
    }
    PurchaseOrder "1" -- "*" PurchaseOrderItem
```

---

## 7. Sales order

```mermaid
classDiagram
    class Order {
        +Long id
        +Long locationId
        +String status
        +BigDecimal totalAmount
    }
    class OrderItem {
        +Long id
        +Long orderId
        +Long productId
        +Integer quantity
        +BigDecimal unitPrice
    }
    Order "1" -- "*" OrderItem
```

---

## 8. Alert

```mermaid
classDiagram
    class Alert {
        +Long id
        +Long productId
        +Long locationId
        +String alertType
        +String message
        +String status
    }
```

---

## 9. Service layer (conceptual)

```mermaid
classDiagram
    class InventoryService {
        +stockIn()
        +stockOut()
        +adjust()
        +transfer()
        +receivePurchaseOrder()
        +fulfillOrder()
    }
    class ProductService {
        +crud()
        +findByBarcode()
    }
    InventoryService --> Inventory
    InventoryService --> InventoryTransaction
    ProductService --> Product
```
