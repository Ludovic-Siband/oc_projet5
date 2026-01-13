import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, map, Observable, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiError } from '../../shared/models/api-error.model';
import { CreatePostError, CreatePostRequest, CreatePostResponse, SubjectOption } from './post-create.model';

type SubjectResponse = {
  id: number;
  name: string;
};

@Injectable({ providedIn: 'root' })
export class PostCreateService {
  private readonly http = inject(HttpClient);

  getSubjects(): Observable<SubjectOption[]> {
    return this.http.get<SubjectResponse[]>(`${environment.apiBaseUrl}/api/subjects`).pipe(
      map((subjects) => subjects.map((subject) => ({ id: subject.id, name: subject.name }))),
    );
  }

  createPost(payload: CreatePostRequest): Observable<CreatePostResponse> {
    return this.http.post<CreatePostResponse>(`${environment.apiBaseUrl}/api/posts`, payload).pipe(
      catchError((error: HttpErrorResponse) => throwError(() => this.toCreateError(error))),
    );
  }

  private toCreateError(error: HttpErrorResponse): CreatePostError {
    const apiError = (error.error || {}) as ApiError;
    const message = apiError.message ?? 'Une erreur est survenue';

    if (error.status === 400 && apiError.fields) {
      return { kind: 'validation', message, fields: apiError.fields };
    }

    if (error.status === 404) {
      return { kind: 'not-found', message };
    }

    return { kind: 'unknown', message };
  }
}
