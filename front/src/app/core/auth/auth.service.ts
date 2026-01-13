import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { finalize, map, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

type RefreshResponse = { accessToken: string };

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenKey = 'access_token';

  constructor(private readonly http: HttpClient) {}

  getAccessToken(): string | null {
    return this.safeStorageGet(this.tokenKey);
  }

  setAccessToken(token: string): void {
    this.safeStorageSet(this.tokenKey, token);
  }

  clearAccessToken(): void {
    this.safeStorageRemove(this.tokenKey);
  }

  refreshAccessToken(): Observable<string> {
    return this.http
      .post<RefreshResponse>(`${environment.apiBaseUrl}/api/auth/refresh`, {}, { withCredentials: true })
      .pipe(
        tap((response) => this.setAccessToken(response.accessToken)),
        map((response) => response.accessToken),
      );
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${environment.apiBaseUrl}/api/auth/logout`, {}, { withCredentials: true }).pipe(
      finalize(() => this.clearAccessToken()),
    );
  }

  private safeStorageGet(key: string): string | null {
    try {
      return localStorage.getItem(key);
    } catch {
      return null;
    }
  }

  private safeStorageSet(key: string, value: string): void {
    try {
      localStorage.setItem(key, value);
    } catch {
      // Ignore storage failures (private mode, SSR, etc.).
    }
  }

  private safeStorageRemove(key: string): void {
    try {
      localStorage.removeItem(key);
    } catch {
      // Ignore storage failures (private mode, SSR, etc.).
    }
  }
}
