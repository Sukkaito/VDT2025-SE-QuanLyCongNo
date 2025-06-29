import { Routes } from '@angular/router';

export const invoiceRoutes: Routes = [
    {
        path: '',
        loadComponent: () => import('./invoice.component').then(m => m.InvoiceComponent),
    },
    {
        path: ':id',
        loadComponent: () => import('./pages/invoice-detail/invoice-detail.component').then(m => m.InvoiceDetailComponent),
    }
];