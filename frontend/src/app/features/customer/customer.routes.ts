import { Routes } from '@angular/router';

export const customerRoutes: Routes = [
    {
        path: '',
        loadComponent: () => import('./customer.component').then(m => m.CustomerComponent),
    },
    {
        path: ':id',
        loadComponent: () => import('./pages/customer-detail/customer-detail.component').then(m => m.CustomerDetailComponent),
    }
];