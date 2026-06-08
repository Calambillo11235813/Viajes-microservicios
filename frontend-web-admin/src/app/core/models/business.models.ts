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

// --- BI GERENCIAL MODELS ---
export interface KpisGeneralesResponse {
  fechaSnapshot: string;
  segmentacion: {
    totalUsuarios: number;
    totalSegmentados: number;
    clusters: {
      clusterId: number;
      etiqueta: string;
      cantidad: number;
      porcentaje: number;
      ingresoPromedio: number;
    }[];
    conversionPorCluster: {
      clusterId: number;
      tasaConversion: number;
    }[];
  };
  reglasAsociacion: {
    totalReglas: number;
    reglasAltoLift: number;
    supportPromedioTop20: number;
    indiceCrossSelling: number;
  };
}

export interface ReglaAsociacionEnriquecida {
  antecedentes: { idRuta: number; descripcion: string }[];
  consecuente: { idRuta: number; descripcion: string };
  soporte: number;
  confianza: number;
  lift: number;
  interpretacion: string;
}

export interface DistribucionClustersResponse {
  fechaUltimaSegmentacion: string;
  nClusters: number;
  clusters: {
    clusterId: number;
    etiqueta: string;
    cantidadUsuarios: number;
    porcentaje: number;
    centroide: {
      totalGastado: number;
      numReservas: number;
      rutasDistintas: number;
      promedioPasajeros: number;
    };
    metricas: {
      ingresoTotal: number;
      ingresoPromedio: number;
      tasaConversion: number;
      ticketPromedio: number;
    };
  }[];
}

export interface EvolucionClustersResponse {
  fechaInicio: string;
  fechaFin: string;
  intervalo: string;
  serie: {
    fecha: string;
    clusters: {
      clusterId: number;
      cantidad: number;
      porcentaje: number;
    }[];
  }[];
}

export interface MapaRutasComplementariasResponse {
  rutas: { idRuta: number; descripcion: string }[];
  matriz: {
    rutaOrigen: number;
    rutaDestino: number;
    lift: number;
    confianza: number;
  }[];
}

export interface RutasPorClusterResponse {
  clusterId: number;
  etiqueta: string;
  rutasFrecuentes: {
    idRuta: number;
    descripcion: string;
    frecuencia: number;
    ingresoTotal: number;
  }[];
  reglasRelevantes: ReglaAsociacionEnriquecida[];
}
