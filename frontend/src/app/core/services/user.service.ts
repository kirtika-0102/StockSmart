import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CreateUserRequest, UpdateUserRequest, UserRecord } from '../models/user.models';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/users`;

  list(): Observable<UserRecord[]> {
    return this.http.get<UserRecord[]>(this.baseUrl);
  }

  create(request: CreateUserRequest): Observable<UserRecord> {
    return this.http.post<UserRecord>(this.baseUrl, request);
  }

  update(id: number, request: UpdateUserRequest): Observable<UserRecord> {
    return this.http.put<UserRecord>(`${this.baseUrl}/${id}`, request);
  }

  deactivate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
