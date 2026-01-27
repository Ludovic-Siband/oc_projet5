import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { AppHeaderComponent } from './app-header.component';
import { AuthService } from '../../core/auth/auth.service';
import { describe, beforeEach, it, expect, vi } from 'vitest';


describe('AppHeaderComponent', () => {
  let fixture: ComponentFixture<AppHeaderComponent>;
  let component: AppHeaderComponent;
  let authService: { logout: ReturnType<typeof vi.fn> };
  let router: Router;

  beforeEach(async () => {
    authService = { logout: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [AppHeaderComponent, RouterTestingModule],
      providers: [{ provide: AuthService, useValue: authService }],
    }).compileComponents();

    fixture = TestBed.createComponent(AppHeaderComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
  });

  it('toggles menu state', () => {
    expect(component.isMenuOpen()).toBe(false);

    component.openMenu();
    expect(component.isMenuOpen()).toBe(true);

    component.closeMenu();
    expect(component.isMenuOpen()).toBe(false);

    component.toggleMenu();
    expect(component.isMenuOpen()).toBe(true);
  });

  it('navigates home on logout success', () => {
    authService.logout.mockReturnValue(of(undefined));
    const navSpy = vi.spyOn(router, 'navigateByUrl');

    component.logout();

    expect(navSpy).toHaveBeenCalledWith('/');
  });

  it('navigates home on logout error', () => {
    authService.logout.mockReturnValue(throwError(() => new Error('fail')));
    const navSpy = vi.spyOn(router, 'navigateByUrl');

    component.logout();

    expect(navSpy).toHaveBeenCalledWith('/');
  });
});
