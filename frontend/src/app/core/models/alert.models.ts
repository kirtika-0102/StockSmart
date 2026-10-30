export type AlertType = 'LOW_STOCK' | 'OUT_OF_STOCK';
export type AlertStatus = 'ACTIVE' | 'ACKNOWLEDGED' | 'RESOLVED';

export interface AlertRecord {
  id: number;
  productId: number;
  productName: string;
  productSku: string;
  locationId: number;
  locationName: string;
  alertType: AlertType;
  message: string;
  status: AlertStatus;
  createdAt: string;
}
