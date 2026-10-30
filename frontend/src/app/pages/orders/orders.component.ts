import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LocationRecord } from '../../core/models/location.models';
import { ProductRecord } from '../../core/models/product.models';
import { OrderRecord } from '../../core/models/workflow.models';
import { LocationService } from '../../core/services/location.service';
import { ProductService } from '../../core/services/product.service';
import { OrderService } from '../../core/services/workflow.service';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './orders.component.html',
  styleUrl: './orders.component.scss'
})
export class OrdersComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly orderApi = inject(OrderService);
  private readonly locationApi = inject(LocationService);
  private readonly productApi = inject(ProductService);
  private readonly destroyRef = inject(DestroyRef);

  orders: OrderRecord[] = [];
  locations: LocationRecord[] = [];
  products: ProductRecord[] = [];

  selectedOrder: OrderRecord | null = null;
  loading = false;
  submitting = false;
  message = '';
  errorMessage = '';

  readonly form = this.fb.nonNullable.group({
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
      unitPrice: this.fb.control<number | null>(null, [Validators.min(0)])
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
    this.locationApi.list().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (locs) => (this.locations = locs)
    });
    this.productApi.list().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (prods) => (this.products = prods)
    });
  }

  viewDetails(order: OrderRecord): void {
    this.selectedOrder = order;
  }

  closeDetails(): void {
    this.selectedOrder = null;
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
      locationId: val.locationId as number,
      items: val.items.map((i: any) => ({
        productId: i.productId as number,
        quantity: i.quantity as number,
        unitPrice: i.unitPrice ?? null
      }))
    };

    this.submitting = true;
    this.orderApi
      .create(payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (created) => {
          this.submitting = false;
          this.message = `Sales order #${created.id} placed successfully. Total: $${created.totalAmount}`;
          this.form.reset({ locationId: null });
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

  confirmOrder(order: OrderRecord): void {
    if (!confirm(`Fulfill and complete Order #${order.id}? Required stock will be deducted from ${order.locationName}.`)) {
      return;
    }
    this.orderApi
      .confirm(order.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (updated) => {
          this.message = `Order #${updated.id} completed! Inventory stock decreased.`;
          if (this.selectedOrder?.id === updated.id) {
            this.selectedOrder = updated;
          }
          this.reload();
        },
        error: (err: HttpErrorResponse) => {
          this.errorMessage = this.readError(err);
        }
      });
  }

  cancelOrder(order: OrderRecord): void {
    if (!confirm(`Cancel Order #${order.id}?`)) {
      return;
    }
    this.orderApi
      .cancel(order.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (updated) => {
          this.message = `Order #${updated.id} cancelled.`;
          if (this.selectedOrder?.id === updated.id) {
            this.selectedOrder = updated;
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
    this.orderApi
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
