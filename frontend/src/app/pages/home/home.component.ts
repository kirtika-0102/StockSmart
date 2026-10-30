import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { DashboardKpiRecord } from '../../core/models/dashboard.models';
import { DashboardService } from '../../core/services/dashboard.service';
import { HealthService } from '../../core/services/health.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {
  private readonly healthService = inject(HealthService);
  private readonly dashboardService = inject(DashboardService);
  private readonly destroyRef = inject(DestroyRef);

  backendStatus = 'Checking backend…';
  kpis: DashboardKpiRecord | null = null;
  loading = true;

  ngOnInit(): void {
    this.healthService.getHealth().subscribe({
      next: (health) => {
        this.backendStatus = `${health.application} is ${health.status}`;
      },
      error: () => {
        this.backendStatus = 'Backend is not reachable yet. Start Spring Boot on port 8080.';
      }
    });

    this.dashboardService
      .getSummary()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (summary) => {
          this.kpis = summary;
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
  }
}
