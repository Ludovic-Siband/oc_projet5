import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { PostCreateService } from './post-create.service';
import { environment } from '../../../environments/environment';
import { describe, beforeEach, afterEach, it, expect } from 'vitest';
import { lastValueFrom } from 'rxjs';


describe('PostCreateService', () => {
  let service: PostCreateService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [PostCreateService],
    });

    service = TestBed.inject(PostCreateService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('filters subscribed subjects', () => {
    let result: unknown;
    service.getSubjects().subscribe((subjects) => (result = subjects));

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/api/subjects`);
    request.flush([
      { id: 1, name: 'A', subscribed: true },
      { id: 2, name: 'B', subscribed: false },
    ]);

    expect(result).toEqual([{ id: 1, name: 'A' }]);
  });

  it('posts create payload', () => {
    service.createPost({ subjectId: 1, title: 'Title', content: 'Body' }).subscribe();

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/api/posts`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ subjectId: 1, title: 'Title', content: 'Body' });
    request.flush({ id: 10 });
  });

  it('maps validation errors', async () => {
    const request$ = service.createPost({ subjectId: 1, title: 'Title', content: 'Body' });
    const promise = lastValueFrom(request$).catch((error) => error);

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/api/posts`);
    request.flush({ message: 'Invalid', fields: { title: 'required' } }, { status: 400, statusText: 'Bad' });

    await expect(promise).resolves.toMatchObject({
      kind: 'validation',
      fields: { title: 'required' },
    });
  });

  it('maps not found errors', async () => {
    const request$ = service.createPost({ subjectId: 1, title: 'Title', content: 'Body' });
    const promise = lastValueFrom(request$).catch((error) => error);

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/api/posts`);
    request.flush({ message: 'Not found' }, { status: 404, statusText: 'Not Found' });

    await expect(promise).resolves.toMatchObject({
      kind: 'not-found',
    });
  });
});
