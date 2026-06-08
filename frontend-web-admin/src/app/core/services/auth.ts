import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, map, tap, catchError, throwError } from 'rxjs';
import { LoginCredentials, AuthResponse, UsuarioPerfil } from '../models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:9090/graphql';
  
  private currentUserSubject = new BehaviorSubject<UsuarioPerfil | null>(this.getStoredProfile());
  public currentUser$ = this.currentUserSubject.asObservable();

  /**
   * Envía las credenciales al backend mediante GraphQL para autenticarse.
   * @param credentials Objeto con email y password
   * @returns Observable con la respuesta de autenticación
   */
  login(credentials: LoginCredentials): Observable<AuthResponse> {
    const query = `
      mutation Login($email: String!, $passwordHash: String!) {
        login(email: $email, passwordHash: $passwordHash) {
          token
          usuario {
            idUsuario
            ciPasaporte
            nombreCompleto
            email
            telefono
            idRol
          }
        }
      }
    `;

    return this.http.post<any>(this.API_URL, {
      query,
      variables: { email: credentials.email, passwordHash: credentials.password }
    }).pipe(
      map(response => {
        if (response.errors) {
          throw new Error(response.errors[0].message);
        }
        const loginData = response.data.login;
        // Normalizar: el backend devuelve `usuario`, nuestros modelos usan `perfil`
        return { token: loginData.token, perfil: loginData.usuario } as AuthResponse;
      }),
      tap(authResponse => {
        this.saveSession(authResponse);
      }),
      catchError(error => {
        console.error('Error en autenticación:', error);
        return throwError(() => new Error(error.message || 'Error de conexión'));
      })
    );
  }

  /**
   * Cierra la sesión eliminando el estado local
   */
  logout(): void {
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem('token');
      localStorage.removeItem('userProfile');
    }
    this.currentUserSubject.next(null);
  }

  /**
   * Retorna el token actual
   */
  getToken(): string | null {
    if (typeof localStorage !== 'undefined') {
      return localStorage.getItem('token');
    }
    return null;
  }

  /**
   * Retorna el perfil del usuario actual sincrónicamente
   */
  getCurrentUser(): UsuarioPerfil | null {
    return this.currentUserSubject.value;
  }

  /**
   * Verifica si el usuario actual tiene el rol especificado
   */
  hasRole(roleName: string): boolean {
    const user = this.getCurrentUser();
    if (!user) return false;
    if (roleName === 'ADMINISTRADOR' && user.idRol === 1) return true;
    if (roleName === 'CLIENTE' && user.idRol === 2) return true;
    if (roleName === 'GERENTE' && user.idRol === 3) return true;
    return false;
  }

  private saveSession(authResponse: AuthResponse): void {
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem('token', authResponse.token);
      localStorage.setItem('userProfile', JSON.stringify(authResponse.perfil));
    }
    this.currentUserSubject.next(authResponse.perfil);
  }

  private getStoredProfile(): UsuarioPerfil | null {
    if (typeof localStorage !== 'undefined') {
      const stored = localStorage.getItem('userProfile');
      return stored ? JSON.parse(stored) : null;
    }
    return null;
  }
}

