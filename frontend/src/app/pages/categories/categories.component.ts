import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CategoryRecord } from '../../core/models/category.models';
import { CategoryService } from '../../core/services/category.service';

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './categories.component.html',
  styleUrl: './categories.component.scss'
})
export class CategoriesComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly categoriesApi = inject(CategoryService);
  private readonly destroyRef = inject(DestroyRef);

  categories: CategoryRecord[] = [];
  editingId: number | null = null;
  submitting = false;
  loading = false;
  message = '';
  errorMessage = '';

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    parentId: this.fb.control<number | null>(null)
  });

  ngOnInit(): void {
    this.reload();
  }

  parentChoices(): CategoryRecord[] {
    return this.categories.filter((category) => category.id !== this.editingId);
  }

  startCreate(): void {
    this.editingId = null;
    this.errorMessage = '';
    this.form.reset({ name: '', parentId: null });
  }

  startEdit(category: CategoryRecord): void {
    this.editingId = category.id;
    this.message = '';
    this.errorMessage = '';
    this.form.reset({
      name: category.name,
      parentId: category.parentId
    });
  }

  submit(): void {
    this.errorMessage = '';
    this.message = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const payload = {
      name: value.name.trim(),
      parentId: value.parentId
    };
    this.submitting = true;
    const request$ = this.editingId === null
      ? this.categoriesApi.create(payload)
      : this.categoriesApi.update(this.editingId, payload);

    request$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.submitting = false;
        this.message = this.editingId === null ? 'Category created.' : 'Category updated.';
        this.startCreate();
        this.reload();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting = false;
        this.errorMessage = this.readError(error);
      }
    });
  }

  remove(category: CategoryRecord): void {
    if (!confirm(`Delete category "${category.name}"?`)) {
      return;
    }
    this.categoriesApi.delete(category.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.message = `${category.name} deleted.`;
        if (this.editingId === category.id) {
          this.startCreate();
        }
        this.reload();
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = this.readError(error);
      }
    });
  }

  private reload(): void {
    this.loading = true;
    this.categoriesApi.list().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (categories) => {
        this.categories = categories;
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
      return 'Admin or inventory manager role is required to change categories.';
    }
    return 'Request failed. Please try again.';
  }
}
