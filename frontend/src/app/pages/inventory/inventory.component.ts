import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  BarcodeLookupResult,
  InventoryRecord,
  InventoryTransactionRecord
} from '../../core/models/inventory.models';
import { LocationRecord } from '../../core/models/location.models';
import { ProductRecord } from '../../core/models/product.models';
import { AuthService } from '../../core/services/auth.service';
import { InventoryService } from '../../core/services/inventory.service';
import { LocationService } from '../../core/services/location.service';
import { ProductService } from '../../core/services/product.service';

type ActionType = 'IN' | 'OUT' | 'ADJUST' | 'REORDER';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule],
  templateUrl: './inventory.component.html',
  styleUrl: './inventory.component.scss'
})
export class InventoryComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly inventoryApi = inject(InventoryService);
  private readonly locationApi = inject(LocationService);
  private readonly productApi = inject(ProductService);
  readonly auth = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  inventoryList: InventoryRecord[] = [];
  locations: LocationRecord[] = [];
  products: ProductRecord[] = [];
  transactions: InventoryTransactionRecord[] = [];

  loading = false;
  submitting = false;
  message = '';
  errorMessage = '';

  // Filter state
  filterLocationId: number | null = null;
  searchTerm = '';

  // Barcode quick search state
  barcodeQuery = '';
  barcodeResult: BarcodeLookupResult | null = null;
  barcodeError = '';

  // Action form state
  actionType: ActionType = 'IN';

  readonly actionForm = this.fb.nonNullable.group({
    locationId: this.fb.control<number | null>(null, [Validators.required]),
    productId: this.fb.control<number | null>(null, [Validators.required]),
    quantity: this.fb.control<number | null>(null, [Validators.required, Validators.min(1)]),
    referenceType: this.fb.control<string | null>(null),
    referenceId: this.fb.control<string | null>(null),
    reason: this.fb.control<string | null>(null)
  });

  readonly reorderForm = this.fb.nonNullable.group({
    locationId: this.fb.control<number | null>(null, [Validators.required]),
    productId: this.fb.control<number | null>(null, [Validators.required]),
    reorderLevel: this.fb.control<number | null>(null, [Validators.required, Validators.min(0)])
  });

  ngOnInit(): void {
    this.reload();
    this.locationApi.list().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (locs) => (this.locations = locs)
    });
    this.productApi.list().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (prods) => (this.products = prods)
    });
    this.loadTransactions();
  }

  setAction(type: ActionType, record?: InventoryRecord): void {
    this.actionType = type;
    this.message = '';
    this.errorMessage = '';

    if (record) {
      if (type === 'REORDER') {
        this.reorderForm.reset({
          locationId: record.locationId,
          productId: record.productId,
          reorderLevel: record.reorderLevel
        });
      } else {
        this.actionForm.reset({
          locationId: record.locationId,
          productId: record.productId,
          quantity: type === 'ADJUST' ? record.quantity : null,
          referenceType: null,
          referenceId: null,
          reason: null
        });
      }
    } else {
      this.actionForm.reset({
        locationId: this.filterLocationId,
        productId: null,
        quantity: null,
        referenceType: null,
        referenceId: null,
        reason: null
      });
    }
  }

  submitAction(): void {
    this.message = '';
    this.errorMessage = '';

    if (this.actionType === 'REORDER') {
      if (this.reorderForm.invalid) {
        this.reorderForm.markAllAsTouched();
        return;
      }
      const val = this.reorderForm.getRawValue();
      this.submitting = true;
      this.inventoryApi
        .updateReorderLevel({
          locationId: val.locationId as number,
          productId: val.productId as number,
          reorderLevel: val.reorderLevel as number
        })
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.submitting = false;
            this.message = 'Reorder level updated successfully.';
            this.reload();
          },
          error: (err: HttpErrorResponse) => {
            this.submitting = false;
            this.errorMessage = this.readError(err);
          }
        });
      return;
    }

    if (this.actionForm.invalid) {
      this.actionForm.markAllAsTouched();
      return;
    }

    const formVal = this.actionForm.getRawValue();
    this.submitting = true;

    if (this.actionType === 'IN') {
      this.inventoryApi
        .stockIn({
          locationId: formVal.locationId as number,
          productId: formVal.productId as number,
          quantity: formVal.quantity as number,
          referenceType: formVal.referenceType?.trim() || null,
          referenceId: formVal.referenceId?.trim() || null,
          reason: formVal.reason?.trim() || null
        })
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.submitting = false;
            this.message = 'Stock received successfully.';
            this.actionForm.reset({ locationId: formVal.locationId, productId: null, quantity: null, referenceType: null, referenceId: null, reason: null });
            this.reload();
            this.loadTransactions();
          },
          error: (err: HttpErrorResponse) => {
            this.submitting = false;
            this.errorMessage = this.readError(err);
          }
        });
    } else if (this.actionType === 'OUT') {
      this.inventoryApi
        .stockOut({
          locationId: formVal.locationId as number,
          productId: formVal.productId as number,
          quantity: formVal.quantity as number,
          referenceType: formVal.referenceType?.trim() || null,
          referenceId: formVal.referenceId?.trim() || null,
          reason: formVal.reason?.trim() || null
        })
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.submitting = false;
            this.message = 'Stock issued successfully.';
            this.actionForm.reset({ locationId: formVal.locationId, productId: null, quantity: null, referenceType: null, referenceId: null, reason: null });
            this.reload();
            this.loadTransactions();
          },
          error: (err: HttpErrorResponse) => {
            this.submitting = false;
            this.errorMessage = this.readError(err);
          }
        });
    } else if (this.actionType === 'ADJUST') {
      this.inventoryApi
        .adjust({
          locationId: formVal.locationId as number,
          productId: formVal.productId as number,
          newQuantity: formVal.quantity as number,
          reason: formVal.reason?.trim() || null
        })
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.submitting = false;
            this.message = 'Stock adjusted successfully.';
            this.actionForm.reset({ locationId: formVal.locationId, productId: null, quantity: null, referenceType: null, referenceId: null, reason: null });
            this.reload();
            this.loadTransactions();
          },
          error: (err: HttpErrorResponse) => {
            this.submitting = false;
            this.errorMessage = this.readError(err);
          }
        });
    }
  }

  searchBarcode(): void {
    this.barcodeError = '';
    this.barcodeResult = null;
    const q = this.barcodeQuery.trim();
    if (!q) return;

    this.inventoryApi
      .lookupBarcode(q)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (result) => {
          this.barcodeResult = result;
        },
        error: (err: HttpErrorResponse) => {
          this.barcodeError = this.readError(err);
        }
      });
  }

  clearBarcode(): void {
    this.barcodeQuery = '';
    this.barcodeResult = null;
    this.barcodeError = '';
  }

  filteredInventory(): InventoryRecord[] {
    return this.inventoryList.filter((item) => {
      const matchLoc = this.filterLocationId === null || item.locationId === this.filterLocationId;
      const term = this.searchTerm.trim().toLowerCase();
      const matchSearch =
        !term ||
        item.productName.toLowerCase().includes(term) ||
        item.productSku.toLowerCase().includes(term) ||
        (item.barcode && item.barcode.toLowerCase().includes(term));
      return matchLoc && matchSearch;
    });
  }

  private reload(): void {
    this.loading = true;
    this.inventoryApi
      .listAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (list) => {
          this.inventoryList = list;
          this.loading = false;
        },
        error: (err: HttpErrorResponse) => {
          this.loading = false;
          this.errorMessage = this.readError(err);
        }
      });
  }

  private loadTransactions(): void {
    this.inventoryApi
      .listTransactions()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (txs) => (this.transactions = txs.slice(0, 20))
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
      return 'You do not have permission to perform this action.';
    }
    return 'Operation failed. Please check inputs and try again.';
  }
}
