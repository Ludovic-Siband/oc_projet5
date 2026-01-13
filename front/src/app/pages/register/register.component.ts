import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Router, RouterLink } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';

type ApiError = { message?: string; fields?: Record<string, string> };

const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z\d]).+$/;

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly apiError = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.pattern(PASSWORD_PATTERN)]],
  });

  submit(): void {
    if (this.loading()) {
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.apiError.set(null);
    this.loading.set(true);

    this.http
      .post('/api/auth/register', this.form.getRawValue())
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => this.router.navigateByUrl('/login'),
        error: (error: HttpErrorResponse) => this.handleError(error),
      });
  }

  private handleError(error: HttpErrorResponse): void {
    const apiError = (error.error || {}) as ApiError;

    if (error.status === 400 && apiError.fields) {
      Object.entries(apiError.fields).forEach(([field, message]) => {
        const control = this.form.get(field);
        if (control) {
          control.setErrors({ server: message });
          control.markAsTouched();
        }
      });
      return;
    }

    this.apiError.set(apiError.message ?? "Une erreur est survenue. Veuillez réessayer.");
  }
}
