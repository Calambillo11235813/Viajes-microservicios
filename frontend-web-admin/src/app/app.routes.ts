import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';
import { roleGuard } from './core/guards/role-guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login').then(m => m.Login)
  },
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard],
    data: { requiredRoles: [1] }, // 1 = ADMINISTRADOR
    loadComponent: () => import('./features/inventory/inventory-dashboard/inventory-dashboard').then(m => m.InventoryDashboard),
    children: [
      { path: '', redirectTo: 'rutas', pathMatch: 'full' },
      { path: 'rutas', loadComponent: () => import('./features/inventory/rutas/rutas').then(m => m.Rutas) },
      { path: 'viajes', loadComponent: () => import('./features/inventory/viajes/viajes').then(m => m.Viajes) },
      { path: 'flotas', loadComponent: () => import('./features/inventory/flotas/flotas').then(m => m.Flotas) }
    ]
  },
  {
    path: 'gerencia',
    canActivate: [authGuard, roleGuard],
    data: { requiredRoles: [3] }, // 3 = GERENTE
    loadComponent: () => import('./features/dashboard-bi/dashboard-bi/dashboard-bi').then(m => m.DashboardBi)
  },
  { path: '**', redirectTo: 'login' }
];
