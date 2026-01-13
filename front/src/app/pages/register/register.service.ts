import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, Observable, throwError } from 'rxjs';
import { ApiError } from '../../shared/models/api-error.model';
import { RegisterError, RegisterRequest, RegisterResponse } from './register.model';

@Injectable({ providedIn: 'root' })
export class RegisterService {
  private readonly http = inject(HttpClient);

  register(payload: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>('/api/auth/register', payload).pipe(
      catchError((error: HttpErrorResponse) => throwError(() => this.toRegisterError(error))),
    );
  }

  private toRegisterError(error: HttpErrorResponse): RegisterError {
    const apiError = (error.error || {}) as ApiError;
    const message = apiError.message ?? 'Une erreur est survenue, veuillez recommencer.';

    if (error.status === 400 && apiError.fields) {
      return { kind: 'validation', message, fields: apiError.fields };
    }

    if (error.status === 409) {
      return { kind: 'conflict', message };
    }

    return { kind: 'unknown', message };
  }
}
