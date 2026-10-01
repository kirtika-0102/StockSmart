# StockSmart Activity Diagrams

Business process flowcharts for the **simplified** inventory model (no double-entry ledger, no optimistic locking).

---

## 1. Purchase order receiving

```mermaid
flowchart TD
    Start([Start]) --> OpenPO[Select PO]
    OpenPO --> Valid{Status allows receive?}
    Valid -->|No| E1[Show error]
    E1 --> End([End])
    Valid -->|Yes| Lines[For each line: qty to receive]
    Lines --> Svc[InventoryService increase stock]
    Svc --> UpdPO[Update quantity_received + PO status]
    UpdPO --> Alert{Below reorder level?}
    Alert -->|Yes| CreateAlert[Create LOW_STOCK alert if needed]
    Alert -->|No| End
    CreateAlert --> End
```

---

## 2. Stock adjustment

```mermaid
flowchart TD
    Start([Start]) --> Sel[Select product & location]
    Sel --> Enter[Enter adjustment + reason]
    Enter --> Neg{Decrease?}
    Neg -->|Yes| Enough{Qty sufficient?}
    Enough -->|No| E1[Error]
    E1 --> End([End])
    Enough -->|Yes| Apply
    Neg -->|No| Apply[Apply change via InventoryService]
    Apply --> Hist[Optional transaction row]
    Hist --> End
```

---

## 3. Stock transfer

```mermaid
flowchart TD
    Start([Start]) --> Create[Create transfer request]
    Create --> Complete[Complete transfer]
    Complete --> Src[Decrease source inventory]
    Src --> Dst[Increase destination inventory]
    Dst --> End([End])
```

---

## 4. Sales order fulfillment

```mermaid
flowchart TD
    Start([Start]) --> New[Create order]
    New --> Confirm[User confirms]
    Confirm --> Loop[For each line]
    Loop --> Stock{Enough stock?}
    Stock -->|No| Rollback[Abort / cancel]
    Rollback --> End([End])
    Stock -->|Yes| Out[Decrease inventory]
    Out --> More{More lines?}
    More -->|Yes| Loop
    More -->|No| Done[Mark COMPLETED]
    Done --> End
```

---

## 5. Barcode product lookup

```mermaid
flowchart TD
    Start([Scan]) --> Read[Read barcode string]
    Read --> API[Query by barcode]
    API --> Hit{Product exists?}
    Hit -->|No| NF[Show not found]
    NF --> End([End])
    Hit -->|Yes| UI[Show details / use in form]
    UI --> End
```

---

## Future diagrams (not MVP)

- Multi-step transfer approval chain
- RFID bulk reconcile
- Offline scan queue flush
