import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { UserService } from './user.service';
import { environment } from '../../../environments/environment';
import { describe, beforeEach, afterEach, it, expect } from 'vitest';
import { lastValueFrom } from 'rxjs';


describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [UserService],
    });

    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('gets profile', () => {
    service.getProfile().subscribe();

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/api/users/me`);
    expect(request.request.method).toBe('GET');
    request.flush({
      id: 1,
      email: 'a@b.c',
      username: 'name',
      subscriptions: [],
    });
  });

  it('updates profile', () => {
    service.updateProfile({ email: 'a@b.c' }).subscribe();

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/api/users/me`);
    expect(request.request.method).toBe('PUT');
    request.flush({ id: 1, email: 'a@b.c', username: 'name' });
  });

  it('maps validation errors', async () => {
    const request$ = service.updateProfile({ email: 'bad' });
    const promise = lastValueFrom(request$).catch((error) => error);

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/api/users/me`);
    request.flush({ message: 'Invalid', fields: { email: 'invalid' } }, { status: 400, statusText: 'Bad' });

    await expect(promise).resolves.toMatchObject({
      kind: 'validation',
      fields: { email: 'invalid' },
    });
  });

  it('maps conflict errors', async () => {
    const request$ = service.updateProfile({ email: 'a@b.c' });
    const promise = lastValueFrom(request$).catch((error) => error);

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/api/users/me`);
    request.flush({ message: 'Conflict' }, { status: 409, statusText: 'Conflict' });

    await expect(promise).resolves.toMatchObject({
      kind: 'conflict',
    });
  });

  it('unsubscribes from subject', () => {
    service.unsubscribe(3).subscribe();

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/api/subjects/3/subscribe`);
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });
});
