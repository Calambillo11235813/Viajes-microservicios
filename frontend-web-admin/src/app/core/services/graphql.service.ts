import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, throwError, catchError } from 'rxjs';
import { RutaDestino, ViajeDisponible, ReporteVentas, Flota } from '../models/business.models';

@Injectable({
  providedIn: 'root'
})
export class GraphqlService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:9090/graphql';

  private executeQuery<T>(query: string, variables: any = {}): Observable<T> {
    return this.http.post<any>(this.API_URL, { query, variables }).pipe(
      map(response => {
        if (response.errors) {
          throw new Error(response.errors[0].message);
        }
        return response.data;
      }),
      catchError(error => throwError(() => new Error(error.message || 'Error GraphQL')))
    );
  }

  // --- RUTAS ---
  listarRutas(): Observable<RutaDestino[]> {
    const query = `
      query {
        listarRutas {
          id
          ciudadOrigen
          ciudadDestino
          duracionEstimadaHoras
          precioBase
          categoriaTuristica
        }
      }
    `;
    return this.executeQuery<any>(query).pipe(map(data => data.listarRutas));
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

  // --- VIAJES ---
  listarViajesPorRuta(idRuta: number): Observable<ViajeDisponible[]> {
    const query = `
      query ListarViajes($idRuta: Int!) {
        listarViajesPorRuta(idRuta: $idRuta) {
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
      }
    `;
    return this.executeQuery<any>(query, { idRuta }).pipe(map(data => data.listarViajesPorRuta));
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
}
