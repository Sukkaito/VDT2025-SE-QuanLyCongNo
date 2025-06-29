import { Routes } from '@angular/router';

export const contractRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./contract.component').then(m => m.ContractComponent),
  },
  {
    path: ':id',
    loadComponent: () => import('./pages/contract-detail/contract-detail.component').then(m => m.ContractDetailComponent),
  }
];