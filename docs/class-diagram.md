# StockSmart Domain Model Class Diagrams

This document contains conceptual Mermaid class diagrams representing the major domain objects and their relationships in the StockSmart project. The diagrams are consistent with the database design, utilizing principles such as double-entry stock ledgers and multi-location scoping.

## 1. User/Auth Domain

```mermaid
classDiagram
    class User {
        +Long id
        +String username
        +String email
        +String passwordHash
        +String firstName
        +String lastName
        +Boolean active
        +DateTime createdAt
        +DateTime updatedAt
    }

    class Role {
        +Long id
        +String name
        +String description
    }

    class Permission {
        +Long id
        +String name
        +String description
        +String module
    }

    User "*" -- "*" Role
    Role "*" -- "*" Permission
```

## 2. Product Domain

```mermaid
classDiagram
    class Product {
        +Long id
        +String sku
        +String name
        +String description
        +Decimal unitPrice
        +Decimal costPrice
        +String unitOfMeasure
        +Decimal weight
        +Boolean active
        +Long categoryId
        +Long brandId
    }

    class Category {
        +Long id
        +String name
        +String description
        +Long parentId
    }

    class Brand {
        +Long id
        +String name
        +String description
        +Boolean active
    }

    class ProductIdentifier {
        +Long id
        +Long productId
        +Enum identifierType
        +String identifierValue
        +Boolean primary
        +Boolean active
    }
    
    note for ProductIdentifier "identifierType: BARCODE_EAN13, BARCODE_UPC_A,\nBARCODE_GS1_128, RFID_SGTIN96"

    class Supplier {
        +Long id
        +String name
        +String contactName
        +String email
        +String phone
        +String address
        +Boolean active
    }

    class SupplierProduct {
        +Long id
        +Long supplierId
        +Long productId
        +String supplierSku
        +Decimal unitCost
        +Integer leadTimeDays
        +Integer minOrderQty
        +Boolean preferred
    }

    Product "*" -- "1" Category
    Product "*" -- "1" Brand
    Product "1" -- "*" ProductIdentifier
    Supplier "1" -- "*" SupplierProduct
    Product "1" -- "*" SupplierProduct
```

## 3. Location, Inventory (Ledger)

```mermaid
classDiagram
    class Location {
        +Long id
        +String name
        +String code
        +Enum locationType
        +String address
        +String city
        +String state
        +String country
        +Boolean active
    }
    
    note for Location "locationType: STORE, WAREHOUSE"

    class InventoryBalance {
        +Long id
        +Long productId
        +Long locationId
        +Integer quantityOnHand
        +Integer quantityReserved
        +Integer quantityAvailable
        +Integer version
    }
    
    note for InventoryBalance "Optimistic Concurrency Control via version"

    class StockTransaction {
        +Long id
        +Long productId
        +Long locationId
        +Enum transactionType
        +Integer quantity
        +String referenceType
        +String referenceId
        +String reason
        +String notes
        +String performedBy
        +DateTime performedAt
    }
    
    note for StockTransaction "transactionType: RECEIVE, ADJUST_IN, ADJUST_OUT,\nTRANSFER_OUT, TRANSFER_IN, SALE, RETURN, CORRECTION\nImmutable ledger entry"

    class AuditLog {
        +Long id
        +String entityType
        +String entityId
        +String action
        +String previousState
        +String newState
        +String performedBy
        +DateTime performedAt
        +String ipAddress
    }

    Location "1" -- "*" InventoryBalance
    Product "1" -- "*" InventoryBalance
    Product "1" -- "*" StockTransaction
    Location "1" -- "*" StockTransaction
```

## 4. Transfers

```mermaid
classDiagram
    class StockTransfer {
        +Long id
        +Long sourceLocationId
        +Long destinationLocationId
        +Enum status
        +String requestedBy
        +String approvedBy
        +DateTime shippedAt
        +DateTime receivedAt
    }
    
    note for StockTransfer "status: DRAFT, PENDING_APPROVAL, APPROVED,\nIN_TRANSIT, RECEIVED, CANCELLED"

    class StockTransferItem {
        +Long id
        +Long transferId
        +Long productId
        +Integer requestedQuantity
        +Integer shippedQuantity
        +Integer receivedQuantity
    }

    StockTransfer "1" -- "*" StockTransferItem
```

## 5. Purchasing

```mermaid
classDiagram
    class PurchaseOrder {
        +Long id
        +Long supplierId
        +Long locationId
        +Enum status
        +Decimal totalAmount
        +String orderedBy
        +String approvedBy
    }
    
    note for PurchaseOrder "status: DRAFT, PENDING_APPROVAL, APPROVED,\nSENT, PARTIALLY_RECEIVED, RECEIVED,\nCOMPLETED, CANCELLED"

    class PurchaseOrderItem {
        +Long id
        +Long purchaseOrderId
        +Long productId
        +Integer orderedQuantity
        +Integer receivedQuantity
        +Decimal unitCost
        +Decimal totalCost
    }

    PurchaseOrder "1" -- "*" PurchaseOrderItem
```

## 6. Orders

```mermaid
classDiagram
    class Order {
        +Long id
        +Long locationId
        +String orderNumber
        +Enum status
        +String customerName
        +Decimal totalAmount
    }
    
    note for Order "status: PENDING, PROCESSING, COMPLETED, CANCELLED"

    class OrderItem {
        +Long id
        +Long orderId
        +Long productId
        +Integer quantity
        +Decimal unitPrice
        +Decimal totalPrice
    }

    Order "1" -- "*" OrderItem
```

## 7. Alerts & Reorder Rules

```mermaid
classDiagram
    class ReorderRule {
        +Long id
        +Long productId
        +Long locationId
        +Integer reorderPoint
        +Integer reorderQuantity
        +Integer maxStockLevel
        +Boolean active
    }

    class Alert {
        +Long id
        +Enum alertType
        +Long productId
        +Long locationId
        +String message
        +Boolean acknowledged
        +String acknowledgedBy
    }
    
    note for Alert "alertType: LOW_STOCK, OUT_OF_STOCK,\nOVERSTOCK, PO_OVERDUE"

    Product "1" -- "*" ReorderRule
    Location "1" -- "*" ReorderRule
```

## 8. Full Overview (Simplified)

```mermaid
classDiagram
    User "*" -- "*" Role
    Role "*" -- "*" Permission
    
    Product "*" -- "1" Category
    Product "*" -- "1" Brand
    Product "1" -- "*" ProductIdentifier
    Supplier "1" -- "*" SupplierProduct
    Product "1" -- "*" SupplierProduct
    
    Location "1" -- "*" InventoryBalance
    Product "1" -- "*" InventoryBalance
    
    Location "1" -- "*" StockTransaction
    Product "1" -- "*" StockTransaction
    
    Product "1" -- "*" ReorderRule
    Location "1" -- "*" ReorderRule
    
    StockTransfer "1" -- "*" StockTransferItem
    PurchaseOrder "1" -- "*" PurchaseOrderItem
    Order "1" -- "*" OrderItem
    
    class User
    class Role
    class Permission
    class Product
    class Category
    class Brand
    class ProductIdentifier
    class Supplier
    class SupplierProduct
    class Location
    class InventoryBalance
    class StockTransaction
    class AuditLog
    class StockTransfer
    class StockTransferItem
    class PurchaseOrder
    class PurchaseOrderItem
    class Order
    class OrderItem
    class ReorderRule
    class Alert
```
