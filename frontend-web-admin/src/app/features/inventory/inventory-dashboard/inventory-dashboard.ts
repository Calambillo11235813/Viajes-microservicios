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

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
