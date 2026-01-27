import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthService } from './auth.service';
import { describe, beforeEach, it, expect, vi } from 'vitest';


describe('authGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: { getAccessToken: vi.fn() } },
        { provide: Router, useValue: { parseUrl: vi.fn() } },
      ],
    });
  });

  it('allows access when token exists', () => {
    const authService = TestBed.inject(AuthService);
    vi.mocked(authService.getAccessToken).mockReturnValue('token');

    const result = TestBed.runInInjectionContext(() => authGuard({} as any, []));

    expect(result).toBe(true);
  });

  it('redirects to login when token is missing', () => {
    const authService = TestBed.inject(AuthService);
    const router = TestBed.inject(Router);

    vi.mocked(authService.getAccessToken).mockReturnValue(null);
    vi.mocked(router.parseUrl).mockReturnValue('login-tree' as any);

    const result = TestBed.runInInjectionContext(() => authGuard({} as any, []));

    expect(router.parseUrl).toHaveBeenCalledWith('/login');
    expect(result).toBe('login-tree');
  });
});
