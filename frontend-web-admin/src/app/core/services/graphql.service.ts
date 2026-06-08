import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, map, throwError, catchError } from 'rxjs';
import { RutaDestino, ViajeDisponible, ReporteVentas, Flota, PaginaViajes, AsientoEstado, ReservaProvisional, PagoConfirmado, PaginaHistorial, DestinoRecomendado, ReelTuristico } from '../models/business.models';
import { AuthService } from './auth';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class GraphqlService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private router = inject(Router);
  private readonly API_URL = 'http://localhost:9090/graphql';

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    let headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    return headers;
  }

  private executeQuery<T>(query: string, variables: any = {}): Observable<T> {
    return this.http.post<any>(this.API_URL, { query, variables }, { headers: this.getHeaders() }).pipe(
      map(response => {
        if (response.errors) {
          const msg = response.errors[0].message;
          // Si el error es de autenticación, redirigir al login
          if (msg.includes('Unauthorized') || msg.includes('403') || msg.includes('token')) {
            this.authService.logout();
            this.router.navigate(['/login']);
          }
          throw new Error(msg);
        }
        return response.data;
      }),
      catchError(error => {
        // Interceptar errores HTTP 401/403
        if (error.status === 401 || error.status === 403) {
          this.authService.logout();
          this.router.navigate(['/login']);
        }
        return throwError(() => new Error(error.message || 'Error GraphQL'));
      })
    );
  }

  // --- RUTAS (PAGINADOS) ---
  listarRutas(pagina = 0, tamanio = 10): Observable<import('../models/business.models').PaginaRutas> {
    const query = `
      query ListarRutas($pagina: Int, $tamanio: Int) {
        listarRutas(pagina: $pagina, tamanio: $tamanio) {
          contenido {
            id
            ciudadOrigen
            ciudadDestino
            duracionEstimadaHoras
            precioBase
            categoriaTuristica
          }
          totalPaginas
          totalElementos
          paginaActual
          tieneSiguiente
        }
      }
    `;
    return this.executeQuery<any>(query, { pagina, tamanio }).pipe(map(data => data.listarRutas));
  }

  crearRuta(ruta: Partial<RutaDestino>): Observable<RutaDestino> {
    const query = `
      mutation CrearRuta($origen: String!, $destino: String!, $duracion: Float!, $precio: Float!, $categoria: String) {
        crearRuta(origen: $origen, destino: $destino, duracion: $duracion, precio: $precio, categoria: $categoria) {
          id
          ciudadOrigen
          ciudadDestino
          duracionEstimadaHoras
          precioBase
          categoriaTuristica
        }
      }
    `;
    return this.executeQuery<any>(query, {
      origen: ruta.ciudadOrigen,
      destino: ruta.ciudadDestino,
      duracion: ruta.duracionEstimadaHoras,
      precio: ruta.precioBase,
      categoria: ruta.categoriaTuristica || 'Normal'
    }).pipe(map(data => data.crearRuta));
  }

  eliminarRuta(idRuta: number): Observable<boolean> {
    const query = `
      mutation EliminarRuta($id: Int!) {
        eliminarRuta(idRuta: $id)
      }
    `;
    return this.executeQuery<any>(query, { id: idRuta }).pipe(map(data => data.eliminarRuta));
  }

  // --- VIAJES (PAGINADOS) ---
  listarViajesPorRuta(idRuta: number, pagina = 0, tamanio = 10): Observable<PaginaViajes> {
    const query = `
      query ListarViajes($idRuta: Int!, $pagina: Int, $tamanio: Int) {
        listarViajesPorRuta(idRuta: $idRuta, pagina: $pagina, tamanio: $tamanio) {
          contenido {
            idViaje
            idRuta
            ciudadOrigen
            ciudadDestino
            fechaHoraSalida
            fechaHoraLlegada
            precioBase
            idBus
            estadoViaje
          }
          totalPaginas
          totalElementos
          paginaActual
          tieneSiguiente
        }
      }
    `;
    return this.executeQuery<any>(query, { idRuta, pagina, tamanio }).pipe(map(data => data.listarViajesPorRuta));
  }

  programarViaje(idRuta: number, idBus: number, fechaHoraSalida: string): Observable<ViajeDisponible> {
    const query = `
      mutation ProgramarViaje($idRuta: Int!, $idBus: Int!, $fecha: String!) {
        programarViaje(idRuta: $idRuta, idBus: $idBus, fechaHoraSalida: $fecha) {
          idViaje
          ciudadOrigen
          ciudadDestino
          fechaHoraSalida
          estadoViaje
        }
      }
    `;
    return this.executeQuery<any>(query, { idRuta, idBus, fecha: fechaHoraSalida }).pipe(map(data => data.programarViaje));
  }

  // --- REPORTES ---
  generarReporteVentas(fechaInicio: string, fechaFin: string): Observable<ReporteVentas> {
    const query = `
      query GenerarReporte($inicio: String!, $fin: String!) {
        generarReporteVentas(fechaInicio: $inicio, fechaFin: $fin) {
          montoTotal
          cantidadPagos
          ocupacionFlota
          fechaInicio
          fechaFin
          detallesPorFecha {
            fecha
            montoDia
            cantidadPagosDia
          }
        }
      }
    `;
    return this.executeQuery<any>(query, { inicio: fechaInicio, fin: fechaFin }).pipe(map(data => data.generarReporteVentas));
  }

  // --- FLOTAS ---
  listarFlotas(): Observable<Flota[]> {
    const query = `
      query {
        listarFlotas {
          idBus
          placa
          capacidadTotalAsientos
          tipoBus
        }
      }
    `;
    return this.executeQuery<any>(query).pipe(map(data => data.listarFlotas));
  }

  crearFlota(flota: Partial<Flota>): Observable<Flota> {
    const query = `
      mutation CrearFlota($placa: String!, $capacidad: Int!, $tipo: String!) {
        crearFlota(placa: $placa, capacidadTotalAsientos: $capacidad, tipoBus: $tipo) {
          idBus
          placa
          capacidadTotalAsientos
          tipoBus
        }
      }
    `;
    return this.executeQuery<any>(query, {
      placa: flota.placa,
      capacidad: flota.capacidadTotalAsientos,
      tipo: flota.tipoBus
    }).pipe(map(data => data.crearFlota));
  }

  actualizarFlota(idBus: number, flota: Partial<Flota>): Observable<Flota> {
    const query = `
      mutation ActualizarFlota($idBus: Int!, $placa: String, $capacidad: Int, $tipo: String) {
        actualizarFlota(idBus: $idBus, placa: $placa, capacidadTotalAsientos: $capacidad, tipoBus: $tipo) {
          idBus
          placa
          capacidadTotalAsientos
          tipoBus
        }
      }
    `;
    return this.executeQuery<any>(query, {
      idBus: idBus,
      placa: flota.placa,
      capacidad: flota.capacidadTotalAsientos,
      tipo: flota.tipoBus
    }).pipe(map(data => data.actualizarFlota));
  }

  eliminarFlota(idBus: number): Observable<boolean> {
    const query = `
      mutation EliminarFlota($idBus: Int!) {
        eliminarFlota(idBus: $idBus)
      }
    `;
    return this.executeQuery<any>(query, { idBus }).pipe(map(data => data.eliminarFlota));
  }

  // --- CLIENTE API ---
  buscarRutasYHorariosDisponibles(origen: string, destino: string, fecha: string, pagina = 0, tamanio = 10): Observable<PaginaViajes> {
    const query = `
      query BuscarViajes($origen: String!, $destino: String!, $fecha: String!, $pagina: Int, $tamanio: Int) {
        buscarRutasYHorariosDisponibles(origen: $origen, destino: $destino, fecha: $fecha, pagina: $pagina, tamanio: $tamanio) {
          contenido {
            idViaje
            idRuta
            ciudadOrigen
            ciudadDestino
            fechaHoraSalida
            fechaHoraLlegada
            precioBase
            tipoBus
            capacidadTotalAsientos
            estadoViaje
          }
          totalPaginas
          totalElementos
          paginaActual
          tieneSiguiente
        }
      }
    `;
    return this.executeQuery<any>(query, { origen, destino, fecha, pagina, tamanio }).pipe(map(data => data.buscarRutasYHorariosDisponibles));
  }

  obtenerMapaAsientos(idViaje: number): Observable<AsientoEstado[]> {
    const query = `
      query ObtenerMapa($idViaje: Int!) {
        obtenerMapaAsientos(idViaje: $idViaje) {
          numeroAsiento
          ocupado
        }
      }
    `;
    return this.executeQuery<any>(query, { idViaje }).pipe(map(data => data.obtenerMapaAsientos));
  }

  seleccionarAsientoYReservar(idUsuario: number, idViaje: number, numeroAsiento: string, nombrePasajero: string, tipoPasajero: string): Observable<ReservaProvisional> {
    const query = `
      mutation Reservar($idUsuario: Int!, $idViaje: Int!, $numeroAsiento: String!, $nombrePasajero: String!, $tipoPasajero: String) {
        seleccionarAsientoYReservar(
          idUsuario: $idUsuario
          idViaje: $idViaje
          numeroAsiento: $numeroAsiento
          nombrePasajero: $nombrePasajero
          tipoPasajero: $tipoPasajero
        ) {
          idReserva
          idBoleto
          idViaje
          idUsuario
          numeroAsiento
          nombrePasajero
          tipoPasajero
          estadoReserva
          estadoBoleto
          montoEstimado
          fechaCreacion
        }
      }
    `;
    return this.executeQuery<any>(query, { idUsuario, idViaje, numeroAsiento, nombrePasajero, tipoPasajero }).pipe(map(data => data.seleccionarAsientoYReservar));
  }

  realizarPago(idReserva: number, metodoPagoUsado: string, montoTransaccion: number, acreditado: boolean, cuponDescuentoAplicado: string | null = null): Observable<PagoConfirmado> {
    const query = `
      mutation Pagar($idReserva: Int!, $metodoPagoUsado: String!, $montoTransaccion: Float!, $acreditado: Boolean!, $cuponDescuentoAplicado: String) {
        realizarPago(
          idReserva: $idReserva
          metodoPagoUsado: $metodoPagoUsado
          montoTransaccion: $montoTransaccion
          acreditado: $acreditado
          cuponDescuentoAplicado: $cuponDescuentoAplicado
        ) {
          idPago
          idReserva
          idUsuario
          idViaje
          montoTransaccion
          metodoPagoUsado
          estadoPago
          estadoReserva
          fechaPago
          eventoEmitido
        }
      }
    `;
    return this.executeQuery<any>(query, { idReserva, metodoPagoUsado, montoTransaccion, acreditado, cuponDescuentoAplicado }).pipe(map(data => data.realizarPago));
  }

  cancelarReserva(idReserva: number, idUsuario: number): Observable<any> {
    const query = `
      mutation Cancelar($idReserva: Int!, $idUsuario: Int!) {
        cancelarReserva(idReserva: $idReserva, idUsuario: $idUsuario) {
          idReserva
          idUsuario
          idViaje
          ciudadOrigen
          ciudadDestino
          estadoReserva
          estadoPago
          boletosAnulados
          montoReembolsado
          fechaCancelacion
        }
      }
    `;
    return this.executeQuery<any>(query, { idReserva, idUsuario }).pipe(map(data => data.cancelarReserva));
  }

  consultarHistorialViajes(idUsuario: number, pagina = 0, tamanio = 10): Observable<PaginaHistorial> {
    const query = `
      query Historial($idUsuario: Int!, $pagina: Int, $tamanio: Int) {
        consultarHistorialViajes(idUsuario: $idUsuario, pagina: $pagina, tamanio: $tamanio) {
          contenido {
            idReserva
            idViaje
            ciudadOrigen
            ciudadDestino
            fechaHoraSalida
            fechaCreacion
            estadoReserva
            montoTotalPagado
            cantidadPasajeros
          }
          totalPaginas
          totalElementos
          paginaActual
          tieneSiguiente
        }
      }
    `;
    return this.executeQuery<any>(query, { idUsuario, pagina, tamanio }).pipe(map(data => data.consultarHistorialViajes));
  }

  buscarDestinosPorImagen(urlImagen: string): Observable<DestinoRecomendado[]> {
    const query = `
      query BuscarDestinos($urlImagen: String!) {
        buscarDestinosPorImagen(urlImagen: $urlImagen) {
          ciudad
          porcentajeCoincidencia
          descripcion
        }
      }
    `;
    return this.executeQuery<any>(query, { urlImagen }).pipe(map(data => data.buscarDestinosPorImagen));
  }

  generarReelTuristico(idRuta: number): Observable<ReelTuristico> {
    const query = `
      query GenerarReel($idRuta: Int!) {
        generarReelTuristico(idRuta: $idRuta) {
          urlVideo
          duracionSegundos
        }
      }
    `;
    return this.executeQuery<any>(query, { idRuta }).pipe(map(data => data.generarReelTuristico));
  }
}
