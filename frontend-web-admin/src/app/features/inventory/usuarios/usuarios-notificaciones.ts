import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { GraphqlService } from '../../../core/services/graphql.service';
import { RutaDestino, ViajeDisponible } from '../../../core/models/business.models';
import { AuthService } from '../../../core/services/auth';
import { appLog } from '../../../core/utils/logger';

@Component({
  selector: 'app-usuarios-notificaciones',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './usuarios-notificaciones.html'
})
export class UsuariosNotificaciones implements OnInit {
  private fb = inject(FormBuilder);
  private graphqlService = inject(GraphqlService);
  public authService = inject(AuthService);

  rutas: RutaDestino[] = [];
  viajes = signal<ViajeDisponible[]>([]);
  viajeSeleccionado = signal<ViajeDisponible | null>(null);

  isLoadingRutas = signal(false);
  isLoadingViajes = signal(false);
  isEnviando = signal(false);
  ultimoResultado = signal<string | null>(null);
  ultimoResultadoEsError = signal(false);

  paginaActual = signal(0);
  totalPaginas = signal(0);
  tieneSiguiente = signal(false);

  rutaSeleccionadaId: number | null = null;

  docForm: FormGroup = this.fb.group({
    mensaje: [
      'Recuerda llevar tu documento de identidad (CI) para abordar el bus.',
      Validators.required
    ]
  });

  ngOnInit(): void {
    this.cargarRutas();
  }

  cargarRutas() {
    this.isLoadingRutas.set(true);
    this.graphqlService.listarRutas(0, 100).subscribe({
      next: (data) => {
        this.rutas = data.contenido;
        this.isLoadingRutas.set(false);
        if (this.rutas.length > 0) {
          this.seleccionarRuta(Number(this.rutas[0].id));
        }
      },
      error: (err) => {
        console.error(err);
        this.isLoadingRutas.set(false);
      }
    });
  }

  seleccionarRuta(idRuta: number) {
    this.rutaSeleccionadaId = idRuta;
    this.paginaActual.set(0);
    this.viajeSeleccionado.set(null);
    this.ultimoResultado.set(null);
    this.ultimoResultadoEsError.set(false);
    this.cargarViajes(idRuta, 0);
  }

  cargarViajes(idRuta: number, pagina: number) {
    this.isLoadingViajes.set(true);
    this.graphqlService.listarViajesPorRuta(Number(idRuta), pagina, 10).subscribe({
      next: (data) => {
        this.viajes.set(data.contenido);
        this.paginaActual.set(data.paginaActual);
        this.totalPaginas.set(data.totalPaginas);
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

  seleccionarViaje(viaje: ViajeDisponible) {
    if (viaje.estadoViaje === 'CANCELADO') {
      return;
    }
    this.viajeSeleccionado.set(viaje);
    this.ultimoResultado.set(null);
    this.ultimoResultadoEsError.set(false);
  }

  esViajeSeleccionado(viaje: ViajeDisponible): boolean {
    const seleccionado = this.viajeSeleccionado();
    return seleccionado?.idViaje === viaje.idViaje;
  }

  enviarDocumentacionPendiente() {
    const viaje = this.viajeSeleccionado();
    if (!viaje || this.docForm.invalid || viaje.estadoViaje === 'CANCELADO') {
      return;
    }

    const mensaje = this.docForm.value.mensaje;
    const payload = {
      idViaje: Number(viaje.idViaje),
      tipo: 'DOCUMENTACION_FALTANTE',
      titulo: 'Documentación pendiente',
      mensaje,
      datosExtraJson: JSON.stringify({ motivo: 'CI_PENDIENTE', idViaje: viaje.idViaje })
    };
    appLog.info('Notif Admin', 'Solicitud recordatorio CI por viaje:', {
      ...payload,
      viaje: `${viaje.ciudadOrigen} → ${viaje.ciudadDestino}`,
    });

    this.isEnviando.set(true);
    this.ultimoResultado.set(null);
    this.ultimoResultadoEsError.set(false);

    this.graphqlService.enviarNotificacionPorViaje(payload).subscribe({
      next: (notificaciones) => {
        appLog.info('Notif Admin', 'Recordatorio CI confirmado:', {
          destinatarios: notificaciones.length,
          idsNotificacion: notificaciones.map((n: { id: number; idUsuario: number }) => n.id),
        });

        if (notificaciones.length === 0) {
          this.ultimoResultado.set('No hay pasajeros confirmados para este viaje.');
          this.ultimoResultadoEsError.set(true);
        } else {
          this.ultimoResultado.set(
            `Recordatorio enviado a ${notificaciones.length} pasajero(s) confirmado(s).`
          );
          this.ultimoResultadoEsError.set(false);
        }
        this.isEnviando.set(false);
      },
      error: (err) => {
        appLog.error('Notif Admin', 'Error recordatorio CI por viaje:', err.message, err);
        this.ultimoResultado.set('Error al enviar notificación: ' + err.message);
        this.ultimoResultadoEsError.set(true);
        this.isEnviando.set(false);
      }
    });
  }
}
