import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { LoginService } from './login.service';
import { environment } from '../../../environments/environment';
import { describe, beforeEach, afterEach, it, expect } from 'vitest';
import { lastValueFrom } from 'rxjs';


describe('LoginService', () => {
  let service: LoginService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [LoginService],
    });

    service = TestBed.inject(LoginService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('posts login payload', () => {
    service.login({ identifier: 'user', password: 'pass' }).subscribe();

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/api/auth/login`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ identifier: 'user', password: 'pass' });
    request.flush({ accessToken: 'token', user: { id: 1, email: 'a', username: 'b' } });
  });

  it('maps validation errors', async () => {
    const request$ = service.login({ identifier: 'user', password: 'pass' });
    const promise = lastValueFrom(request$).catch((error) => error);

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/api/auth/login`);
    request.flush({ message: 'Invalid', fields: { identifier: 'required' } }, { status: 400, statusText: 'Bad' });

    await expect(promise).resolves.toMatchObject({
      kind: 'validation',
      fields: { identifier: 'required' },
    });
  });

  it('maps unauthorized errors', async () => {
    const request$ = service.login({ identifier: 'user', password: 'pass' });
    const promise = lastValueFrom(request$).catch((error) => error);

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/api/auth/login`);
    request.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });

    await expect(promise).resolves.toMatchObject({
      kind: 'unauthorized',
    });
  });
});
