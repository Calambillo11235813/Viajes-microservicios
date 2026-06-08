import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { GraphqlService } from '../../../core/services/graphql.service';
import { RutaDestino, ViajeDisponible } from '../../../core/models/business.models';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-viajes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './viajes.html'
})
export class Viajes implements OnInit {
  private fb = inject(FormBuilder);
  private graphqlService = inject(GraphqlService);
  public authService = inject(AuthService);

  rutas: RutaDestino[] = [];
  viajes = signal<ViajeDisponible[]>([]);
  
  isLoadingRutas = false;
  isLoadingViajes = signal(false);
  showModal = false;

  // Paginación
  paginaActual = signal(0);
  totalPaginas = signal(0);
  totalElementos = signal(0);
  tieneSiguiente = signal(false);

  rutaSeleccionadaId: number | null = null;
  
  viajeForm: FormGroup = this.fb.group({
    idRuta: ['', Validators.required],
    idBus: [1, Validators.required],
    fechaHoraSalida: ['', Validators.required]
  });

  ngOnInit(): void {
    this.cargarRutas();
  }

  cargarRutas() {
    this.isLoadingRutas = true;
    this.graphqlService.listarRutas(0, 100).subscribe({
      next: (data) => {
        this.rutas = data.contenido;
        this.isLoadingRutas = false;
        if(this.rutas.length > 0) {
          this.seleccionarRuta(Number(this.rutas[0].id));
        }
      },
      error: (err) => {
        console.error(err);
        this.isLoadingRutas = false;
      }
    });
  }

  seleccionarRuta(idRuta: number) {
    this.rutaSeleccionadaId = idRuta;
    this.paginaActual.set(0);
    this.cargarViajes(idRuta, 0);
  }

  cargarViajes(idRuta: number, pagina: number) {
    this.isLoadingViajes.set(true);
    this.graphqlService.listarViajesPorRuta(Number(idRuta), pagina, 10).subscribe({
      next: (data) => {
        this.viajes.set(data.contenido);
        this.paginaActual.set(data.paginaActual);
        this.totalPaginas.set(data.totalPaginas);
        this.totalElementos.set(data.totalElementos);
        this.tieneSiguiente.set(data.tieneSiguiente);
        this.isLoadingViajes.set(false);
      },
      error: (err) => {
        console.error(err);
        this.isLoadingViajes.set(false);
      }
    });
  }

  paginaSiguiente() {
    if (this.tieneSiguiente() && this.rutaSeleccionadaId) {
      this.cargarViajes(this.rutaSeleccionadaId, this.paginaActual() + 1);
    }
  }

  paginaAnterior() {
    if (this.paginaActual() > 0 && this.rutaSeleccionadaId) {
      this.cargarViajes(this.rutaSeleccionadaId, this.paginaActual() - 1);
    }
  }

  abrirModal() {
    const today = new Date();
    today.setHours(today.getHours() + 24);
    const tzoffset = (new Date()).getTimezoneOffset() * 60000;
    const localISOTime = (new Date(today.getTime() - tzoffset)).toISOString().slice(0, 16);

    this.viajeForm.reset({ 
      idRuta: this.rutaSeleccionadaId, 
      idBus: 1, 
      fechaHoraSalida: localISOTime + ':00'
    });
    this.showModal = true;
  }

  cerrarModal() {
    this.showModal = false;
  }

  programarViaje() {
    if (this.viajeForm.invalid) return;

    const v = this.viajeForm.value;
    let fecha = v.fechaHoraSalida;
    if(fecha.length === 16) {
      fecha += ':00';
    }

    this.graphqlService.programarViaje(Number(v.idRuta), Number(v.idBus), fecha).subscribe({
      next: () => {
        // Recargar la página actual para ver el nuevo viaje
        if(this.rutaSeleccionadaId) {
          this.cargarViajes(this.rutaSeleccionadaId, this.paginaActual());
        }
        this.cerrarModal();
      },
      error: (err) => alert('Error al programar el viaje: ' + err.message)
    });
  }
}
