import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LocationRecord } from '../../core/models/location.models';
import { ProductRecord } from '../../core/models/product.models';
import { StockTransferRecord } from '../../core/models/workflow.models';
import { LocationService } from '../../core/services/location.service';
import { ProductService } from '../../core/services/product.service';
import { StockTransferService } from '../../core/services/workflow.service';

@Component({
  selector: 'app-stock-transfers',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './stock-transfers.component.html',
  styleUrl: './stock-transfers.component.scss'
})
export class StockTransfersComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly transferApi = inject(StockTransferService);
  private readonly locationApi = inject(LocationService);
  private readonly productApi = inject(ProductService);
  private readonly destroyRef = inject(DestroyRef);

  transfers: StockTransferRecord[] = [];
  locations: LocationRecord[] = [];
  products: ProductRecord[] = [];

  selectedTransfer: StockTransferRecord | null = null;
  loading = false;
  submitting = false;
  message = '';
  errorMessage = '';

  readonly form = this.fb.nonNullable.group({
    sourceLocationId: this.fb.control<number | null>(null, [Validators.required]),
    destLocationId: this.fb.control<number | null>(null, [Validators.required]),
    items: this.fb.array([this.createItemFormGroup()])
  });

  get items(): FormArray {
    return this.form.get('items') as FormArray;
  }

  createItemFormGroup() {
    return this.fb.nonNullable.group({
      productId: this.fb.control<number | null>(null, [Validators.required]),
      quantity: this.fb.control<number | null>(1, [Validators.required, Validators.min(1)])
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

  viewDetails(transfer: StockTransferRecord): void {
    this.selectedTransfer = transfer;
  }

  closeDetails(): void {
    this.selectedTransfer = null;
  }

  submit(): void {
    this.message = '';
    this.errorMessage = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const val = this.form.getRawValue();
    if (val.sourceLocationId === val.destLocationId) {
      this.errorMessage = 'Source and destination locations cannot be the same.';
      return;
    }

    const payload = {
      sourceLocationId: val.sourceLocationId as number,
      destLocationId: val.destLocationId as number,
      items: val.items.map((i: any) => ({
        productId: i.productId as number,
        quantity: i.quantity as number
      }))
    };

    this.submitting = true;
    this.transferApi
      .create(payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (created) => {
          this.submitting = false;
          this.message = `Stock transfer #${created.id} initiated.`;
          this.form.reset({ sourceLocationId: null, destLocationId: null });
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

  completeTransfer(transfer: StockTransferRecord): void {
    if (!confirm(`Complete Transfer #${transfer.id}? Stock will move from ${transfer.sourceLocationName} to ${transfer.destLocationName}.`)) {
      return;
    }
    this.transferApi
      .complete(transfer.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (updated) => {
          this.message = `Transfer #${updated.id} completed! Quantities updated at both locations.`;
          if (this.selectedTransfer?.id === updated.id) {
            this.selectedTransfer = updated;
          }
          this.reload();
        },
        error: (err: HttpErrorResponse) => {
          this.errorMessage = this.readError(err);
        }
      });
  }

  cancelTransfer(transfer: StockTransferRecord): void {
    if (!confirm(`Cancel Transfer #${transfer.id}?`)) {
      return;
    }
    this.transferApi
      .cancel(transfer.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (updated) => {
          this.message = `Transfer #${updated.id} cancelled.`;
          if (this.selectedTransfer?.id === updated.id) {
            this.selectedTransfer = updated;
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
    this.transferApi
      .listAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (list) => {
          this.transfers = list;
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
