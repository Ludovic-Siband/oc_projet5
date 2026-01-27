import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { LoginService } from './login.service';
import { AuthService } from '../../core/auth/auth.service';
import { describe, beforeEach, it, expect, vi } from 'vitest';


describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let component: LoginComponent;
  let loginService: { login: ReturnType<typeof vi.fn> };
  let authService: { setAccessToken: ReturnType<typeof vi.fn> };
  let router: Router;
  let snackBar: { open: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    loginService = { login: vi.fn() };
    authService = { setAccessToken: vi.fn() };
    snackBar = { open: vi.fn() };

    TestBed.overrideComponent(LoginComponent, {
      set: { providers: [{ provide: MatSnackBar, useValue: snackBar }] },
    });

    await TestBed.configureTestingModule({
      imports: [LoginComponent, RouterTestingModule],
      providers: [
        provideNoopAnimations(),
        { provide: LoginService, useValue: loginService },
        { provide: AuthService, useValue: authService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('marks form as touched when invalid', () => {
    component.submit();

    expect(component.form.touched).toBe(true);
    expect(component.loading()).toBe(false);
  });

  it('logs in and navigates on success', () => {
    loginService.login.mockReturnValue(of({ accessToken: 'token', user: { id: 1, email: 'a', username: 'u' } }));
    const navSpy = vi.spyOn(router, 'navigateByUrl');

    component.form.setValue({ identifier: 'user', password: 'pass' });
    component.submit();

    expect(authService.setAccessToken).toHaveBeenCalledWith('token');
    expect(navSpy).toHaveBeenCalledWith('/feed');
    expect(component.loading()).toBe(false);
  });

  it('applies server validation errors to fields', () => {
    loginService.login.mockReturnValue(
      throwError(() => ({ kind: 'validation', message: 'Invalid', fields: { identifier: 'required' } })),
    );

    component.form.setValue({ identifier: 'user', password: 'pass' });
    component.submit();

    expect(component.form.get('identifier')?.hasError('server')).toBe(true);
  });

  it('shows snackbar for unknown errors', () => {
    loginService.login.mockReturnValue(throwError(() => ({ kind: 'unknown', message: 'Boom' })));

    component.form.setValue({ identifier: 'user', password: 'pass' });
    component.submit();

    fixture.detectChanges();
    expect(snackBar.open).toHaveBeenCalledWith('Boom', 'Fermer', { duration: 4000 });
  });
});
