import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, Observable, throwError } from 'rxjs';
import { ApiError } from '../../shared/models/api-error.model';
import { LoginError, LoginRequest, LoginResponse } from './login.model';

@Injectable({ providedIn: 'root' })
export class LoginService {
  private readonly http = inject(HttpClient);

  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', payload).pipe(
      catchError((error: HttpErrorResponse) => throwError(() => this.toLoginError(error))),
    );
  }

  private toLoginError(error: HttpErrorResponse): LoginError {
    const apiError = (error.error || {}) as ApiError;
    const message = apiError.message ?? 'Une erreur est survenue';

    if (error.status === 400 && apiError.fields) {
      return { kind: 'validation', message, fields: apiError.fields };
    }

    if (error.status === 401) {
      return { kind: 'unauthorized', message };
    }

    return { kind: 'unknown', message };
  }
}
