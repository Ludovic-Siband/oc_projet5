import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { UserComponent } from './user.component';
import { UserService } from './user.service';
import { describe, beforeEach, it, expect, vi } from 'vitest';


describe('UserComponent', () => {
  let fixture: ComponentFixture<UserComponent>;
  let component: UserComponent;
  let userService: {
    getProfile: ReturnType<typeof vi.fn>;
    updateProfile: ReturnType<typeof vi.fn>;
    unsubscribe: ReturnType<typeof vi.fn>;
  };
  let snackBar: { open: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    userService = {
      getProfile: vi.fn(),
      updateProfile: vi.fn(),
      unsubscribe: vi.fn(),
    };
    snackBar = { open: vi.fn() };

    TestBed.overrideComponent(UserComponent, {
      set: { providers: [{ provide: MatSnackBar, useValue: snackBar }] },
    });

    await TestBed.configureTestingModule({
      imports: [UserComponent],
      providers: [
        provideNoopAnimations(),
        { provide: UserService, useValue: userService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UserComponent);
    component = fixture.componentInstance;
  });

  it('loads profile on init', () => {
    userService.getProfile.mockReturnValue(
      of({
        id: 1,
        email: 'a@b.c',
        username: 'user',
        subscriptions: [{ subjectId: 1, name: 'A', description: 'd' }],
      }),
    );

    fixture.detectChanges();

    expect(component.form.value.email).toBe('a@b.c');
    expect(component.subscriptions().length).toBe(1);
  });

  it('shows snackbar when no fields are provided', () => {
    userService.getProfile.mockReturnValue(of({ id: 1, email: '', username: '', subscriptions: [] }));

    fixture.detectChanges();

    component.save();

    fixture.detectChanges();
    expect(snackBar.open).toHaveBeenCalledWith('Veuillez renseigner au moins un champ.', 'Fermer', { duration: 4000 });
  });

  it('updates profile and shows success message', () => {
    userService.getProfile.mockReturnValue(of({ id: 1, email: 'a@b.c', username: 'user', subscriptions: [] }));
    userService.updateProfile.mockReturnValue(of({ id: 1, email: 'b@c.d', username: 'user2' }));

    fixture.detectChanges();

    component.form.patchValue({ email: 'b@c.d' });
    component.save();

    expect(userService.updateProfile).toHaveBeenCalled();
    fixture.detectChanges();
    expect(snackBar.open).toHaveBeenCalledWith('Modification enregistrée', 'Fermer', { duration: 3000 });
  });

  it('applies server validation errors', () => {
    userService.getProfile.mockReturnValue(of({ id: 1, email: 'a@b.c', username: 'user', subscriptions: [] }));
    userService.updateProfile.mockReturnValue(
      throwError(() => ({ kind: 'validation', message: 'Invalid', fields: { email: 'invalid' } })),
    );

    fixture.detectChanges();

    component.form.patchValue({ email: 'user@test.com' });
    component.save();

    fixture.detectChanges();
    expect(component.form.get('email')?.hasError('server')).toBe(true);
  });

  it('removes subscription on unsubscribe', () => {
    userService.getProfile.mockReturnValue(
      of({
        id: 1,
        email: 'a@b.c',
        username: 'user',
        subscriptions: [{ subjectId: 1, name: 'A', description: 'd' }],
      }),
    );
    userService.unsubscribe.mockReturnValue(of(undefined));

    fixture.detectChanges();

    component.unsubscribe(1);

    expect(component.subscriptions().length).toBe(0);
  });
});
