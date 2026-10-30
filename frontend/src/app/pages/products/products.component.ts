import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CategoryRecord } from '../../core/models/category.models';
import { ProductRecord } from '../../core/models/product.models';
import { CategoryService } from '../../core/services/category.service';
import { ProductService } from '../../core/services/product.service';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './products.component.html',
  styleUrl: './products.component.scss'
})
export class ProductsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly productsApi = inject(ProductService);
  private readonly categoriesApi = inject(CategoryService);
  private readonly destroyRef = inject(DestroyRef);

  products: ProductRecord[] = [];
  categories: CategoryRecord[] = [];
  editingId: number | null = null;
  submitting = false;
  loading = false;
  message = '';
  errorMessage = '';

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(200)]],
    sku: ['', [Validators.required, Validators.maxLength(100)]],
    barcode: this.fb.control<string | null>(null),
    description: this.fb.control<string | null>(null),
    unitPrice: this.fb.control<number | null>(null, [Validators.min(0)]),
    categoryId: this.fb.control<number | null>(null)
  });

  ngOnInit(): void {
    this.reload();
    this.categoriesApi.list().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (cats) => { this.categories = cats; }
    });
  }

  startCreate(): void {
    this.editingId = null;
    this.errorMessage = '';
    this.form.reset({ name: '', sku: '', barcode: null, description: null, unitPrice: null, categoryId: null });
  }

  startEdit(product: ProductRecord): void {
    this.editingId = product.id;
    this.message = '';
    this.errorMessage = '';
    this.form.reset({
      name: product.name,
      sku: product.sku,
      barcode: product.barcode,
      description: product.description,
      unitPrice: product.unitPrice,
      categoryId: product.categoryId
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
      sku: value.sku.trim(),
      barcode: value.barcode?.trim() || null,
      description: value.description?.trim() || null,
      unitPrice: value.unitPrice,
      categoryId: value.categoryId
    };

    this.submitting = true;
    const request$ = this.editingId === null
      ? this.productsApi.create(payload)
      : this.productsApi.update(this.editingId, payload);

    request$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.submitting = false;
        this.message = this.editingId === null ? 'Product created.' : 'Product updated.';
        this.startCreate();
        this.reload();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting = false;
        this.errorMessage = this.readError(error);
      }
    });
  }

  remove(product: ProductRecord): void {
    if (!confirm(`Delete product "${product.name}"?`)) {
      return;
    }
    this.productsApi.delete(product.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.message = `${product.name} deleted.`;
        if (this.editingId === product.id) {
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
    this.productsApi.list().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (products) => {
        this.products = products;
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
      return 'Admin or inventory manager role is required to change products.';
    }
    return 'Request failed. Please try again.';
  }
}
