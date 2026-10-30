import { LocationType } from './location.models';
import { ProductRecord } from './product.models';

export interface InventoryRecord {
  id: number;
  productId: number;
  productName: string;
  productSku: string;
  barcode: string | null;
  locationId: number;
  locationName: string;
  locationType: LocationType;
  quantity: number;
  reorderLevel: number;
  lowStock: boolean;
}

export interface StockMovementRequest {
  locationId: number;
  productId: number;
  quantity: number;
  referenceType: string | null;
  referenceId: string | null;
  reason: string | null;
}

export interface StockAdjustmentRequest {
  locationId: number;
  productId: number;
  newQuantity: number;
  reason: string | null;
}

export interface UpdateReorderLevelRequest {
  locationId: number;
  productId: number;
  reorderLevel: number;
}

export interface BarcodeLookupResult {
  product: ProductRecord;
  totalQuantity: number;
  balances: InventoryRecord[];
}

export interface InventoryTransactionRecord {
  id: number;
  productId: number;
  productName: string;
  productSku: string;
  locationId: number;
  locationName: string;
  transactionType: string;
  quantityChange: number;
  referenceType: string | null;
  referenceId: string | null;
  username: string | null;
  createdAt: string;
}
