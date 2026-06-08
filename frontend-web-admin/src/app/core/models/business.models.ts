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
  ocupacionFlota: number;
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

export interface PaginaViajes {
  contenido: ViajeDisponible[];
  totalPaginas: number;
  totalElementos: number;
  paginaActual: number;
  tieneSiguiente: boolean;
}

export interface PaginaRutas {
  contenido: RutaDestino[];
  totalPaginas: number;
  totalElementos: number;
  paginaActual: number;
  tieneSiguiente: boolean;
}

export interface AsientoEstado {
  numeroAsiento: string;
  ocupado: boolean;
}

export interface ReservaProvisional {
  idReserva: number;
  idBoleto: number;
  idViaje: number;
  idUsuario: number;
  numeroAsiento: string;
  nombrePasajero: string;
  tipoPasajero: string;
  estadoReserva: string;
  estadoBoleto: string;
  montoEstimado: number;
  fechaCreacion: string;
}

export interface PagoConfirmado {
  idPago: number;
  idReserva: number;
  idUsuario: number;
  idViaje: number;
  montoTransaccion: number;
  metodoPagoUsado: string;
  estadoPago: string;
  estadoReserva: string;
  fechaPago: string;
  eventoEmitido: boolean;
}

export interface HistorialViaje {
  idReserva: number;
  idViaje: number;
  ciudadOrigen: string;
  ciudadDestino: string;
  fechaHoraSalida: string;
  fechaCreacion: string;
  estadoReserva: string;
  montoTotalPagado: number;
  cantidadPasajeros: number;
}

export interface PaginaHistorial {
  contenido: HistorialViaje[];
  totalPaginas: number;
  totalElementos: number;
  paginaActual: number;
  tieneSiguiente: boolean;
}

export interface DestinoRecomendado {
  ciudad: string;
  porcentajeCoincidencia: number;
  descripcion: string;
}

export interface ReelTuristico {
  urlVideo: string;
  duracionSegundos: number;
}
