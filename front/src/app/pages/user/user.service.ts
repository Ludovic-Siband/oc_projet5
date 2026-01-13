import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, Observable, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiError } from '../../shared/models/api-error.model';
import {
  UpdateUserRequest,
  UpdateUserResponse,
  UserProfile,
  UserUpdateError,
} from './user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);

  getProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${environment.apiBaseUrl}/api/users/me`);
  }

  updateProfile(payload: UpdateUserRequest): Observable<UpdateUserResponse> {
    return this.http.put<UpdateUserResponse>(`${environment.apiBaseUrl}/api/users/me`, payload).pipe(
      catchError((error: HttpErrorResponse) => throwError(() => this.toUpdateError(error))),
    );
  }

  unsubscribe(subjectId: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiBaseUrl}/api/subjects/${subjectId}/subscribe`);
  }

  private toUpdateError(error: HttpErrorResponse): UserUpdateError {
    const apiError = (error.error || {}) as ApiError;
    const message = apiError.message ?? 'Une erreur est survenue';

    if (error.status === 400 && apiError.fields) {
      return { kind: 'validation', message, fields: apiError.fields };
    }

    if (error.status === 409) {
      return { kind: 'conflict', message };
    }

    return { kind: 'unknown', message };
  }
}
