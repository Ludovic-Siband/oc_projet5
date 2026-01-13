import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CreateCommentRequest, PostDetail } from './post-detail.model';

@Injectable({ providedIn: 'root' })
export class PostDetailService {
  private readonly http = inject(HttpClient);

  getPost(postId: number): Observable<PostDetail> {
    return this.http.get<PostDetail>(`${environment.apiBaseUrl}/api/posts/${postId}`);
  }

  addComment(postId: number, payload: CreateCommentRequest): Observable<void> {
    return this.http.post<void>(`${environment.apiBaseUrl}/api/posts/${postId}/comments`, payload);
  }
}
