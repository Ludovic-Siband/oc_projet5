import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RegisterService } from './register.service';
import { environment } from '../../../environments/environment';
import { describe, beforeEach, afterEach, it, expect } from 'vitest';
import { lastValueFrom } from 'rxjs';


describe('RegisterService', () => {
  let service: RegisterService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [RegisterService],
    });

    service = TestBed.inject(RegisterService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('posts register payload', () => {
    service.register({ username: 'u', email: 'e', password: 'p' }).subscribe();

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/api/auth/register`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ username: 'u', email: 'e', password: 'p' });
    request.flush({ id: 1, email: 'e', username: 'u' });
  });

  it('maps validation errors', async () => {
    const request$ = service.register({ username: 'u', email: 'e', password: 'p' });
    const promise = lastValueFrom(request$).catch((error) => error);

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/api/auth/register`);
    request.flush({ message: 'Invalid', fields: { email: 'invalid' } }, { status: 400, statusText: 'Bad' });

    await expect(promise).resolves.toMatchObject({
      kind: 'validation',
      fields: { email: 'invalid' },
    });
  });

  it('maps conflict errors', async () => {
    const request$ = service.register({ username: 'u', email: 'e', password: 'p' });
    const promise = lastValueFrom(request$).catch((error) => error);

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/api/auth/register`);
    request.flush({ message: 'Conflict' }, { status: 409, statusText: 'Conflict' });

    await expect(promise).resolves.toMatchObject({
      kind: 'conflict',
    });
  });
});
