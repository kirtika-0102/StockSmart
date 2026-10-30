import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProductRecord } from '../../core/models/product.models';
import { SupplierProductRecord, SupplierRecord } from '../../core/models/supplier.models';
import { ProductService } from '../../core/services/product.service';
import { SupplierService } from '../../core/services/supplier.service';

@Component({
  selector: 'app-suppliers',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './suppliers.component.html',
  styleUrl: './suppliers.component.scss'
})
export class SuppliersComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly suppliersApi = inject(SupplierService);
  private readonly productsApi = inject(ProductService);
  private readonly destroyRef = inject(DestroyRef);

  suppliers: SupplierRecord[] = [];
  allProducts: ProductRecord[] = [];
  editingId: number | null = null;
  submitting = false;
  loading = false;
  message = '';
  errorMessage = '';

  // Supplier Products linking state
  selectedSupplier: SupplierRecord | null = null;
  supplierProducts: SupplierProductRecord[] = [];
  loadingProducts = false;
  linkingProduct = false;

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    contactName: this.fb.control<string | null>(null),
    email: this.fb.control<string | null>(null, [Validators.email]),
    phone: this.fb.control<string | null>(null),
    address: this.fb.control<string | null>(null)
  });

  readonly linkForm = this.fb.nonNullable.group({
    productId: this.fb.control<number | null>(null, [Validators.required]),
    unitCost: this.fb.control<number | null>(null, [Validators.min(0)])
  });

  ngOnInit(): void {
    this.reload();
    this.productsApi.list().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (products) => {
        this.allProducts = products;
      }
    });
  }

  startCreate(): void {
    this.editingId = null;
    this.errorMessage = '';
    this.form.reset({ name: '', contactName: null, email: null, phone: null, address: null });
  }

  startEdit(supplier: SupplierRecord): void {
    this.editingId = supplier.id;
    this.message = '';
    this.errorMessage = '';
    this.form.reset({
      name: supplier.name,
      contactName: supplier.contactName,
      email: supplier.email,
      phone: supplier.phone,
      address: supplier.address
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
      contactName: value.contactName?.trim() || null,
      email: value.email?.trim() || null,
      phone: value.phone?.trim() || null,
      address: value.address?.trim() || null
    };

    this.submitting = true;
    const request$ = this.editingId === null
      ? this.suppliersApi.create(payload)
      : this.suppliersApi.update(this.editingId, payload);

    request$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.submitting = false;
        this.message = this.editingId === null ? 'Supplier created.' : 'Supplier updated.';
        this.startCreate();
        this.reload();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting = false;
        this.errorMessage = this.readError(error);
      }
    });
  }

  remove(supplier: SupplierRecord): void {
    if (!confirm(`Delete supplier "${supplier.name}"?`)) {
      return;
    }
    this.suppliersApi.delete(supplier.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.message = `${supplier.name} deleted.`;
        if (this.editingId === supplier.id) {
          this.startCreate();
        }
        if (this.selectedSupplier?.id === supplier.id) {
          this.closeProducts();
        }
        this.reload();
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = this.readError(error);
      }
    });
  }

  // --- Supplier Products ---

  selectSupplier(supplier: SupplierRecord): void {
    this.selectedSupplier = supplier;
    this.message = '';
    this.errorMessage = '';
    this.linkForm.reset({ productId: null, unitCost: null });
    this.loadSupplierProducts(supplier.id);
  }

  closeProducts(): void {
    this.selectedSupplier = null;
    this.supplierProducts = [];
  }

  loadSupplierProducts(supplierId: number): void {
    this.loadingProducts = true;
    this.suppliersApi.listProducts(supplierId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (prods) => {
        this.supplierProducts = prods;
        this.loadingProducts = false;
      },
      error: (error: HttpErrorResponse) => {
        this.loadingProducts = false;
        this.errorMessage = this.readError(error);
      }
    });
  }

  linkProduct(): void {
    if (!this.selectedSupplier) return;
    if (this.linkForm.invalid || !this.linkForm.value.productId) {
      this.linkForm.markAllAsTouched();
      return;
    }

    const val = this.linkForm.getRawValue();
    this.linkingProduct = true;
    this.suppliersApi.linkProduct(this.selectedSupplier.id, {
      productId: val.productId as number,
      unitCost: val.unitCost ?? null
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.linkingProduct = false;
        this.message = 'Product linked to supplier successfully.';
        this.linkForm.reset({ productId: null, unitCost: null });
        if (this.selectedSupplier) {
          this.loadSupplierProducts(this.selectedSupplier.id);
        }
      },
      error: (error: HttpErrorResponse) => {
        this.linkingProduct = false;
        this.errorMessage = this.readError(error);
      }
    });
  }

  unlinkProduct(prod: SupplierProductRecord): void {
    if (!this.selectedSupplier) return;
    if (!confirm(`Unlink product "${prod.productName}" from supplier?`)) {
      return;
    }

    this.suppliersApi.unlinkProduct(this.selectedSupplier.id, prod.productId)
      .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: () => {
          this.message = 'Product unlinked.';
          if (this.selectedSupplier) {
            this.loadSupplierProducts(this.selectedSupplier.id);
          }
        },
        error: (error: HttpErrorResponse) => {
          this.errorMessage = this.readError(error);
        }
      });
  }

  availableProducts(): ProductRecord[] {
    const linkedIds = new Set(this.supplierProducts.map((p) => p.productId));
    return this.allProducts.filter((p) => !linkedIds.has(p.id));
  }

  private reload(): void {
    this.loading = true;
    this.suppliersApi.list().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (suppliers) => {
        this.suppliers = suppliers;
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
      return 'Admin or inventory manager role is required to change suppliers.';
    }
    return 'Request failed. Please try again.';
  }
}
