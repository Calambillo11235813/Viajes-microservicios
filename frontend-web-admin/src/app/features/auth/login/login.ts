import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.html'
})
export class Login {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  loginForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  errorMessage: string = '';
  isLoading: boolean = false;

  onSubmit() {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(this.loginForm.value).subscribe({
      next: (res) => {
        const idRol = res.perfil.idRol;
        if (idRol === 1) {
          this.router.navigate(['/admin']);
        } else if (idRol === 3) {
          this.router.navigate(['/gerencia']);
        } else {
          this.errorMessage = 'Tu rol no tiene acceso a este panel.';
          this.authService.logout();
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.message || 'Error al iniciar sesión';
        this.isLoading = false;
      }
    });
  }
}
