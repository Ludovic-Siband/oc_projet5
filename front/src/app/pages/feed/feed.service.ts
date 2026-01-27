import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FeedPost, FeedSort } from './feed.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class FeedService {
  private readonly http = inject(HttpClient);

  getFeed(sort: FeedSort): Observable<FeedPost[]> {
    return this.http.get<FeedPost[]>(`${environment.apiBaseUrl}/api/feed`, { params: { sort } });
  }
}
