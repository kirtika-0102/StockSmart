import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { roleGuard } from './core/guards/role.guard';
import { ShellComponent } from './layout/shell/shell.component';
import { AlertsComponent } from './pages/alerts/alerts.component';
import { CategoriesComponent } from './pages/categories/categories.component';
import { HomeComponent } from './pages/home/home.component';
import { InventoryComponent } from './pages/inventory/inventory.component';
import { LocationsComponent } from './pages/locations/locations.component';
import { LoginComponent } from './pages/login/login.component';
import { OrdersComponent } from './pages/orders/orders.component';
import { ProductsComponent } from './pages/products/products.component';
import { PurchaseOrdersComponent } from './pages/purchase-orders/purchase-orders.component';
import { ReportsComponent } from './pages/reports/reports.component';
import { StockTransfersComponent } from './pages/stock-transfers/stock-transfers.component';
import { SuppliersComponent } from './pages/suppliers/suppliers.component';
import { UsersComponent } from './pages/users/users.component';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    canActivate: [guestGuard]
  },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', component: HomeComponent },
      {
        path: 'inventory',
        component: InventoryComponent,
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'INVENTORY_MANAGER', 'STAFF'] }
      },
      {
        path: 'alerts',
        component: AlertsComponent,
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'INVENTORY_MANAGER', 'STAFF'] }
      },
      {
        path: 'reports',
        component: ReportsComponent,
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'INVENTORY_MANAGER', 'STAFF'] }
      },
      {
        path: 'orders',
        component: OrdersComponent,
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'INVENTORY_MANAGER', 'STAFF'] }
      },
      {
        path: 'purchase-orders',
        component: PurchaseOrdersComponent,
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'INVENTORY_MANAGER'] }
      },
      {
        path: 'transfers',
        component: StockTransfersComponent,
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'INVENTORY_MANAGER'] }
      },
      {
        path: 'categories',
        component: CategoriesComponent,
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'INVENTORY_MANAGER'] }
      },
      {
        path: 'products',
        component: ProductsComponent,
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'INVENTORY_MANAGER'] }
      },
      {
        path: 'locations',
        component: LocationsComponent,
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'INVENTORY_MANAGER'] }
      },
      {
        path: 'suppliers',
        component: SuppliersComponent,
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'INVENTORY_MANAGER'] }
      },
      {
        path: 'users',
        component: UsersComponent,
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] }
      }
    ]
  },
  { path: '**', redirectTo: '' }
];
