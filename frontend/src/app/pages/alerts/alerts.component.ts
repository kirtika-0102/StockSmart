import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { AlertRecord, AlertStatus } from '../../core/models/alert.models';
import { AlertService } from '../../core/services/alert.service';

@Component({
  selector: 'app-alerts',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './alerts.component.html',
  styleUrl: './alerts.component.scss'
})
export class AlertsComponent implements OnInit {
  private readonly alertApi = inject(AlertService);
  private readonly destroyRef = inject(DestroyRef);

  alerts: AlertRecord[] = [];
  statusFilter: AlertStatus | '' = 'ACTIVE';
  loading = false;
  scanning = false;
  message = '';
  errorMessage = '';

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading = true;
    const filter = this.statusFilter === '' ? undefined : this.statusFilter;
    this.alertApi
      .list(filter)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (list) => {
          this.alerts = list;
          this.loading = false;
        },
        error: (err: HttpErrorResponse) => {
          this.loading = false;
          this.errorMessage = this.readError(err);
        }
      });
  }

  scanAlerts(): void {
    this.scanning = true;
    this.message = '';
    this.errorMessage = '';
    this.alertApi
      .checkAlerts()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (active) => {
          this.scanning = false;
          this.message = `Scan complete. Found ${active.length} active alert(s).`;
          this.reload();
        },
        error: (err: HttpErrorResponse) => {
          this.scanning = false;
          this.errorMessage = this.readError(err);
        }
      });
  }

  acknowledge(id: number): void {
    this.alertApi
      .acknowledge(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.message = 'Alert acknowledged.';
          this.reload();
        },
        error: (err: HttpErrorResponse) => {
          this.errorMessage = this.readError(err);
        }
      });
  }

  resolve(id: number): void {
    this.alertApi
      .resolve(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.message = 'Alert marked as resolved.';
          this.reload();
        },
        error: (err: HttpErrorResponse) => {
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
