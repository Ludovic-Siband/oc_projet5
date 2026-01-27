import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, Observable, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiError } from '../../shared/models/api-error.model';
import { SubscribeError, SubjectItem } from './subjects.model';

@Injectable({ providedIn: 'root' })
export class SubjectsService {
  private readonly http = inject(HttpClient);

  listSubjects(): Observable<SubjectItem[]> {
    return this.http.get<SubjectItem[]>(`${environment.apiBaseUrl}/api/subjects`);
  }

  subscribe(subjectId: number): Observable<void> {
    return this.http
      .post<void>(`${environment.apiBaseUrl}/api/subjects/${subjectId}/subscribe`, {})
      .pipe(catchError((error: HttpErrorResponse) => throwError(() => this.toSubscribeError(error))));
  }

  private toSubscribeError(error: HttpErrorResponse): SubscribeError {
    const apiError = (error.error || {}) as ApiError;
    const message = apiError.message ?? 'Une erreur est survenue';

    if (error.status === 404) {
      return { kind: 'not-found', message };
    }

    if (error.status === 409) {
      return { kind: 'conflict', message };
    }

    return { kind: 'unknown', message };
  }
}
