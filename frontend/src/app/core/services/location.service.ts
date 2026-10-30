import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LocationRecord, LocationRequest } from '../models/location.models';

@Injectable({ providedIn: 'root' })
export class LocationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/locations`;

  list(): Observable<LocationRecord[]> {
    return this.http.get<LocationRecord[]>(this.baseUrl);
  }

  create(request: LocationRequest): Observable<LocationRecord> {
    return this.http.post<LocationRecord>(this.baseUrl, request);
  }

  update(id: number, request: LocationRequest): Observable<LocationRecord> {
    return this.http.put<LocationRecord>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
