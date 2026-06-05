import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { GraphqlService } from '../../../core/services/graphql.service';
import { RutaDestino, ViajeDisponible } from '../../../core/models/business.models';

@Component({
  selector: 'app-viajes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './viajes.html'
})
export class Viajes implements OnInit {
  private fb = inject(FormBuilder);
  private graphqlService = inject(GraphqlService);

  rutas: RutaDestino[] = [];
  viajes: ViajeDisponible[] = [];
  
  isLoadingRutas = false;
  isLoadingViajes = false;
  showModal = false;

  rutaSeleccionadaId: number | null = null;
  
  viajeForm: FormGroup = this.fb.group({
    idRuta: ['', Validators.required],
    idBus: [1, Validators.required], // Asumimos bus 1 por defecto ya que no hay endpoints de bus
    fechaHoraSalida: ['', Validators.required]
  });

  ngOnInit(): void {
    this.cargarRutas();
  }

  cargarRutas() {
    this.isLoadingRutas = true;
    this.graphqlService.listarRutas().subscribe({
      next: (data) => {
        this.rutas = data;
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
    this.cargarViajes(idRuta);
  }

  cargarViajes(idRuta: number) {
    this.isLoadingViajes = true;
    this.graphqlService.listarViajesPorRuta(Number(idRuta)).subscribe({
      next: (data) => {
        this.viajes = data;
        this.isLoadingViajes = false;
      },
      error: (err) => {
        console.error(err);
        this.isLoadingViajes = false;
      }
    });
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
    // Format required by backend usually is ISO string: 2026-06-15T10:00:00
    let fecha = v.fechaHoraSalida;
    if(fecha.length === 16) {
      fecha += ':00';
    }

    this.graphqlService.programarViaje(Number(v.idRuta), Number(v.idBus), fecha).subscribe({
      next: (nuevoViaje) => {
        if(this.rutaSeleccionadaId === Number(v.idRuta)) {
          this.viajes.push(nuevoViaje);
        }
        this.cerrarModal();
      },
      error: (err) => alert('Error al programar el viaje: ' + err.message)
    });
  }
}
