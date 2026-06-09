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
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./features/inventory/inventory-dashboard/inventory-dashboard').then(m => m.InventoryDashboard),
    children: [
      {
        path: 'admin',
        canActivate: [roleGuard],
        data: { requiredRoles: [1, 3] }, // 1 = ADMINISTRADOR, 3 = GERENTE
        children: [
          { path: '', redirectTo: 'rutas', pathMatch: 'full' },
          { path: 'rutas', loadComponent: () => import('./features/inventory/rutas/rutas').then(m => m.Rutas) },
          { path: 'viajes', loadComponent: () => import('./features/inventory/viajes/viajes').then(m => m.Viajes) },
          { path: 'flotas', loadComponent: () => import('./features/inventory/flotas/flotas').then(m => m.Flotas) },
          { path: 'notificaciones', loadComponent: () => import('./features/inventory/usuarios/usuarios-notificaciones').then(m => m.UsuariosNotificaciones) }
        ]
      },
      {
        path: 'gerencia',
        canActivate: [roleGuard],
        data: { requiredRoles: [3] }, // 3 = GERENTE
        loadComponent: () => import('./features/dashboard-bi/dashboard-bi/dashboard-bi').then(m => m.DashboardBi)
      },
      {
        path: 'cliente',
        canActivate: [roleGuard],
        data: { requiredRoles: [2] }, // 2 = CLIENTE
        children: [
          { path: '', redirectTo: 'buscar', pathMatch: 'full' },
          { path: 'buscar', loadComponent: () => import('./features/cliente/buscar-viajes/buscar-viajes').then(m => m.BuscarViajes) },
          { path: 'itinerarios', loadComponent: () => import('./features/cliente/mis-reservas/mis-reservas').then(m => m.MisReservas) },
          { path: 'reservas', loadComponent: () => import('./features/cliente/mis-reservas/mis-reservas').then(m => m.MisReservas) }
        ]
      }
    ]
  },
  { path: '**', redirectTo: 'login' }
];
