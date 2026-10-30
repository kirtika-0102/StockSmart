import { InventoryTransactionRecord } from './inventory.models';
import { LocationType } from './location.models';

export interface CategoryStockSummary {
  categoryId: number | null;
  categoryName: string;
  productCount: number;
}

export interface LocationStockSummary {
  locationId: number;
  locationName: string;
  locationType: LocationType;
  totalQuantity: number;
  totalValue: number;
}

export interface DashboardKpiRecord {
  totalProducts: number;
  totalInventoryQuantity: number;
  totalInventoryValue: number;
  lowStockCount: number;
  outOfStockCount: number;
  activeAlertsCount: number;
  pendingPurchaseOrders: number;
  totalOrders: number;
  categoryDistribution: CategoryStockSummary[];
  locationDistribution: LocationStockSummary[];
  recentTransactions: InventoryTransactionRecord[];
}
