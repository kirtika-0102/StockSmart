import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AddSupplierProductRequest, SupplierProductRecord, SupplierRecord, SupplierRequest } from '../models/supplier.models';

@Injectable({ providedIn: 'root' })
export class SupplierService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/suppliers`;

  list(): Observable<SupplierRecord[]> {
    return this.http.get<SupplierRecord[]>(this.baseUrl);
  }

  create(request: SupplierRequest): Observable<SupplierRecord> {
    return this.http.post<SupplierRecord>(this.baseUrl, request);
  }

  update(id: number, request: SupplierRequest): Observable<SupplierRecord> {
    return this.http.put<SupplierRecord>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  listProducts(supplierId: number): Observable<SupplierProductRecord[]> {
    return this.http.get<SupplierProductRecord[]>(`${this.baseUrl}/${supplierId}/products`);
  }

  linkProduct(supplierId: number, request: AddSupplierProductRequest): Observable<SupplierProductRecord> {
    return this.http.post<SupplierProductRecord>(`${this.baseUrl}/${supplierId}/products`, request);
  }

  unlinkProduct(supplierId: number, productId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${supplierId}/products/${productId}`);
  }
}
