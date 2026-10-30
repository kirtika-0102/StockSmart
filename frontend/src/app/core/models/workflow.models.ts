export type PurchaseOrderStatus = 'DRAFT' | 'SUBMITTED' | 'RECEIVED' | 'CANCELLED';
export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';
export type TransferStatus = 'PENDING' | 'COMPLETED' | 'CANCELLED';

// Purchase Orders
export interface PurchaseOrderItemRecord {
  id: number;
  productId: number;
  productName: string;
  productSku: string;
  quantityOrdered: number;
  quantityReceived: number;
  unitCost: number | null;
}

export interface PurchaseOrderRecord {
  id: number;
  supplierId: number;
  supplierName: string;
  locationId: number;
  locationName: string;
  status: PurchaseOrderStatus;
  orderDate: string;
  items: PurchaseOrderItemRecord[];
  createdAt: string;
}

export interface CreatePurchaseOrderItemRequest {
  productId: number;
  quantity: number;
  unitCost: number | null;
}

export interface CreatePurchaseOrderRequest {
  supplierId: number;
  locationId: number;
  items: CreatePurchaseOrderItemRequest[];
}

// Sales Orders
export interface OrderItemRecord {
  id: number;
  productId: number;
  productName: string;
  productSku: string;
  quantity: number;
  unitPrice: number | null;
}

export interface OrderRecord {
  id: number;
  locationId: number;
  locationName: string;
  status: OrderStatus;
  orderDate: string;
  totalAmount: number;
  items: OrderItemRecord[];
  createdAt: string;
}

export interface CreateOrderItemRequest {
  productId: number;
  quantity: number;
  unitPrice: number | null;
}

export interface CreateOrderRequest {
  locationId: number;
  items: CreateOrderItemRequest[];
}

// Stock Transfers
export interface StockTransferItemRecord {
  id: number;
  productId: number;
  productName: string;
  productSku: string;
  quantity: number;
}

export interface StockTransferRecord {
  id: number;
  sourceLocationId: number;
  sourceLocationName: string;
  destLocationId: number;
  destLocationName: string;
  status: TransferStatus;
  items: StockTransferItemRecord[];
  createdAt: string;
}

export interface CreateTransferItemRequest {
  productId: number;
  quantity: number;
}

export interface CreateTransferRequest {
  sourceLocationId: number;
  destLocationId: number;
  items: CreateTransferItemRequest[];
}
