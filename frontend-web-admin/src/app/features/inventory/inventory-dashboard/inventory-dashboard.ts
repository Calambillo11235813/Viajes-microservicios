import { Component, inject } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-inventory-dashboard',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './inventory-dashboard.html'
})
export class InventoryDashboard {
  private authService = inject(AuthService);
  private router = inject(Router);

  // Exponemos el rol para usarlo en el HTML
  get rolId(): number | undefined {
    return this.authService.getCurrentUser()?.idRol;
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
