import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { GraphqlService } from '../../../core/services/graphql.service';
import { AuthService } from '../../../core/services/auth';
import { HistorialViaje } from '../../../core/models/business.models';

@Component({
  selector: 'app-mis-reservas',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './mis-reservas.html'
})
export class MisReservas implements OnInit {
  private graphqlService = inject(GraphqlService);
  private authService = inject(AuthService);
  private route = inject(ActivatedRoute);

  reservas: HistorialViaje[] = [];
  isLoading = false;
  onlyCompleted = false;

  // Pagination
  paginaActual = 0;
  totalPaginas = 0;
  totalElementos = 0;
  tieneSiguiente = false;
  tamanioPagina = 5;

  ngOnInit() {
    this.route.url.subscribe(url => {
      this.onlyCompleted = url.some(segment => segment.path === 'itinerarios');
      this.cargarReservas();
    });
  }

  cargarReservas() {
    const user = this.authService.getCurrentUser();
    if (!user) return;

    this.isLoading = true;
    this.graphqlService.consultarHistorialViajes(Number(user.idUsuario), this.paginaActual, this.tamanioPagina).subscribe({
      next: (data) => {
        let content = data.contenido;
        if (this.onlyCompleted) {
          content = content.filter(r => r.estadoReserva === 'COMPLETADA');
        }
        this.reservas = content;
        this.totalPaginas = data.totalPaginas;
        this.totalElementos = data.totalElementos;
        this.tieneSiguiente = data.tieneSiguiente;
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        alert('Error al cargar historial: ' + err.message);
        this.isLoading = false;
      }
    });
  }

  cancelar(reservaId: number) {
    const user = this.authService.getCurrentUser();
    if (!user) return;

    if (confirm('¿Estás seguro de cancelar esta reserva?')) {
      this.graphqlService.cancelarReserva(reservaId, Number(user.idUsuario)).subscribe({
        next: () => {
          alert('Reserva cancelada exitosamente.');
          this.cargarReservas();
        },
        error: (err) => alert('Error al cancelar reserva: ' + err.message)
      });
    }
  }

  siguientePagina() {
    if (this.tieneSiguiente) {
      this.paginaActual++;
      this.cargarReservas();
    }
  }

  anteriorPagina() {
    if (this.paginaActual > 0) {
      this.paginaActual--;
      this.cargarReservas();
    }
  }
}
