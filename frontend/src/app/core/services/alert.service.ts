import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AlertRecord, AlertStatus } from '../models/alert.models';

@Injectable({ providedIn: 'root' })
export class AlertService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/alerts`;

  list(status?: AlertStatus): Observable<AlertRecord[]> {
    let params = new HttpParams();
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<AlertRecord[]>(this.baseUrl, { params });
  }

  acknowledge(id: number): Observable<AlertRecord> {
    return this.http.post<AlertRecord>(`${this.baseUrl}/${id}/acknowledge`, {});
  }

  resolve(id: number): Observable<AlertRecord> {
    return this.http.post<AlertRecord>(`${this.baseUrl}/${id}/resolve`, {});
  }

  checkAlerts(): Observable<AlertRecord[]> {
    return this.http.post<AlertRecord[]>(`${this.baseUrl}/check`, {});
  }
}
