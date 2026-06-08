import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { GraphqlService } from '../../../core/services/graphql.service';
import { AuthService } from '../../../core/services/auth';
import { ViajeDisponible, AsientoEstado, ReservaProvisional, DestinoRecomendado, ReelTuristico } from '../../../core/models/business.models';

@Component({
  selector: 'app-buscar-viajes',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './buscar-viajes.html'
})
export class BuscarViajes implements OnInit {
  private fb = inject(FormBuilder);
  private graphqlService = inject(GraphqlService);
  private authService = inject(AuthService);

  // Search Form
  searchForm: FormGroup = this.fb.group({
    origen: ['Santa Cruz', Validators.required],
    destino: ['La Paz', Validators.required],
    fecha: [new Date().toISOString().substring(0, 10), Validators.required]
  });

  // State Variables
  viajes: ViajeDisponible[] = [];
  isLoading = false;
  hasSearched = false;
  viajeSeleccionado: ViajeDisponible | null = null;
  asientos: AsientoEstado[] = [];
  asientoSeleccionado: string | null = null;

  // Reservation details
  reservaForm: FormGroup = this.fb.group({
    nombrePasajero: ['', Validators.required],
    tipoPasajero: ['ADULTO', Validators.required]
  });

  reservaProvisional: ReservaProvisional | null = null;
  showPagoModal = false;
  isPaying = false;
  pagoExitoso = false;
  cupon = '';

  // AI Image Search (CU-06)
  imageUrl = 'https://images.unsplash.com/photo-1580618672591-eb180b1a973f?w=600';
  destinosRecomendados: DestinoRecomendado[] = [];
  isLoadingIA = false;
  showIAPanel = false;

  // AI Reels (CU-07)
  reelTuristico: ReelTuristico | null = null;
  isLoadingReel = false;
  activeReelRutaId: number | null = null;

  ngOnInit() {
    // Cargar perfil predeterminado del pasajero
    const user = this.authService.getCurrentUser();
    if (user) {
      this.reservaForm.patchValue({
        nombrePasajero: user.nombreCompleto
      });
    }
    this.buscar();
  }

  buscar() {
    if (this.searchForm.invalid) return;
    this.isLoading = true;
    this.hasSearched = true;
    this.viajeSeleccionado = null;
    this.asientos = [];
    this.asientoSeleccionado = null;

    const { origen, destino, fecha } = this.searchForm.value;
    this.graphqlService.buscarRutasYHorariosDisponibles(origen, destino, fecha, 0, 20).subscribe({
      next: (data) => {
        this.viajes = data.contenido;
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        alert('Error en búsqueda: ' + err.message);
        this.isLoading = false;
      }
    });
  }

  seleccionarViaje(viaje: ViajeDisponible) {
    this.viajeSeleccionado = viaje;
    this.asientos = [];
    this.asientoSeleccionado = null;
    this.reservaProvisional = null;
    this.graphqlService.obtenerMapaAsientos(Number(viaje.idViaje)).subscribe({
      next: (data) => {
        this.asientos = data;
      },
      error: (err) => alert('Error al cargar asientos: ' + err.message)
    });
  }

  elegirAsiento(asiento: string) {
    this.asientoSeleccionado = asiento;
  }

  reservar() {
    if (!this.viajeSeleccionado || !this.asientoSeleccionado || this.reservaForm.invalid) return;
    const user = this.authService.getCurrentUser();
    if (!user) {
      alert('Debes iniciar sesión.');
      return;
    }

    const { nombrePasajero, tipoPasajero } = this.reservaForm.value;
    this.graphqlService.seleccionarAsientoYReservar(
      Number(user.idUsuario),
      Number(this.viajeSeleccionado.idViaje),
      this.asientoSeleccionado,
      nombrePasajero,
      tipoPasajero
    ).subscribe({
      next: (reserva) => {
        this.reservaProvisional = reserva;
        this.showPagoModal = true;
        this.pagoExitoso = false;
      },
      error: (err) => alert('Error al realizar la reserva: ' + err.message)
    });
  }

  pagar() {
    if (!this.reservaProvisional) return;
    this.isPaying = true;
    
    this.graphqlService.realizarPago(
      Number(this.reservaProvisional.idReserva),
      'QR',
      this.reservaProvisional.montoEstimado,
      true,
      this.cupon || null
    ).subscribe({
      next: (pago) => {
        this.isPaying = false;
        this.pagoExitoso = true;
        // Refrescar listado y reiniciar selección
        this.buscar();
      },
      error: (err) => {
        alert('Error al realizar pago: ' + err.message);
        this.isPaying = false;
      }
    });
  }

  cerrarPagoModal() {
    this.showPagoModal = false;
    this.reservaProvisional = null;
    this.asientoSeleccionado = null;
    this.viajeSeleccionado = null;
  }

  // CU-06: Buscar por imagen
  buscarDestinosImagen() {
    this.isLoadingIA = true;
    this.graphqlService.buscarDestinosPorImagen(this.imageUrl).subscribe({
      next: (destinos) => {
        this.destinosRecomendados = destinos;
        this.isLoadingIA = false;
      },
      error: (err) => {
        alert('Error IA: ' + err.message);
        this.isLoadingIA = false;
      }
    });
  }

  // CU-07: Generación de Reel
  generarReel(idRuta: number) {
    this.isLoadingReel = true;
    this.activeReelRutaId = idRuta;
    this.graphqlService.generarReelTuristico(idRuta).subscribe({
      next: (reel) => {
        this.reelTuristico = reel;
        this.isLoadingReel = false;
      },
      error: (err) => {
        alert('Error al generar Reel: ' + err.message);
        this.isLoadingReel = false;
        this.activeReelRutaId = null;
      }
    });
  }
}
