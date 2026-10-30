import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  BarcodeLookupResult,
  InventoryRecord,
  InventoryTransactionRecord,
  StockAdjustmentRequest,
  StockMovementRequest,
  UpdateReorderLevelRequest
} from '../models/inventory.models';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/inventory`;

  listAll(): Observable<InventoryRecord[]> {
    return this.http.get<InventoryRecord[]>(this.baseUrl);
  }

  listByLocation(locationId: number): Observable<InventoryRecord[]> {
    return this.http.get<InventoryRecord[]>(`${this.baseUrl}/locations/${locationId}`);
  }

  stockIn(request: StockMovementRequest): Observable<InventoryRecord> {
    return this.http.post<InventoryRecord>(`${this.baseUrl}/stock-in`, request);
  }

  stockOut(request: StockMovementRequest): Observable<InventoryRecord> {
    return this.http.post<InventoryRecord>(`${this.baseUrl}/stock-out`, request);
  }

  adjust(request: StockAdjustmentRequest): Observable<InventoryRecord> {
    return this.http.post<InventoryRecord>(`${this.baseUrl}/adjust`, request);
  }

  updateReorderLevel(request: UpdateReorderLevelRequest): Observable<InventoryRecord> {
    return this.http.put<InventoryRecord>(`${this.baseUrl}/reorder-level`, request);
  }

  lookupBarcode(barcode: string): Observable<BarcodeLookupResult> {
    return this.http.get<BarcodeLookupResult>(`${this.baseUrl}/barcode/${encodeURIComponent(barcode)}`);
  }

  listTransactions(): Observable<InventoryTransactionRecord[]> {
    return this.http.get<InventoryTransactionRecord[]>(`${this.baseUrl}/transactions`);
  }
}
