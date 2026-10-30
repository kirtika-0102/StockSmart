import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CategoryRecord, CategoryRequest } from '../models/category.models';

@Injectable({ providedIn: 'root' })
export class CategoryService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/categories`;

  list(): Observable<CategoryRecord[]> {
    return this.http.get<CategoryRecord[]>(this.baseUrl);
  }

  create(request: CategoryRequest): Observable<CategoryRecord> {
    return this.http.post<CategoryRecord>(this.baseUrl, request);
  }

  update(id: number, request: CategoryRequest): Observable<CategoryRecord> {
    return this.http.put<CategoryRecord>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
