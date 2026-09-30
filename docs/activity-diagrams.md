# StockSmart Activity Diagrams

This document contains activity diagrams (modeled as Mermaid flowcharts) for major business processes in the StockSmart system. Each diagram documents the decision logic, error paths, and the double-entry ledger interactions.

---

## 1. Inventory Receiving Process

```mermaid
flowchart TD
    Start([Start]) --> Method{Receiving Method?}

    Method -->|PO-Based| SelectPO[Select Purchase Order]
    Method -->|Manual| SelectLoc[Select Location]

    SelectPO --> VerifyPO{PO Status Valid?}
    VerifyPO -->|No| ErrorPO[Display Error: Invalid PO]
    ErrorPO --> End1([End])
    VerifyPO -->|Yes| ScanItemsPO[Scan / Enter Items]
    ScanItemsPO --> ValidateQty{Qty <= PO Remaining?}
    ValidateQty -->|No| RecordDiscrep[Record Discrepancy]
    RecordDiscrep --> ProcessItem
    ValidateQty -->|Yes| ProcessItem

    SelectLoc --> EnterReason[Enter Receiving Reason]
    EnterReason --> ScanItemsManual[Scan / Enter Items and Quantities]
    ScanItemsManual --> ProcessItem

    ProcessItem[Process Each Item] --> CreateTx["Create STOCK_TRANSACTION (RECEIVE)"]
    CreateTx --> UpdateBal["Update INVENTORY_BALANCE (+qty, version++)"]
    UpdateBal --> VersionOk{Version Conflict?}
    VersionOk -->|Yes| Retry[Retry with Fresh Version]
    Retry --> UpdateBal
    VersionOk -->|No| CheckReorder{Balance <= Reorder Point?}
    CheckReorder -->|Yes| GenAlert[Generate/Update LOW_STOCK Alert]
    CheckReorder -->|No| MoreItems{More Items?}
    GenAlert --> MoreItems
    MoreItems -->|Yes| ProcessItem
    MoreItems -->|No| IsPO{Was PO-Based?}
    IsPO -->|Yes| UpdatePO[Update PO Item Received Quantities]
    UpdatePO --> AllReceived{All PO Items Received?}
    AllReceived -->|Yes| ClosePO["Update PO Status: COMPLETED"]
    AllReceived -->|No| PartialPO["Update PO Status: PARTIALLY_RECEIVED"]
    ClosePO --> End2([End])
    PartialPO --> End2
    IsPO -->|No| End2
```

---

## 2. Stock Adjustment Process

```mermaid
flowchart TD
    Start([Start]) --> SelectProduct[Select Product and Location]
    SelectProduct --> ViewBalance[View Current Balance]
    ViewBalance --> EnterAdj[Enter Adjustment Quantity]
    EnterAdj --> Direction{Positive or Negative?}
    Direction -->|Positive| TypeIn["Transaction Type: ADJUST_IN"]
    Direction -->|Negative| CheckStock{Sufficient Stock?}
    CheckStock -->|No| ErrorStock[Error: Insufficient Stock for Negative Adjustment]
    ErrorStock --> End1([End])
    CheckStock -->|Yes| TypeOut["Transaction Type: ADJUST_OUT"]

    TypeIn --> SelectReason
    TypeOut --> SelectReason[Select Reason Code]

    SelectReason --> EnterNotes[Enter Notes / Justification]
    EnterNotes --> Validate{Valid Input?}
    Validate -->|No| ErrorValid[Display Validation Errors]
    ErrorValid --> EnterAdj
    Validate -->|Yes| CreateTx["Create STOCK_TRANSACTION (ADJUST_IN/OUT)"]
    CreateTx --> UpdateBal["Update INVENTORY_BALANCE (version++)"]
    UpdateBal --> LogAudit["Log AUDIT_LOG Entry"]
    LogAudit --> CheckReorder{Balance <= Reorder Point?}
    CheckReorder -->|Yes| GenAlert[Generate LOW_STOCK Alert]
    CheckReorder -->|No| End2([End])
    GenAlert --> End2
```

---

## 3. Stock Transfer Process

```mermaid
flowchart TD
    Start([Start]) --> CreateReq[Create Transfer Request]
    CreateReq --> SelectLocs[Select Source and Destination Locations]
    SelectLocs --> AddItems[Add Products and Quantities]
    AddItems --> ValidateStock{Source Stock Sufficient?}
    ValidateStock -->|No| ErrorStock[Error: Insufficient Stock at Source]
    ErrorStock --> AddItems
    ValidateStock -->|Yes| SubmitApproval["Submit for Approval (Status: PENDING_APPROVAL)"]
    SubmitApproval --> ManagerReview{Manager Approves?}
    ManagerReview -->|Reject| Rejected["Status: CANCELLED"]
    Rejected --> End1([End])
    ManagerReview -->|Approve| Approved["Status: APPROVED"]

    Approved --> InitShip[Initiate Shipment]
    InitShip --> TxOut["Create STOCK_TRANSACTION (TRANSFER_OUT) at Source"]
    TxOut --> UpdateSrc["Update Source INVENTORY_BALANCE (-qty)"]
    UpdateSrc --> StatusTransit["Status: IN_TRANSIT"]

    StatusTransit --> DestReceive[Destination Receives Shipment]
    DestReceive --> VerifyItems{Items Match?}
    VerifyItems -->|Discrepancy| RecordDiscrep[Record Discrepancy]
    RecordDiscrep --> TxIn
    VerifyItems -->|Match| TxIn["Create STOCK_TRANSACTION (TRANSFER_IN) at Destination"]
    TxIn --> UpdateDest["Update Destination INVENTORY_BALANCE (+qty)"]
    UpdateDest --> StatusDone["Status: RECEIVED"]
    StatusDone --> End2([End])
```

---

## 4. Purchase Order Process

```mermaid
flowchart TD
    Start([Start]) --> IdentifyNeed{Need Source?}
    IdentifyNeed -->|Manual| ManualPO[Procurement Creates PO Manually]
    IdentifyNeed -->|Reorder Alert| FromAlert[Generate Draft PO from Reorder Rules]

    ManualPO --> SelectSupplier[Select Supplier]
    FromAlert --> SelectSupplier

    SelectSupplier --> AddProducts[Add Products with Quantities and Prices]
    AddProducts --> CalcTotals[Calculate Totals]
    CalcTotals --> SubmitPO["Submit PO (Status: DRAFT -> PENDING_APPROVAL)"]

    SubmitPO --> ManagerApproval{Manager Approves?}
    ManagerApproval -->|Reject| POReject["Status: CANCELLED"]
    POReject --> End1([End])
    ManagerApproval -->|Approve| POApproved["Status: APPROVED"]

    POApproved --> SendSupplier["Send to Supplier (Status: SENT)"]
    SendSupplier --> GoodsArrive[Goods Arrive at Location]
    GoodsArrive --> ReceiveGoods["Trigger Inventory Receiving Process"]
    ReceiveGoods --> CheckComplete{All Items Received?}
    CheckComplete -->|Partial| PartialStatus["Status: PARTIALLY_RECEIVED"]
    PartialStatus --> AwaitRemaining[Await Remaining Shipment]
    AwaitRemaining --> GoodsArrive
    CheckComplete -->|Full| CompleteStatus["Status: COMPLETED"]
    CompleteStatus --> End2([End])

    %% Cancellation paths
    SendSupplier -.->|Cancel| CancelSent["Status: CANCELLED"]
    CancelSent --> End1
```

---

## 5. Order Processing

```mermaid
flowchart TD
    Start([Start]) --> CreateOrder[Create Order]
    CreateOrder --> AddItems[Add Items to Order]
    AddItems --> SelectLoc[Select Fulfillment Location]
    SelectLoc --> CheckStock{Stock Sufficient for All Items?}

    CheckStock -->|Insufficient| Notify[Notify User]
    Notify --> Alternatives{Suggest Alternatives?}
    Alternatives -->|Other Location| SuggestLoc[Suggest Alternative Locations]
    Alternatives -->|Partial| PartialFulfill[Offer Partial Fulfillment]
    Alternatives -->|Cancel| CancelOrder["Cancel Order"]
    SuggestLoc --> SelectLoc
    PartialFulfill --> AdjustQty[Adjust Order Quantities]
    AdjustQty --> ReserveStock
    CancelOrder --> End1([End])

    CheckStock -->|Sufficient| ReserveStock[Reserve Stock]
    ReserveStock --> ProcessOrder["Process Order (Status: PROCESSING)"]
    ProcessOrder --> SaleTx["Create STOCK_TRANSACTION (SALE) per Item"]
    SaleTx --> UpdateBal["Update INVENTORY_BALANCE (-qty)"]
    UpdateBal --> CheckReorder{Balance <= Reorder Point?}
    CheckReorder -->|Yes| GenAlert[Generate LOW_STOCK Alert]
    CheckReorder -->|No| MarkComplete
    GenAlert --> MarkComplete["Mark Order COMPLETED"]
    MarkComplete --> End2([End])

    %% Cancellation after processing
    ProcessOrder -.->|Cancel| ReturnStock["Create STOCK_TRANSACTION (RETURN)"]
    ReturnStock -.-> RestoreBalance["Restore INVENTORY_BALANCE (+qty)"]
    RestoreBalance -.-> OrderCancelled["Status: CANCELLED"]
    OrderCancelled -.-> End1
```

---

## 6. Reorder Process

```mermaid
flowchart TD
    Start([Start: Balance Updated]) --> CheckRule{Reorder Rule Exists?}
    CheckRule -->|No| End1([End: No Action])
    CheckRule -->|Yes| BelowPoint{Balance <= Reorder Point?}
    BelowPoint -->|No| End1
    BelowPoint -->|Yes| AlertExists{Alert Already Active?}
    AlertExists -->|Yes| UpdateAlert[Update Existing Alert]
    AlertExists -->|No| CreateAlert["Create LOW_STOCK Alert"]

    UpdateAlert --> NotifyPM[Notify Procurement Manager]
    CreateAlert --> NotifyPM

    NotifyPM --> PMReview[Procurement Reviews Alert]
    PMReview --> Action{Reorder Action?}
    Action -->|Auto-Generate| AutoPO[Generate Draft PO from Reorder Rules]
    Action -->|Manual| ManualPO[Manually Create PO]
    Action -->|Dismiss| Dismiss[Acknowledge Alert - No Action]
    Dismiss --> End2([End])

    AutoPO --> SetQty["Set Recommended Qty (Reorder Quantity)"]
    ManualPO --> SetQty

    SetQty --> SelectSupplier["Select Preferred Supplier (from Supplier Products)"]
    SelectSupplier --> SubmitPO[Submit PO for Approval]
    SubmitPO --> POProcess["Continue to Purchase Order Process"]
    POProcess --> End2
```
