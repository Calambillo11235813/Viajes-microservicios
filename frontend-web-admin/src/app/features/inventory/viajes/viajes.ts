import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { GraphqlService } from '../../../core/services/graphql.service';
import { RutaDestino, ViajeDisponible } from '../../../core/models/business.models';
import { AuthService } from '../../../core/services/auth';
import { appLog } from '../../../core/utils/logger';
import { fromDatetimeLocalValue, toDatetimeLocalValue } from '../../../core/utils/datetime-local';

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
  showAlertaModal = false;
  showEditarModal = false;

  viajeSeleccionado: ViajeDisponible | null = null;
  ultimoResultadoNotificacion = signal<string | null>(null);

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

  alertaForm: FormGroup = this.fb.group({
    titulo: ['Alerta en ruta', Validators.required],
    mensaje: ['', Validators.required]
  });

  editarForm: FormGroup = this.fb.group({
    fechaHoraSalida: ['', Validators.required],
    idBus: [1, Validators.required]
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
        if (this.rutas.length > 0) {
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

  abrirAlertaModal(viaje: ViajeDisponible) {
    this.viajeSeleccionado = viaje;
    this.alertaForm.reset({
      titulo: 'Alerta en ruta',
      mensaje: `Atención pasajeros del viaje ${viaje.ciudadOrigen} → ${viaje.ciudadDestino}.`
    });
    this.showAlertaModal = true;
  }

  cerrarAlertaModal() {
    this.showAlertaModal = false;
    this.viajeSeleccionado = null;
  }

  abrirEditarModal(viaje: ViajeDisponible) {
    this.viajeSeleccionado = viaje;

    this.editarForm.reset({
      fechaHoraSalida: toDatetimeLocalValue(viaje.fechaHoraSalida),
      idBus: viaje.idBus
    });
    this.showEditarModal = true;
  }

  cerrarEditarModal() {
    this.showEditarModal = false;
    this.viajeSeleccionado = null;
  }

  programarViaje() {
    if (this.viajeForm.invalid) return;

    const v = this.viajeForm.value;
    const fecha = fromDatetimeLocalValue(v.fechaHoraSalida);

    this.graphqlService.programarViaje(Number(v.idRuta), Number(v.idBus), fecha).subscribe({
      next: () => {
        if (this.rutaSeleccionadaId) {
          this.cargarViajes(this.rutaSeleccionadaId, this.paginaActual());
        }
        this.cerrarModal();
      },
      error: (err) => alert('Error al programar el viaje: ' + err.message)
    });
  }

  enviarAlertaEmergencia() {
    if (!this.viajeSeleccionado || this.alertaForm.invalid) return;

    const v = this.alertaForm.value;
    const payload = {
      idViaje: Number(this.viajeSeleccionado.idViaje),
      tipo: 'EMERGENCIA_RUTA',
      titulo: v.titulo,
      mensaje: v.mensaje,
      datosExtraJson: JSON.stringify({ idViaje: this.viajeSeleccionado.idViaje, severidad: 'ALTA' })
    };
    appLog.info('Notif Admin', 'Solicitud alerta emergencia:', {
      ...payload,
      viaje: `${this.viajeSeleccionado.ciudadOrigen} → ${this.viajeSeleccionado.ciudadDestino}`,
    });

    this.graphqlService.enviarNotificacionPorViaje(payload).subscribe({
      next: (notificaciones) => {
        appLog.info('Notif Admin', 'Alerta emergencia confirmada:', {
          destinatarios: notificaciones.length,
          idsNotificacion: notificaciones.map((n: { id: number; idUsuario: number }) => ({
            id: n.id,
            idUsuario: n.idUsuario,
          })),
        });
        this.ultimoResultadoNotificacion.set(
          `Alerta enviada a ${notificaciones.length} pasajero(s).`
        );
        this.cerrarAlertaModal();
      },
      error: (err) => {
        appLog.error('Notif Admin', 'Error al enviar alerta emergencia:', err.message, err);
        alert('Error al enviar alerta: ' + err.message);
      }
    });
  }

  actualizarHorarioViaje() {
    if (!this.viajeSeleccionado || this.editarForm.invalid) return;

    const v = this.editarForm.value;
    const fecha = fromDatetimeLocalValue(v.fechaHoraSalida);

    appLog.info('Notif Admin', 'Solicitud cambio horario:', {
      idViaje: this.viajeSeleccionado.idViaje,
      idBus: v.idBus,
      fechaHoraSalida: fecha,
      horarioAnterior: this.viajeSeleccionado.fechaHoraSalida,
    });

    this.graphqlService.actualizarViajeProgramado(
      Number(this.viajeSeleccionado.idViaje),
      Number(v.idBus),
      fecha
    ).subscribe({
      next: () => {
        appLog.info('Notif Admin', 'Cambio horario OK — backend notificará pasajeros (CAMBIO_HORARIO)');
        this.ultimoResultadoNotificacion.set(
          'Horario actualizado. Se notificó automáticamente a los pasajeros afectados.'
        );
        if (this.rutaSeleccionadaId) {
          this.cargarViajes(this.rutaSeleccionadaId, this.paginaActual());
        }
        this.cerrarEditarModal();
      },
      error: (err) => {
        appLog.error('Notif Admin', 'Error al actualizar viaje:', err.message, err);
        alert('Error al actualizar viaje: ' + err.message);
      }
    });
  }

  cancelarViaje(viaje: ViajeDisponible) {
    if (!confirm(`¿Cancelar el viaje #${viaje.idViaje} (${viaje.ciudadOrigen} → ${viaje.ciudadDestino})?`)) {
      return;
    }

    appLog.info('Notif Admin', 'Solicitud cancelación viaje:', { idViaje: viaje.idViaje });

    this.graphqlService.cancelarViajeProgramado(Number(viaje.idViaje)).subscribe({
      next: () => {
        appLog.info('Notif Admin', 'Cancelación OK — backend notificará pasajeros (CANCELACION)');
        this.ultimoResultadoNotificacion.set(
          'Viaje cancelado. Se notificó automáticamente a los pasajeros afectados.'
        );
        if (this.rutaSeleccionadaId) {
          this.cargarViajes(this.rutaSeleccionadaId, this.paginaActual());
        }
      },
      error: (err) => {
        appLog.error('Notif Admin', 'Error al cancelar viaje:', err.message, err);
        alert('Error al cancelar viaje: ' + err.message);
      }
    });
  }
}
