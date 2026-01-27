import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { PostDetailService } from './post-detail.service';
import { environment } from '../../../environments/environment';
import { describe, beforeEach, afterEach, it, expect } from 'vitest';


describe('PostDetailService', () => {
  let service: PostDetailService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [PostDetailService],
    });

    service = TestBed.inject(PostDetailService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('gets post detail', () => {
    service.getPost(12).subscribe();

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/api/posts/12`);
    expect(request.request.method).toBe('GET');
    request.flush({
      id: 12,
      subject: { id: 1, name: 'Test' },
      title: 'Title',
      content: 'Body',
      author: 'Me',
      createdAt: '2024-01-01',
      comments: [],
    });
  });

  it('posts a comment', () => {
    service.addComment(12, { content: 'Hello' }).subscribe();

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/api/posts/12/comments`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ content: 'Hello' });
    request.flush(null);
  });
});
