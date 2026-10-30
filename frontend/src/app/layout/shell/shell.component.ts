import { AsyncPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, AsyncPipe],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss'
})
export class ShellComponent {
  private readonly auth = inject(AuthService);

  readonly currentUser$ = this.auth.currentUser$;
  readonly navItems = [
    { path: '/', label: 'Dashboard', roles: [] as string[] },
    { path: '/inventory', label: 'Inventory', roles: ['ADMIN', 'INVENTORY_MANAGER', 'STAFF'] },
    { path: '/alerts', label: 'Alerts', roles: ['ADMIN', 'INVENTORY_MANAGER', 'STAFF'] },
    { path: '/reports', label: 'Reports', roles: ['ADMIN', 'INVENTORY_MANAGER', 'STAFF'] },
    { path: '/orders', label: 'Orders', roles: ['ADMIN', 'INVENTORY_MANAGER', 'STAFF'] },
    { path: '/purchase-orders', label: 'Purchase Orders', roles: ['ADMIN', 'INVENTORY_MANAGER'] },
    { path: '/transfers', label: 'Transfers', roles: ['ADMIN', 'INVENTORY_MANAGER'] },
    { path: '/categories', label: 'Categories', roles: ['ADMIN', 'INVENTORY_MANAGER'] },
    { path: '/products', label: 'Products', roles: ['ADMIN', 'INVENTORY_MANAGER'] },
    { path: '/locations', label: 'Locations', roles: ['ADMIN', 'INVENTORY_MANAGER'] },
    { path: '/suppliers', label: 'Suppliers', roles: ['ADMIN', 'INVENTORY_MANAGER'] },
    { path: '/users', label: 'Users', roles: ['ADMIN'] }
  ];

  canSee(roles: string[]): boolean {
    return roles.length === 0 || roles.some((role) => this.auth.hasRole(role));
  }

  logout(): void {
    this.auth.logout();
  }
}
