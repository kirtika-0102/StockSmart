import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CreateOrderRequest,
  CreatePurchaseOrderRequest,
  CreateTransferRequest,
  OrderRecord,
  PurchaseOrderRecord,
  StockTransferRecord
} from '../models/workflow.models';

@Injectable({ providedIn: 'root' })
export class PurchaseOrderService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/purchase-orders`;

  listAll(): Observable<PurchaseOrderRecord[]> {
    return this.http.get<PurchaseOrderRecord[]>(this.baseUrl);
  }

  get(id: number): Observable<PurchaseOrderRecord> {
    return this.http.get<PurchaseOrderRecord>(`${this.baseUrl}/${id}`);
  }

  create(request: CreatePurchaseOrderRequest): Observable<PurchaseOrderRecord> {
    return this.http.post<PurchaseOrderRecord>(this.baseUrl, request);
  }

  receive(id: number): Observable<PurchaseOrderRecord> {
    return this.http.post<PurchaseOrderRecord>(`${this.baseUrl}/${id}/receive`, {});
  }

  cancel(id: number): Observable<PurchaseOrderRecord> {
    return this.http.post<PurchaseOrderRecord>(`${this.baseUrl}/${id}/cancel`, {});
  }
}

@Injectable({ providedIn: 'root' })
export class OrderService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/orders`;

  listAll(): Observable<OrderRecord[]> {
    return this.http.get<OrderRecord[]>(this.baseUrl);
  }

  get(id: number): Observable<OrderRecord> {
    return this.http.get<OrderRecord>(`${this.baseUrl}/${id}`);
  }

  create(request: CreateOrderRequest): Observable<OrderRecord> {
    return this.http.post<OrderRecord>(this.baseUrl, request);
  }

  confirm(id: number): Observable<OrderRecord> {
    return this.http.post<OrderRecord>(`${this.baseUrl}/${id}/confirm`, {});
  }

  cancel(id: number): Observable<OrderRecord> {
    return this.http.post<OrderRecord>(`${this.baseUrl}/${id}/cancel`, {});
  }
}

@Injectable({ providedIn: 'root' })
export class StockTransferService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/transfers`;

  listAll(): Observable<StockTransferRecord[]> {
    return this.http.get<StockTransferRecord[]>(this.baseUrl);
  }

  get(id: number): Observable<StockTransferRecord> {
    return this.http.get<StockTransferRecord>(`${this.baseUrl}/${id}`);
  }

  create(request: CreateTransferRequest): Observable<StockTransferRecord> {
    return this.http.post<StockTransferRecord>(this.baseUrl, request);
  }

  complete(id: number): Observable<StockTransferRecord> {
    return this.http.post<StockTransferRecord>(`${this.baseUrl}/${id}/complete`, {});
  }

  cancel(id: number): Observable<StockTransferRecord> {
    return this.http.post<StockTransferRecord>(`${this.baseUrl}/${id}/cancel`, {});
  }
}
