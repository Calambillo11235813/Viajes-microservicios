export interface RutaDestino {
  id: number;
  ciudadOrigen: string;
  ciudadDestino: string;
  duracionEstimadaHoras: number;
  precioBase: number;
  categoriaTuristica?: string;
}

export interface ViajeDisponible {
  idViaje: number;
  idRuta: number;
  ciudadOrigen: string;
  ciudadDestino: string;
  fechaHoraSalida: string;
  fechaHoraLlegada: string;
  duracionEstimadaHoras: number;
  precioBase: number;
  categoriaTuristica?: string;
  idBus: number;
  tipoBus: string;
  capacidadTotalAsientos: number;
  estadoViaje: string;
}

export interface VentasPorFecha {
  fecha: string;
  montoDia: number;
  cantidadPagosDia: number;
}

export interface ReporteVentas {
  montoTotal: number;
  cantidadPagos: number;
  fechaInicio: string;
  fechaFin: string;
  detallesPorFecha: VentasPorFecha[];
}

export interface Flota {
  idBus?: number;
  placa: string;
  capacidadTotalAsientos: number;
  tipoBus: string;
}
