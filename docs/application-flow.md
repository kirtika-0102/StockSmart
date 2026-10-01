# StockSmart — Application Flow

Core workflows for the **college-level** implementation. Inventory changes run through **InventoryService** (update `INVENTORY.quantity`, optional `INVENTORY_TRANSACTION` row).

## 1. Application startup

```mermaid
flowchart TD
    Start((Start)) --> Init[Bootstrap Angular]
    Init --> Token{JWT present?}
    Token -->|Yes| Load[Load user / dashboard]
    Token -->|No| Login[/login]
```

## 2. Login

```mermaid
flowchart TD
    Start((Start)) --> Creds[Enter credentials]
    Creds --> POST[POST /api/auth/login]
    POST --> OK{Valid?}
    OK -->|No| Err[Show error]
    OK -->|Yes| Store[Store JWT]
    Store --> Dash[Navigate dashboard]
```

## 3. Product management

```mermaid
flowchart TD
    Start((Start)) --> List[List products]
    List --> CRUD{Action}
    CRUD --> Create[Form + POST /api/products]
    CRUD --> Edit[PUT /api/products/id]
    Create --> End((End))
    Edit --> End
```

Include **barcode** on create/edit; validate uniqueness.

## 4. PO receive → stock in

```mermaid
flowchart TD
    Start((Start)) --> SelectPO[Open PO]
    SelectPO --> Receive[Enter received qty per line]
    Receive --> API[POST /api/purchase-orders/id/receive]
    API --> Svc[InventoryService.stockIn per line]
    Svc --> Upd[Increase INVENTORY at PO location]
    Upd --> POStatus[Update PO status PARTIAL/RECEIVED]
    POStatus --> End((End))
```

## 5. Stock adjustment

```mermaid
flowchart TD
    Start((Start)) --> Pick[Product + location]
    Pick --> Qty[Enter delta or new qty + reason]
    Qty --> API[POST /api/inventory/adjust]
    API --> Svc[InventoryService.adjust]
    Svc --> Check{Sufficient stock if decrease?}
    Check -->|No| Fail[400 error]
    Check -->|Yes| Save[Update INVENTORY]
    Save --> End((End))
```

## 6. Stock transfer

```mermaid
flowchart TD
    Start((Start)) --> Form[Source, dest, lines]
    Form --> API[POST /api/transfers]
    API --> Complete[Complete transfer]
    Complete --> Svc[Deduct source, add destination]
    Svc --> End((End))
```

Optional two-step (PENDING → COMPLETED) — document chosen approach in implementation.

## 7. Sales order

```mermaid
flowchart TD
    Start((Start)) --> Create[Create order + items]
    Create --> Confirm[Confirm order]
    Confirm --> Check{Stock OK?}
    Check -->|No| Err[Reject]
    Check -->|Yes| Svc[InventoryService.stockOut]
    Svc --> Done[Status COMPLETED]
    Done --> End((End))
```

No payment gateway in MVP.

## 8. Low-stock detection

```mermaid
flowchart TD
    Start((After inventory change)) --> Cmp{quantity < reorderLevel?}
    Cmp -->|Yes| Alert[Create or refresh Alert]
    Cmp -->|No| End((End))
    Alert --> End
```

## 9. Barcode lookup

```mermaid
flowchart TD
    Start((Scan or type)) --> Input[Barcode value]
    Input --> API[GET product by barcode]
    API --> Found{Found?}
    Found -->|No| NF[Not found message]
    Found -->|Yes| Show[Show product / add to order]
```

No GS1 parsing step.

## 10. RFID (future — diagram only)

```mermaid
flowchart TD
    Start((Future)) --> Reader[RFID reader]
    Reader --> Middleware[Middleware]
    Middleware --> API[StockSmart API]
    API --> Inv[Update inventory]
```

Not implemented in MVP — see [barcode-rfid.md](./barcode-rfid.md).

## 11. KPI dashboard load

```mermaid
flowchart TD
    Start((Open dashboard)) --> Calls[Parallel GET KPI endpoints]
    Calls --> Bind[Bind to cards/charts]
    Bind --> End((End))
```

See [kpi-reporting.md](./kpi-reporting.md).
