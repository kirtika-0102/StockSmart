import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ProductRecord, ProductRequest } from '../models/product.models';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/products`;

  list(): Observable<ProductRecord[]> {
    return this.http.get<ProductRecord[]>(this.baseUrl);
  }

  get(id: number): Observable<ProductRecord> {
    return this.http.get<ProductRecord>(`${this.baseUrl}/${id}`);
  }

  findByBarcode(barcode: string): Observable<ProductRecord> {
    return this.http.get<ProductRecord>(`${this.baseUrl}/barcode/${encodeURIComponent(barcode)}`);
  }

  create(request: ProductRequest): Observable<ProductRecord> {
    return this.http.post<ProductRecord>(this.baseUrl, request);
  }

  update(id: number, request: ProductRequest): Observable<ProductRecord> {
    return this.http.put<ProductRecord>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
