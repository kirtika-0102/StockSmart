import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, CurrentUser, LoginRequest } from '../models/auth.models';

const TOKEN_KEY = 'access_token';
const USER_KEY = 'auth_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly userSubject = new BehaviorSubject<CurrentUser | null>(this.readStoredUser());
  readonly currentUser$ = this.userSubject.asObservable();

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiBaseUrl}/auth/login`, request).pipe(
      tap((response) => this.persistSession(response))
    );
  }

  logout(): void {
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(USER_KEY);
    this.userSubject.next(null);
    void this.router.navigateByUrl('/login');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getToken(): string | null {
    return sessionStorage.getItem(TOKEN_KEY);
  }

  currentUser(): CurrentUser | null {
    return this.userSubject.value;
  }

  hasRole(role: string): boolean {
    return this.userSubject.value?.roles.includes(role) ?? false;
  }

  private persistSession(response: AuthResponse): void {
    const user: CurrentUser = {
      username: response.username,
      fullName: response.fullName,
      roles: response.roles
    };
    sessionStorage.setItem(TOKEN_KEY, response.accessToken);
    sessionStorage.setItem(USER_KEY, JSON.stringify(user));
    this.userSubject.next(user);
  }

  private readStoredUser(): CurrentUser | null {
    const raw = sessionStorage.getItem(USER_KEY);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as CurrentUser;
    } catch {
      sessionStorage.removeItem(USER_KEY);
      return null;
    }
  }
}
