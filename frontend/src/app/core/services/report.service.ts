import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { InventoryValuationItem } from '../models/report.models';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/reports`;

  getValuationReport(): Observable<InventoryValuationItem[]> {
    return this.http.get<InventoryValuationItem[]>(`${this.baseUrl}/valuation`);
  }
}
