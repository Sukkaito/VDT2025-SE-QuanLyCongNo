import { Routes } from '@angular/router';
import { authRoutes } from './auth/auth.routes';
import { customerRoutes } from './features/customer/customer.routes';
import { invoiceRoutes } from './features/invoice/invoice.routes';
import { contractRoutes } from './features/contract/contract.routes';
import { dashboardRoutes } from './features/dashboard/dashboard.routes';
import { DashboardLayoutComponent } from './features/dashboard-layout.component';
import { AuthComponent } from './auth/auth.component';
import { AuthGuard } from './core/guards/auth.guard';

export const appRoutes: Routes = [
  {
    path: 'auth',
    component: AuthComponent,
    children: authRoutes,
  },
  {
    path: '',
    component: DashboardLayoutComponent,
    children: [
      {
        path: 'customers',
        children: customerRoutes,
      },
      {
        path: 'invoices',
        children: invoiceRoutes,
      },
      {
        path: 'contracts',
        children: contractRoutes,
      },
      {
        path: 'dashboard',
        children: dashboardRoutes,
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'customers'
      }
    ],
    canActivate: [AuthGuard],
  },
  {
    path: '**',
    redirectTo: 'auth/login'
  }
];
