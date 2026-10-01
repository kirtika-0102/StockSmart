import { Component, OnInit, inject } from '@angular/core';
import { HealthService } from '../../core/services/health.service';

@Component({
  selector: 'app-home',
  standalone: true,
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {
  private readonly healthService = inject(HealthService);

  backendStatus = 'Checking backend…';

  ngOnInit(): void {
    this.healthService.getHealth().subscribe({
      next: (health) => {
        this.backendStatus = `${health.application} is ${health.status}`;
      },
      error: () => {
        this.backendStatus = 'Backend is not reachable yet. Start Spring Boot on port 8080.';
      }
    });
  }
}
