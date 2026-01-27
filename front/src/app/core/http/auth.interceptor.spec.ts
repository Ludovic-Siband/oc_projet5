import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { authInterceptor } from './auth.interceptor';
import { environment } from '../../../environments/environment';
import { describe, beforeEach, afterEach, it, expect, vi } from 'vitest';


describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let authService: AuthService;
  const router = { navigateByUrl: vi.fn() };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        AuthService,
        { provide: Router, useValue: router },
      ],
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
    router.navigateByUrl.mockReset();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('passes through non API requests', () => {
    http.get('https://example.com/health').subscribe();

    const request = httpMock.expectOne('https://example.com/health');
    expect(request.request.withCredentials).toBe(false);
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({ ok: true });
  });

  it('adds bearer token for API requests', () => {
    vi.spyOn(authService, 'getAccessToken').mockReturnValue('token');

    http.get(`${environment.apiBaseUrl}/api/feed`).subscribe();

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/api/feed`);
    expect(request.request.withCredentials).toBe(true);
    expect(request.request.headers.get('Authorization')).toBe('Bearer token');
    request.flush([]);
  });

  it('does not add bearer token for auth endpoints', () => {
    vi.spyOn(authService, 'getAccessToken').mockReturnValue('token');

    http.post(`${environment.apiBaseUrl}/api/auth/login`, { identifier: 'a', password: 'b' }).subscribe();

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/api/auth/login`);
    expect(request.request.withCredentials).toBe(true);
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({ accessToken: 'token' });
  });

  it('refreshes token and retries on 401', () => {
    vi.spyOn(authService, 'getAccessToken').mockReturnValue('old-token');
    vi.spyOn(authService, 'setAccessToken').mockImplementation(() => undefined);

    let response: unknown;
    http.get(`${environment.apiBaseUrl}/api/subjects`).subscribe((value) => (response = value));

    const initial = httpMock.expectOne(`${environment.apiBaseUrl}/api/subjects`);
    initial.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });

    const refresh = httpMock.expectOne(`${environment.apiBaseUrl}/api/auth/refresh`);
    expect(refresh.request.method).toBe('POST');
    refresh.flush({ accessToken: 'new-token' });

    const retry = httpMock.expectOne(`${environment.apiBaseUrl}/api/subjects`);
    expect(retry.request.headers.get('Authorization')).toBe('Bearer new-token');
    retry.flush([{ id: 1 }]);

    expect(response).toEqual([{ id: 1 }]);
  });

  it('redirects to login when refresh fails', () => {
    vi.spyOn(authService, 'getAccessToken').mockReturnValue('old-token');
    vi.spyOn(authService, 'clearAccessToken').mockImplementation(() => undefined);

    http.get(`${environment.apiBaseUrl}/api/subjects`).subscribe({
      error: () => undefined,
    });

    const initial = httpMock.expectOne(`${environment.apiBaseUrl}/api/subjects`);
    initial.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });

    const refresh = httpMock.expectOne(`${environment.apiBaseUrl}/api/auth/refresh`);
    refresh.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });

    expect(router.navigateByUrl).toHaveBeenCalledWith('/login');
  });
});
