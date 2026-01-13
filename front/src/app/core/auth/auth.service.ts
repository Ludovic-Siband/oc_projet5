import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable, tap } from 'rxjs';

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
      .post<RefreshResponse>('/api/auth/refresh', {}, { withCredentials: true })
      .pipe(
        tap((response) => this.setAccessToken(response.accessToken)),
        map((response) => response.accessToken),
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
