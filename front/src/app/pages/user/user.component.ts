import { ChangeDetectionStrategy, Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { UserService } from './user.service';
import { UserProfile, UserSubscription, UserUpdateError } from './user.model';

const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z\d]).+$/;

@Component({
  selector: 'app-user',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSnackBarModule,
  ],
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);

  readonly loadingProfile = signal(false);
  readonly saving = signal(false);
  readonly subscriptions = signal<UserSubscription[]>([]);

  readonly form = this.fb.nonNullable.group({
    username: ['', [Validators.minLength(3), Validators.maxLength(50)]],
    email: ['', [Validators.email]],
    password: ['', [Validators.minLength(8), Validators.pattern(PASSWORD_PATTERN)]],
  });

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.loadingProfile.set(true);
    this.userService
      .getProfile()
      .pipe(
        finalize(() => this.loadingProfile.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (profile) => this.applyProfile(profile),
        error: (error) => this.handleError(error),
      });
  }

  save(): void {
    if (this.saving()) {
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const payload = this.buildPayload();
    if (!payload) {
      this.snackBar.open('Veuillez renseigner au moins un champ.', 'Fermer', { duration: 4000 });
      return;
    }

    this.saving.set(true);
    this.userService
      .updateProfile(payload)
      .pipe(
        finalize(() => this.saving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (response) => {
          this.form.patchValue({ username: response.username, email: response.email, password: '' });
          this.form.markAsPristine();
          this.snackBar.open('Modification enregistrée', 'Fermer', { duration: 3000 });
        },
        error: (error: UserUpdateError) => this.handleUpdateError(error),
      });
  }

  unsubscribe(subjectId: number): void {
    this.userService
      .unsubscribe(subjectId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.subscriptions.update((items) => items.filter((item) => item.subjectId !== subjectId));
        },
        error: (error) => this.handleError(error),
      });
  }

  private applyProfile(profile: UserProfile): void {
    this.form.patchValue({ username: profile.username, email: profile.email, password: '' });
    this.subscriptions.set(profile.subscriptions);
  }

  private buildPayload(): { username?: string; email?: string; password?: string } | null {
    const { username, email, password } = this.form.getRawValue();
    const payload: { username?: string; email?: string; password?: string } = {};

    if (username && username.trim().length > 0) {
      payload.username = username.trim();
    }
    if (email && email.trim().length > 0) {
      payload.email = email.trim();
    }
    if (password && password.trim().length > 0) {
      payload.password = password;
    }

    return Object.keys(payload).length ? payload : null;
  }

  private handleUpdateError(error: UserUpdateError): void {
    if (error.kind === 'validation' && error.fields) {
      Object.entries(error.fields).forEach(([field, message]) => {
        const control = this.form.get(field);
        if (control) {
          control.setErrors({ server: message });
          control.markAsTouched();
        }
      });
      return;
    }

    if (error.message) {
      this.snackBar.open(error.message, 'Fermer', { duration: 4000 });
    }
  }

  private handleError(error: { error?: { message?: string } }): void {
    const message = error?.error?.message ?? 'Une erreur est survenue';
    this.snackBar.open(message, 'Fermer', { duration: 4000 });
  }
}
