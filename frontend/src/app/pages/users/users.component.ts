import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { USER_ROLES, UserRecord, UserRole } from '../../core/models/user.models';
import { UserService } from '../../core/services/user.service';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './users.component.html',
  styleUrl: './users.component.scss'
})
export class UsersComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly usersApi = inject(UserService);
  private readonly destroyRef = inject(DestroyRef);

  readonly roles = USER_ROLES;
  users: UserRecord[] = [];
  editingId: number | null = null;
  submitting = false;
  loading = false;
  message = '';
  errorMessage = '';

  readonly form = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
    firstName: ['', [Validators.required, Validators.maxLength(100)]],
    lastName: ['', [Validators.required, Validators.maxLength(100)]],
    role: this.fb.nonNullable.control<UserRole>('STAFF', Validators.required),
    active: [true]
  });

  ngOnInit(): void {
    this.reload();
  }

  startCreate(): void {
    this.editingId = null;
    this.errorMessage = '';
    this.form.reset({
      username: '',
      email: '',
      password: '',
      firstName: '',
      lastName: '',
      role: 'STAFF',
      active: true
    });
    this.form.controls.username.enable();
    this.form.controls.password.setValidators([
      Validators.required,
      Validators.minLength(8),
      Validators.maxLength(72)
    ]);
    this.form.controls.password.updateValueAndValidity();
  }

  startEdit(user: UserRecord): void {
    this.editingId = user.id;
    this.message = '';
    this.errorMessage = '';
    this.form.reset({
      username: user.username,
      email: user.email,
      password: '',
      firstName: user.firstName,
      lastName: user.lastName,
      role: this.primaryRole(user),
      active: user.active
    });
    this.form.controls.username.disable();
    this.form.controls.password.clearValidators();
    this.form.controls.password.updateValueAndValidity();
  }

  submit(): void {
    this.errorMessage = '';
    this.message = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    this.submitting = true;
    const request$ = this.editingId === null
      ? this.usersApi.create({
          username: value.username,
          email: value.email,
          password: value.password,
          firstName: value.firstName,
          lastName: value.lastName,
          role: value.role
        })
      : this.usersApi.update(this.editingId, {
          email: value.email,
          firstName: value.firstName,
          lastName: value.lastName,
          role: value.role,
          active: value.active
        });

    request$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.submitting = false;
        this.message = this.editingId === null ? 'User created.' : 'User updated.';
        this.startCreate();
        this.reload();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting = false;
        this.errorMessage = this.readError(error);
      }
    });
  }

  deactivate(user: UserRecord): void {
    if (!confirm(`Deactivate ${user.username}? They will not be able to sign in.`)) {
      return;
    }
    this.usersApi.deactivate(user.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.message = `${user.username} deactivated.`;
        if (this.editingId === user.id) {
          this.startCreate();
        }
        this.reload();
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = this.readError(error);
      }
    });
  }

  primaryRole(user: UserRecord): UserRole {
    const role = user.roles[0];
    return USER_ROLES.includes(role as UserRole) ? (role as UserRole) : 'STAFF';
  }

  private reload(): void {
    this.loading = true;
    this.usersApi.list().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (users) => {
        this.users = users;
        this.loading = false;
      },
      error: (error: HttpErrorResponse) => {
        this.loading = false;
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
    if (error.status === 403) {
      return 'Admin role is required to manage users.';
    }
    return 'Request failed. Please try again.';
  }
}
