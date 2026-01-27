import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';
import { describe, beforeEach, afterEach, it, expect, vi } from 'vitest';


describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.removeItem('access_token');
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.removeItem('access_token');
  });

  it('stores and clears the access token', () => {
    service.setAccessToken('abc');
    expect(service.getAccessToken()).toBe('abc');

    service.clearAccessToken();
    expect(service.getAccessToken()).toBeNull();
  });

  it('returns null when storage access throws', () => {
    const getSpy = vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
      throw new Error('fail');
    });

    expect(service.getAccessToken()).toBeNull();

    getSpy.mockRestore();
  });

  it('refreshes the access token', () => {
    const setSpy = vi.spyOn(service, 'setAccessToken').mockImplementation(() => undefined);
    let received: string | null = null;

    service.refreshAccessToken().subscribe((token) => (received = token));

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/api/auth/refresh`);
    expect(request.request.method).toBe('POST');
    expect(request.request.withCredentials).toBe(true);
    request.flush({ accessToken: 'new-token' });

    expect(received).toBe('new-token');
    expect(setSpy).toHaveBeenCalledWith('new-token');
  });

  it('clears token on logout', () => {
    const clearSpy = vi.spyOn(service, 'clearAccessToken').mockImplementation(() => undefined);

    service.logout().subscribe();

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/api/auth/logout`);
    expect(request.request.method).toBe('POST');
    expect(request.request.withCredentials).toBe(true);
    request.flush(null);

    expect(clearSpy).toHaveBeenCalled();
  });
});
