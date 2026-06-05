import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const userProfile = authService.getCurrentUser();

  if (!userProfile) {
    router.navigate(['/login']);
    return false;
  }

  // Expecting requiredRoles in route data like: { data: { requiredRoles: [1, 3] } }
  const requiredRoles: number[] = route.data?.['requiredRoles'];
  
  if (requiredRoles && requiredRoles.includes(userProfile.idRol)) {
    return true;
  }

  // Not authorized
  alert('Acceso Denegado: No tienes permisos para acceder a esta sección.');
  router.navigate(['/login']);
  return false;
};
