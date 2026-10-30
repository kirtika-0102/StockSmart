import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { InventoryValuationItem } from '../../core/models/report.models';
import { ReportService } from '../../core/services/report.service';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.scss'
})
export class ReportsComponent implements OnInit {
  private readonly reportApi = inject(ReportService);
  private readonly destroyRef = inject(DestroyRef);

  items: InventoryValuationItem[] = [];
  searchTerm = '';
  locationFilter = '';
  categoryFilter = '';
  loading = false;
  errorMessage = '';

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading = true;
    this.reportApi
      .getValuationReport()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.items = data;
          this.loading = false;
        },
        error: (err: HttpErrorResponse) => {
          this.loading = false;
          this.errorMessage = this.readError(err);
        }
      });
  }

  filteredItems(): InventoryValuationItem[] {
    const term = this.searchTerm.trim().toLowerCase();
    return this.items.filter((item) => {
      const matchSearch =
        !term ||
        item.productName.toLowerCase().includes(term) ||
        item.productSku.toLowerCase().includes(term);
      const matchLoc = !this.locationFilter || item.locationName === this.locationFilter;
      const matchCat = !this.categoryFilter || item.categoryName === this.categoryFilter;
      return matchSearch && matchLoc && matchCat;
    });
  }

  uniqueLocations(): string[] {
    return Array.from(new Set(this.items.map((i) => i.locationName))).sort();
  }

  uniqueCategories(): string[] {
    return Array.from(new Set(this.items.map((i) => i.categoryName))).sort();
  }

  totalQuantity(): number {
    return this.filteredItems().reduce((sum, item) => sum + item.quantity, 0);
  }

  totalValuation(): number {
    return this.filteredItems().reduce((sum, item) => sum + (item.totalValue || 0), 0);
  }

  private readError(error: HttpErrorResponse): string {
    if (error.status === 0) return 'Cannot reach the backend. Start Spring Boot on port 8080.';
    if (typeof error.error === 'object' && error.error?.message) return error.error.message;
    return 'Failed to load report.';
  }
}
