import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LocationRecord, LocationType } from '../../core/models/location.models';
import { LocationService } from '../../core/services/location.service';

@Component({
  selector: 'app-locations',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './locations.component.html',
  styleUrl: './locations.component.scss'
})
export class LocationsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly locationsApi = inject(LocationService);
  private readonly destroyRef = inject(DestroyRef);

  locations: LocationRecord[] = [];
  editingId: number | null = null;
  submitting = false;
  loading = false;
  message = '';
  errorMessage = '';

  readonly locationTypes: LocationType[] = ['STORE', 'WAREHOUSE'];

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    type: this.fb.control<LocationType>('STORE', Validators.required),
    address: this.fb.control<string | null>(null)
  });

  ngOnInit(): void {
    this.reload();
  }

  startCreate(): void {
    this.editingId = null;
    this.errorMessage = '';
    this.form.reset({ name: '', type: 'STORE', address: null });
  }

  startEdit(location: LocationRecord): void {
    this.editingId = location.id;
    this.message = '';
    this.errorMessage = '';
    this.form.reset({
      name: location.name,
      type: location.type,
      address: location.address
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
      type: value.type as LocationType,
      address: value.address?.trim() || null
    };

    this.submitting = true;
    const request$ = this.editingId === null
      ? this.locationsApi.create(payload)
      : this.locationsApi.update(this.editingId, payload);

    request$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.submitting = false;
        this.message = this.editingId === null ? 'Location created.' : 'Location updated.';
        this.startCreate();
        this.reload();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting = false;
        this.errorMessage = this.readError(error);
      }
    });
  }

  remove(location: LocationRecord): void {
    if (!confirm(`Delete location "${location.name}"?`)) {
      return;
    }
    this.locationsApi.delete(location.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.message = `${location.name} deleted.`;
        if (this.editingId === location.id) {
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
    this.locationsApi.list().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (locations) => {
        this.locations = locations;
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
      return 'Admin or inventory manager role is required to change locations.';
    }
    return 'Request failed. Please try again.';
  }
}
