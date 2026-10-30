import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  submitting = false;
  errorMessage = '';

  readonly form = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  submit(): void {
    this.errorMessage = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.auth.login(this.form.getRawValue()).subscribe({
        next: () => {
          this.submitting = false;
          void this.router.navigateByUrl('/');
        },
        error: (error: HttpErrorResponse) => {
          this.submitting = false;
          this.errorMessage = this.readError(error);
        }
      });
  }

  private readError(error: HttpErrorResponse): string {
    if (error.status === 0) {
      return 'Cannot reach the backend. Start Spring Boot on port 8080.';
    }
    if (typeof error.error === 'object' && error.error?.message) {
      return error.error.message;
    }
    if (error.status === 401) {
      return 'Invalid credentials';
    }
    return 'Login failed. Please try again.';
  }
}
