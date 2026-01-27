import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { RegisterComponent } from './register.component';
import { RegisterService } from './register.service';
import { describe, beforeEach, it, expect, vi } from 'vitest';


describe('RegisterComponent', () => {
  let fixture: ComponentFixture<RegisterComponent>;
  let component: RegisterComponent;
  let registerService: { register: ReturnType<typeof vi.fn> };
  let router: Router;
  let snackBar: { open: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    registerService = { register: vi.fn() };
    snackBar = { open: vi.fn() };

    TestBed.overrideComponent(RegisterComponent, {
      set: { providers: [{ provide: MatSnackBar, useValue: snackBar }] },
    });

    await TestBed.configureTestingModule({
      imports: [RegisterComponent, RouterTestingModule],
      providers: [
        provideNoopAnimations(),
        { provide: RegisterService, useValue: registerService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('marks form as touched when invalid', () => {
    component.submit();

    expect(component.form.touched).toBe(true);
    expect(component.loading()).toBe(false);
  });

  it('navigates to login on success', () => {
    registerService.register.mockReturnValue(of({ id: 1, email: 'a', username: 'u' }));
    const navSpy = vi.spyOn(router, 'navigateByUrl');

    component.form.setValue({ username: 'user', email: 'user@test.com', password: 'Aa1!aaaa' });
    component.submit();

    expect(navSpy).toHaveBeenCalledWith('/login');
    expect(component.loading()).toBe(false);
  });

  it('applies server validation errors to fields', () => {
    registerService.register.mockReturnValue(
      throwError(() => ({ kind: 'validation', message: 'Invalid', fields: { email: 'invalid' } })),
    );

    component.form.setValue({ username: 'user', email: 'user@test.com', password: 'Aa1!aaaa' });
    component.submit();

    expect(component.form.get('email')?.hasError('server')).toBe(true);
  });

  it('shows snackbar for unknown errors', () => {
    registerService.register.mockReturnValue(throwError(() => ({ kind: 'unknown', message: 'Boom' })));

    component.form.setValue({ username: 'user', email: 'user@test.com', password: 'Aa1!aaaa' });
    component.submit();

    fixture.detectChanges();
    expect(snackBar.open).toHaveBeenCalledWith('Boom', 'Fermer', { duration: 4000 });
  });
});
