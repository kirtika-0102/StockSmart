# StockSmart - Application Flow Documentation

This document outlines the core business workflows and technical execution paths in the StockSmart system. 

## 1. Application Startup
```mermaid
flowchart TD
    Start((Start App)) --> InitApp[Initialize Angular App]
    InitApp --> CheckToken{Has JWT Token?}
    CheckToken -- Yes --> ValidateToken[Validate Token]
    ValidateToken --> IsValid{Is Token Valid?}
    IsValid -- Yes --> LoadUser[Load User Data]
    LoadUser --> RouteDash[Route to Dashboard]
    IsValid -- No --> ClearToken[Clear Invalid Token]
    ClearToken --> RouteLogin
    CheckToken -- No --> RouteLogin[Route to Login Page]
    RouteLogin --> End((Wait for User Input))
    RouteDash --> End
```

## 2. Registration
```mermaid
flowchart TD
    Start((Start)) --> FillForm[User Fills Registration Form]
    FillForm --> ValidateClient{Client-side Valid?}
    ValidateClient -- No --> ShowErrors[Display Form Errors]
    ShowErrors --> FillForm
    ValidateClient -- Yes --> SubmitAPI[Submit POST /api/v1/auth/register]
    SubmitAPI --> APIValidate{Server Valid?}
    APIValidate -- No --> Return400[Return 400 Bad Request]
    Return400 --> ShowErrors
    APIValidate -- Yes --> HashPwd[Hash Password]
    HashPwd --> CreateUser[Create User Record]
    CreateUser --> AssignRole[Assign Default Role]
    AssignRole --> Return201[Return 201 Created]
    Return201 --> RouteLogin[Route to Login]
    RouteLogin --> End((End))
```

## 3. Login
```mermaid
flowchart TD
    Start((Start)) --> EnterCreds[Enter Credentials]
    EnterCreds --> Submit[Submit POST /api/v1/auth/login]
    Submit --> VerifyUser{Valid Credentials?}
    VerifyUser -- No --> Return401[Return 401 Unauthorized]
    Return401 --> ShowError[Show Error Message]
    ShowError --> EnterCreds
    VerifyUser -- Yes --> GenJWT[Generate JWT Token]
    GenJWT --> ReturnToken[Return Token in Response]
    ReturnToken --> StoreToken[Store Token in LocalStorage/Session]
    StoreToken --> RouteDash[Redirect to Dashboard]
    RouteDash --> End((End))
```

## 4. Authentication Flow
```mermaid
flowchart TD
    Start((Client Request)) --> SendReq[Send Request with Bearer JWT]
    SendReq --> Filter[Spring Security AuthFilter]
    Filter --> HasToken{Has Token?}
    HasToken -- No --> Block401[Return 401 Unauthorized]
    HasToken -- Yes --> ParseToken[Parse & Verify Signature]
    ParseToken --> IsExpired{Is Expired?}
    IsExpired -- Yes --> CheckRefresh{Can Refresh?}
    CheckRefresh -- Yes --> Refresh[Refresh Token Flow]
    Refresh --> ContReq
    CheckRefresh -- No --> Block401
    IsExpired -- No --> ExtractUser[Extract User & Authorities]
    ExtractUser --> SetSecurity[Set SecurityContextHolder]
    SetSecurity --> ContReq[Continue to DispatcherServlet]
    ContReq --> End((Next Filter/Controller))
```

## 5. Authorization Flow
```mermaid
flowchart TD
    Start((Controller Method)) --> CheckPreAuth["@PreAuthorize annotation check"]
    CheckPreAuth --> HasRole{User has required Role/Authority?}
    HasRole -- No --> Return403[Return 403 Forbidden]
    HasRole -- Yes --> HasPermission{User has resource permission?}
    HasPermission -- No --> Return403
    HasPermission -- Yes --> Execute[Execute Business Logic]
    Execute --> End((Return Response))
```

## 6. Product Management
```mermaid
flowchart TD
    Start((Start)) --> Action{Choose Action}
    Action -- List --> FetchProducts[GET /api/v1/products]
    FetchProducts --> DisplayList[Display Product Grid]
    Action -- Create --> ShowCreate[Show Create Form]
    ShowCreate --> EnterDetails[Enter Product Info]
    EnterDetails --> AssignMeta[Assign Category, Brand, Identifiers]
    AssignMeta --> SubmitCreate[POST /api/v1/products]
    SubmitCreate --> SaveProduct[Save Product in DB]
    SaveProduct --> End((End))
    Action -- Edit --> FetchProduct[GET /api/v1/products/id]
    FetchProduct --> ShowEdit[Show Edit Form]
    ShowEdit --> UpdateDetails[Update Info]
    UpdateDetails --> SubmitUpdate[PUT /api/v1/products/id]
    SubmitUpdate --> SaveChanges[Save Changes]
    SaveChanges --> End
```

## 7. Inventory Receiving
```mermaid
flowchart TD
    Start((Start)) --> SelectMode{Select Mode}
    SelectMode -- PO Receive --> SelectPO[Select Pending PO]
    SelectMode -- Manual --> ManualEntry[Manual Item Entry]
    SelectPO --> ScanItems[Scan or Enter Items]
    ManualEntry --> ScanItems
    ScanItems --> BufferClient[Buffer in Client]
    BufferClient --> SubmitReceive[Submit Receiving Payload with Idempotency Key]
    SubmitReceive --> ValidateQty[Validate Quantities]
    ValidateQty --> LockDB[Lock INVENTORY_BALANCE Optimistically]
    LockDB --> CreateTrans[Create STOCK_TRANSACTION: RECEIVE]
    CreateTrans --> UpdateBal[Update INVENTORY_BALANCE]
    UpdateBal --> SaveDB[Commit Transaction]
    SaveDB --> End((End))
```

## 8. Inventory Adjustment
```mermaid
flowchart TD
    Start((Start)) --> SelectItem[Select Product & Location]
    SelectItem --> EnterReason[Enter Adjustment Reason]
    EnterReason --> EnterQty[Enter Quantity Diff]
    EnterQty --> SubmitAdj[Submit Adjustment API]
    SubmitAdj --> DetermineType{Positive or Negative?}
    DetermineType -- Positive --> CreateAdjIn[Create STOCK_TRANSACTION: ADJUST_IN]
    DetermineType -- Negative --> CreateAdjOut[Create STOCK_TRANSACTION: ADJUST_OUT]
    CreateAdjIn --> UpdateBal[Update INVENTORY_BALANCE]
    CreateAdjOut --> UpdateBal
    UpdateBal --> Commit[Commit Transaction]
    Commit --> End((End))
```

## 9. Stock Transfer
```mermaid
flowchart TD
    Start((Start)) --> CreateReq[Create Transfer Request]
    CreateReq --> ApproveReq[Approve Transfer]
    ApproveReq --> ShipGoods[Ship Goods from Source]
    ShipGoods --> TransOut[Create STOCK_TRANSACTION: TRANSFER_OUT at Source]
    TransOut --> UpdateSrc[Update Source INVENTORY_BALANCE]
    UpdateSrc --> MarkInTransit[Mark Status as IN_TRANSIT]
    MarkInTransit --> ReceiveDest[Receive at Destination]
    ReceiveDest --> TransIn[Create STOCK_TRANSACTION: TRANSFER_IN at Dest]
    TransIn --> UpdateDest[Update Destination INVENTORY_BALANCE]
    UpdateDest --> CloseTrans[Close Transfer Request]
    CloseTrans --> End((End))
```

## 10. Purchase Order
```mermaid
flowchart TD
    Start((Start)) --> CreatePO[Create Draft PO]
    CreatePO --> AddLines[Select Supplier & Add Products]
    AddLines --> SubmitPO[Submit for Approval]
    SubmitPO --> ReviewPO[Manager Reviews PO]
    ReviewPO --> Decision{Approved?}
    Decision -- No --> Reject[Mark Rejected]
    Reject --> End((End))
    Decision -- Yes --> Approve[Approve PO]
    Approve --> SendSupplier[Send to Supplier]
    SendSupplier --> WaitGoods[Wait for Goods]
    WaitGoods --> ReceiveWorkflow[Trigger Inventory Receiving Workflow]
    ReceiveWorkflow --> ClosePO[Close PO]
    ClosePO --> End
```

## 11. Order Processing
```mermaid
flowchart TD
    Start((Start Order)) --> CreateOrder[Create Order]
    CreateOrder --> CheckAvail[Check Stock Availability]
    CheckAvail --> HasStock{Stock Available?}
    HasStock -- No --> ReturnErr[Return Out of Stock Error]
    ReturnErr --> End((End))
    HasStock -- Yes --> ReserveStock[Reserve Stock]
    ReserveStock --> ProcessPay[Process Payment]
    ProcessPay --> PaySuccess{Payment Success?}
    PaySuccess -- No --> ReleaseStock[Release Reserved Stock]
    ReleaseStock --> End
    PaySuccess -- Yes --> CreateSale[Create STOCK_TRANSACTION: SALE]
    CreateSale --> UpdateBal[Update INVENTORY_BALANCE]
    UpdateBal --> CompleteOrder[Complete Order]
    CompleteOrder --> End
```

## 12. Low-Stock Detection
```mermaid
flowchart TD
    Start((Balance Updated)) --> FetchRules[Fetch Reorder Rules for Product/Location]
    FetchRules --> CheckThreshold{Balance < Threshold?}
    CheckThreshold -- No --> End((No Action))
    CheckThreshold -- Yes --> CheckAlert[Check if Active Alert Exists]
    CheckAlert --> HasAlert{Alert Exists?}
    HasAlert -- Yes --> End
    HasAlert -- No --> CreateAlert[Generate Low-Stock Alert]
    CreateAlert --> Notify[Notify Relevant Users]
    Notify --> End
```

## 13. Reorder Workflow
```mermaid
flowchart TD
    Start((Start)) --> ReviewAlerts[Review Low-Stock Alerts]
    ReviewAlerts --> SelectAlert[Select Alert for Reorder]
    SelectAlert --> Decide{Decide to Reorder?}
    Decide -- No --> Dismiss[Dismiss/Snooze Alert]
    Dismiss --> End((End))
    Decide -- Yes --> AutoGen[Auto-generate Draft PO from Rules]
    AutoGen --> ModifyPO[User Can Modify PO]
    ModifyPO --> SubmitPO[Submit PO for Approval]
    SubmitPO --> End
```

## 14. Barcode Workflow
```mermaid
flowchart TD
    Start((Start Scan)) --> Capture[Capture via Camera or Keyboard Wedge]
    Capture --> ParseGS1[Parse GS1 Format]
    ParseGS1 --> Lookup[Lookup Product by Identifier]
    Lookup --> Found{Product Found?}
    Found -- No --> ShowErr[Display 'Not Found' Error]
    ShowErr --> End((End))
    Found -- Yes --> DisplayInfo[Display Product Info]
    DisplayInfo --> Context{Current App Context?}
    Context -- Receiving --> AddToReceive[Add to Receive List]
    Context -- Adjustment --> OpenAdj[Open Adjustment Form]
    Context -- POS/Sale --> AddToCart[Add to Cart]
    Context -- Inquiry --> End
    AddToReceive --> End
    OpenAdj --> End
    AddToCart --> End
```

## 15. RFID-Ready Workflow
```mermaid
flowchart TD
    Start((RFID Scan Event)) --> ReceiveTags[Reader Sends Tag Data]
    ReceiveTags --> ParseSGTIN[Parse EPC SGTIN-96]
    ParseSGTIN --> Resolve[Resolve to Product Identifiers]
    Resolve --> BatchProcessor[Batch Process Multiple Tags]
    BatchProcessor --> FetchExpected[Fetch Expected Inventory]
    FetchExpected --> Reconcile[Reconcile Scanned vs Expected]
    Reconcile --> HasDiff{Discrepancies?}
    HasDiff -- Yes --> FlagVariances[Flag Variances for Review]
    FlagVariances --> End((End))
    HasDiff -- No --> MarkVerified[Mark Inventory Verified]
    MarkVerified --> End
```

## 16. KPI Generation
```mermaid
flowchart TD
    Start((Start)) --> LoadDash[Load Dashboard]
    LoadDash --> QueryDB[Query Inventory Data]
    QueryDB --> AggMetrics[Aggregate Metrics]
    AggMetrics --> CalcTurn[Calculate Stock Turnover]
    AggMetrics --> CalcVal[Calculate Total Stock Value]
    AggMetrics --> CalcShrink[Calculate Shrinkage]
    CalcTurn --> Combine[Combine KPI Results]
    CalcVal --> Combine
    CalcShrink --> Combine
    Combine --> ReturnUI[Return to Dashboard UI]
    ReturnUI --> End((End))
```

## 17. Reporting
```mermaid
flowchart TD
    Start((Start)) --> SelectReport[Select Report Type]
    SelectReport --> ConfigParams[Configure Parameters: Date, Location, Category]
    ConfigParams --> SubmitReq[Submit Report Request]
    SubmitReq --> GenReport[Generate Report from DB]
    GenReport --> FormatRes[Format Results]
    FormatRes --> Action{User Action}
    Action -- View --> DisplayGrid[Display in Data Grid]
    Action -- Export --> ExportDoc[Export as CSV/PDF]
    DisplayGrid --> End((End))
    ExportDoc --> End
```

## 18. Logout
```mermaid
flowchart TD
    Start((Start)) --> ClickLogout[Click Logout]
    ClickLogout --> SendReq[Optional: Send Logout to API]
    SendReq --> ClearState[Clear Local State]
    ClearState --> ClearToken[Clear JWT Token]
    ClearToken --> RouteLogin[Redirect to Login]
    RouteLogin --> End((End))
```
