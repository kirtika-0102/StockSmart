import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LocationRecord } from '../../core/models/location.models';
import { ProductRecord } from '../../core/models/product.models';
import { SupplierRecord } from '../../core/models/supplier.models';
import { PurchaseOrderRecord } from '../../core/models/workflow.models';
import { LocationService } from '../../core/services/location.service';
import { ProductService } from '../../core/services/product.service';
import { SupplierService } from '../../core/services/supplier.service';
import { PurchaseOrderService } from '../../core/services/workflow.service';

@Component({
  selector: 'app-purchase-orders',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './purchase-orders.component.html',
  styleUrl: './purchase-orders.component.scss'
})
export class PurchaseOrdersComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly poApi = inject(PurchaseOrderService);
  private readonly supplierApi = inject(SupplierService);
  private readonly locationApi = inject(LocationService);
  private readonly productApi = inject(ProductService);
  private readonly destroyRef = inject(DestroyRef);

  orders: PurchaseOrderRecord[] = [];
  suppliers: SupplierRecord[] = [];
  locations: LocationRecord[] = [];
  products: ProductRecord[] = [];

  selectedPo: PurchaseOrderRecord | null = null;
  loading = false;
  submitting = false;
  message = '';
  errorMessage = '';

  readonly form = this.fb.nonNullable.group({
    supplierId: this.fb.control<number | null>(null, [Validators.required]),
    locationId: this.fb.control<number | null>(null, [Validators.required]),
    items: this.fb.array([this.createItemFormGroup()])
  });

  get items(): FormArray {
    return this.form.get('items') as FormArray;
  }

  createItemFormGroup() {
    return this.fb.nonNullable.group({
      productId: this.fb.control<number | null>(null, [Validators.required]),
      quantity: this.fb.control<number | null>(1, [Validators.required, Validators.min(1)]),
      unitCost: this.fb.control<number | null>(null, [Validators.min(0)])
    });
  }

  addItem(): void {
    this.items.push(this.createItemFormGroup());
  }

  removeItem(index: number): void {
    if (this.items.length > 1) {
      this.items.removeAt(index);
    }
  }

  ngOnInit(): void {
    this.reload();
    this.supplierApi.list().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (sups) => (this.suppliers = sups)
    });
    this.locationApi.list().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (locs) => (this.locations = locs)
    });
    this.productApi.list().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (prods) => (this.products = prods)
    });
  }

  viewDetails(po: PurchaseOrderRecord): void {
    this.selectedPo = po;
  }

  closeDetails(): void {
    this.selectedPo = null;
  }

  submit(): void {
    this.message = '';
    this.errorMessage = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const val = this.form.getRawValue();
    const payload = {
      supplierId: val.supplierId as number,
      locationId: val.locationId as number,
      items: val.items.map((i: any) => ({
        productId: i.productId as number,
        quantity: i.quantity as number,
        unitCost: i.unitCost ?? null
      }))
    };

    this.submitting = true;
    this.poApi
      .create(payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (created) => {
          this.submitting = false;
          this.message = `Purchase order #${created.id} created successfully.`;
          this.form.reset({ supplierId: null, locationId: null });
          this.items.clear();
          this.items.push(this.createItemFormGroup());
          this.reload();
        },
        error: (err: HttpErrorResponse) => {
          this.submitting = false;
          this.errorMessage = this.readError(err);
        }
      });
  }

  receiveOrder(po: PurchaseOrderRecord): void {
    if (!confirm(`Confirm receipt of PO #${po.id}? Stock will be added to ${po.locationName}.`)) {
      return;
    }
    this.poApi
      .receive(po.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (updated) => {
          this.message = `PO #${updated.id} received! Inventory quantities updated.`;
          if (this.selectedPo?.id === updated.id) {
            this.selectedPo = updated;
          }
          this.reload();
        },
        error: (err: HttpErrorResponse) => {
          this.errorMessage = this.readError(err);
        }
      });
  }

  cancelOrder(po: PurchaseOrderRecord): void {
    if (!confirm(`Cancel PO #${po.id}?`)) {
      return;
    }
    this.poApi
      .cancel(po.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (updated) => {
          this.message = `PO #${updated.id} cancelled.`;
          if (this.selectedPo?.id === updated.id) {
            this.selectedPo = updated;
          }
          this.reload();
        },
        error: (err: HttpErrorResponse) => {
          this.errorMessage = this.readError(err);
        }
      });
  }

  private reload(): void {
    this.loading = true;
    this.poApi
      .listAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (list) => {
          this.orders = list;
          this.loading = false;
        },
        error: (err: HttpErrorResponse) => {
          this.loading = false;
          this.errorMessage = this.readError(err);
        }
      });
  }

  private readError(error: HttpErrorResponse): string {
    if (error.status === 0) return 'Cannot reach the backend. Start Spring Boot on port 8080.';
    if (typeof error.error === 'object' && error.error?.message) return error.error.message;
    return 'Operation failed. Please try again.';
  }
}
