# StockSmart Sequence Diagrams

This document contains sequence diagrams for key flows within the StockSmart application architecture, aligning with the Hexagonal Architecture and Double-Entry Stock Ledger principles.

## 1. Login Flow

User enters credentials, gets authenticated via the `UserRepository`, and receives a JWT for subsequent requests.

```mermaid
sequenceDiagram
    participant U as Browser/Angular
    participant C as AuthController
    participant S as AuthService
    participant R as UserRepository
    participant DB as Oracle Database
    participant J as JWT/Security

    U->>C: POST /auth/login (credentials)
    activate C
    C->>S: authenticate(credentials)
    activate S
    S->>R: findByUsername(username)
    activate R
    R->>DB: SELECT * FROM users WHERE username = ?
    activate DB
    DB-->>R: User entity
    deactivate DB
    R-->>S: User entity
    deactivate R
    
    alt Invalid Credentials
        S-->>C: AuthenticationException
        C-->>U: 401 Unauthorized
    else Valid Credentials
        S->>J: generateToken(User)
        activate J
        J-->>S: JWT token string
        deactivate J
        S-->>C: AuthResponse(JWT)
        deactivate S
        C-->>U: 200 OK + JWT
    end
    deactivate C
```

## 2. Product Creation

User fills out a new product form. The request is authenticated via an interceptor, validated, mapped via MapStruct, and persisted.

```mermaid
sequenceDiagram
    participant U as Browser/Angular
    participant AI as AuthInterceptor
    participant C as REST Controller
    participant S as Service Layer
    participant R as Repository
    participant DB as Oracle Database

    U->>AI: POST /products (Product Form)
    activate AI
    AI->>AI: Append JWT to Authorization Header
    AI->>C: HTTP POST /products
    deactivate AI
    
    activate C
    C->>C: Validate Request (Jakarta Validation)
    C->>S: createProduct(ProductDTO)
    activate S
    S->>S: Map DTO to Entity (MapStruct)
    S->>R: save(ProductEntity)
    activate R
    R->>DB: INSERT INTO product (...)
    activate DB
    DB-->>R: Product created
    deactivate DB
    R-->>S: Saved ProductEntity
    deactivate R
    S->>S: Map Entity to DTO
    S-->>C: ProductDTO
    deactivate S
    C-->>U: 201 Created (ProductDTO)
    deactivate C
```

## 3. Inventory Receiving

User receives items into stock. The system records immutable ledger transactions and updates balances with version checks.

```mermaid
sequenceDiagram
    participant U as Browser/Angular
    participant C as REST Controller
    participant S as Service Layer
    participant LS as InventoryLedgerService
    participant R as Repository
    participant DB as Oracle Database

    U->>C: POST /inventory/receive (items, location)
    activate C
    C->>S: receiveStock(ReceiveRequestDTO)
    activate S
    S->>LS: recordReceive(items, location)
    activate LS
    
    loop For each item
        LS->>R: save(STOCK_TRANSACTION [RECEIVE])
        activate R
        R->>DB: INSERT transaction
        DB-->>R: Saved
        deactivate R
        
        LS->>R: find INVENTORY_BALANCE
        activate R
        R->>DB: SELECT balance
        DB-->>R: Balance entity (with @Version)
        deactivate R
        
        LS->>LS: Update quantity
        
        LS->>R: save(INVENTORY_BALANCE)
        activate R
        R->>DB: UPDATE balance SET qty, version=version+1
        DB-->>R: Updated
        deactivate R
        
        LS->>LS: Check reorder rules
        alt Stock <= Reorder Point
            LS->>R: save(Alert)
            R->>DB: INSERT alert
        end
    end
    
    LS-->>S: LedgerResult
    deactivate LS
    S-->>C: ReceiveResponseDTO
    deactivate S
    C-->>U: 200 OK
    deactivate C
```

## 4. Stock Transfer

Transferring stock between locations. Involves creating a transfer, shipping (transfer out), and receiving (transfer in) via the LedgerService.

```mermaid
sequenceDiagram
    participant U as Browser/Angular
    participant C as REST Controller
    participant TS as Service Layer
    participant LS as InventoryLedgerService
    participant R as Repository
    participant DB as Oracle Database

    %% Create Transfer
    U->>C: POST /transfers (source, dest, items)
    activate C
    C->>TS: createTransfer(TransferDTO)
    activate TS
    TS->>R: check stock availability
    TS->>R: save(TRANSFER_RECORD [PENDING])
    TS-->>C: Transfer Details
    deactivate TS
    C-->>U: 201 Created
    deactivate C

    %% Ship Transfer (Out)
    U->>C: POST /transfers/{id}/ship
    activate C
    C->>TS: shipTransfer(id)
    activate TS
    TS->>LS: recordTransferOut(id)
    activate LS
    LS->>R: save(STOCK_TRANSACTION [TRANSFER_OUT])
    LS->>R: update INVENTORY_BALANCE (Source, deduct)
    LS-->>TS: LedgerResult
    deactivate LS
    TS->>R: update TRANSFER_RECORD [SHIPPED]
    TS-->>C: Transfer Details
    deactivate TS
    C-->>U: 200 OK
    deactivate C

    %% Receive Transfer (In)
    U->>C: POST /transfers/{id}/receive
    activate C
    C->>TS: receiveTransfer(id)
    activate TS
    TS->>LS: recordTransferIn(id)
    activate LS
    LS->>R: save(STOCK_TRANSACTION [TRANSFER_IN])
    LS->>R: update INVENTORY_BALANCE (Dest, add)
    LS-->>TS: LedgerResult
    deactivate LS
    TS->>R: update TRANSFER_RECORD [RECEIVED]
    TS-->>C: Transfer Details
    deactivate TS
    C-->>U: 200 OK
    deactivate C
```

## 5. Purchase Order Creation

Drafting a new purchase order for a supplier.

```mermaid
sequenceDiagram
    participant U as Browser/Angular
    participant C as REST Controller
    participant POS as Service Layer
    participant R as Repository
    participant DB as Oracle Database

    U->>C: POST /purchase-orders (PO details)
    activate C
    C->>POS: createPO(PurchaseOrderDTO)
    activate POS
    POS->>R: validate supplier & products
    activate R
    R->>DB: SELECT ...
    DB-->>R: Validated
    deactivate R
    
    POS->>POS: Map DTO to Entity, set status DRAFT
    
    POS->>R: save(PurchaseOrder)
    activate R
    R->>DB: INSERT INTO purchase_order
    DB-->>R: Created
    deactivate R
    
    POS->>POS: Map Entity to DTO
    POS-->>C: PurchaseOrderDTO
    deactivate POS
    C-->>U: 201 Created (PO DTO)
    deactivate C
```

## 6. Purchase Order Fulfillment

Approving a PO and receiving goods from a supplier. Items are added to inventory.

```mermaid
sequenceDiagram
    participant U as Browser/Angular
    participant C as REST Controller
    participant POS as Service Layer
    participant LS as InventoryLedgerService
    participant R as Repository
    participant DB as Oracle Database

    U->>C: POST /purchase-orders/{id}/receive
    activate C
    C->>POS: receivePO(id, receiveDetails)
    activate POS
    
    POS->>R: findById(id)
    activate R
    R->>DB: SELECT PO
    DB-->>R: PO Entity
    deactivate R
    
    loop For each received item
        POS->>LS: recordReceive(item, location)
        activate LS
        LS->>R: save(STOCK_TRANSACTION [RECEIVE])
        LS->>R: update INVENTORY_BALANCE (add qty)
        LS-->>POS: LedgerResult
        deactivate LS
    end
    
    POS->>POS: Update PO status (RECEIVED/COMPLETED)
    POS->>R: save(PO Entity)
    activate R
    R->>DB: UPDATE purchase_order
    DB-->>R: Updated
    deactivate R
    
    POS-->>C: Updated PurchaseOrderDTO
    deactivate POS
    C-->>U: 200 OK
    deactivate C
```

## 7. Order Processing

Customer creates an order, reserving and deducting stock via SALE transactions.

```mermaid
sequenceDiagram
    participant U as Browser/Angular
    participant C as REST Controller
    participant OS as Service Layer
    participant LS as InventoryLedgerService
    participant R as Repository
    participant DB as Oracle Database

    U->>C: POST /orders (Order Payload)
    activate C
    C->>OS: processOrder(OrderDTO)
    activate OS
    
    loop For each item
        OS->>R: validate stock availability
        R->>DB: SELECT INVENTORY_BALANCE
        DB-->>R: Balance
    end
    
    alt Stock Unavailable
        OS-->>C: InsufficientStockException
        C-->>U: 409 Conflict
    else Stock Available
        OS->>R: save(Order [PROCESSING])
        
        loop For each item
            OS->>LS: recordSale(item, location)
            activate LS
            LS->>R: save(STOCK_TRANSACTION [SALE])
            LS->>R: update INVENTORY_BALANCE (deduct)
            LS->>LS: check reorder rules
            LS-->>OS: LedgerResult
            deactivate LS
        end
        
        OS->>R: update Order [COMPLETED]
        OS-->>C: OrderResponseDTO
        C-->>U: 200 OK
    end
    deactivate OS
    deactivate C
```

## 8. Barcode Lookup

Client reads a GS1 barcode and fetches product details.

```mermaid
sequenceDiagram
    participant U as Browser/Angular
    participant C as REST Controller
    participant BS as Service Layer
    participant R as Repository
    participant DB as Oracle Database

    U->>U: Parse GS1 format
    U->>C: GET /barcode/{code}
    activate C
    C->>BS: resolveBarcode(code)
    activate BS
    
    BS->>R: ProductIdentifierRepository.findByCode(code)
    activate R
    R->>DB: SELECT * FROM product_identifiers WHERE code = ?
    activate DB
    DB-->>R: Identifier Entity
    deactivate DB
    R-->>BS: Identifier Entity
    deactivate R
    
    BS->>R: resolve to Product + Inventory Info
    activate R
    R->>DB: SELECT product, inventory ...
    DB-->>R: Product details
    deactivate R
    
    BS-->>C: ProductDetailsDTO
    deactivate BS
    C-->>U: 200 OK (Product Info)
    deactivate C
```

## 9. Inventory Balance Update (Optimistic Locking)

Handling concurrency when multiple users attempt to update the same inventory balance.

```mermaid
sequenceDiagram
    participant S as Service Layer
    participant LS as InventoryLedgerService
    participant R as Repository
    participant DB as Oracle Database

    S->>LS: updateStock(item, qty)
    activate LS
    
    LS->>R: findById(INVENTORY_BALANCE)
    activate R
    R->>DB: SELECT * FROM balance WHERE id = 1
    DB-->>R: { qty: 10, version: 1 }
    R-->>LS: Balance Entity
    deactivate R
    
    LS->>LS: Create STOCK_TRANSACTION
    LS->>LS: Update Balance Entity (qty = 5)
    
    LS->>R: save(Balance Entity)
    activate R
    R->>DB: UPDATE balance SET qty=5, version=2 WHERE id=1 AND version=1
    
    alt Another transaction updated first (version changed)
        DB-->>R: 0 rows updated
        R-->>LS: OptimisticLockException
        LS-->>S: Lock Conflict!
        Note over S: Service catches exception<br/>and retries the operation
        S->>S: Retry logic triggered
    else No conflict
        DB-->>R: 1 row updated
        R-->>LS: Saved Entity
        LS-->>S: Success
    end
    deactivate R
    deactivate LS
```

## 10. KPI Generation

Dashboard fetches metrics requiring multiple aggregate repository queries.

```mermaid
sequenceDiagram
    participant U as Browser/Angular
    participant C as REST Controller
    participant DS as Service Layer
    participant R as Repository
    participant DB as Oracle Database

    U->>C: GET /dashboard/kpis
    activate C
    C->>DS: generateKPIs()
    activate DS
    
    DS->>R: aggregateStockValue()
    activate R
    R->>DB: SELECT SUM(...)
    DB-->>R: Total Value
    R-->>DS: Total Value
    deactivate R
    
    DS->>R: countLowStockItems()
    activate R
    R->>DB: SELECT COUNT(...) WHERE qty < reorder_point
    DB-->>R: Low Stock Count
    R-->>DS: Low Stock Count
    deactivate R
    
    DS->>R: calculateTurnover()
    activate R
    R->>DB: Complex query for turnover
    DB-->>R: Turnover Rate
    R-->>DS: Turnover Rate
    deactivate R
    
    DS->>DS: Compile KPI Response DTO
    DS-->>C: DashboardKPIDTO
    deactivate DS
    C-->>U: 200 OK (KPI Data)
    deactivate C
```
