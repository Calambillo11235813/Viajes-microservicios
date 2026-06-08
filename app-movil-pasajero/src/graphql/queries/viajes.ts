import { gql } from '@apollo/client';

/** Resumen de ruta comercial para catálogo y mapeo de recomendaciones. */
export interface RutaResumen {
  id: string;
  ciudadOrigen: string;
  ciudadDestino: string;
}

/** Ruta candidata devuelta por el motor IA con su probabilidad. */
export interface TopRutaRecomendada {
  idRuta: number;
  probabilidad: number | null;
}

/** Respuesta de recomendación personalizada (CU-09). */
export interface RecomendacionRuta {
  rutaRecomendadaId: number | null;
  perfilUsuario: string | null;
  categoriaPreferida: string | null;
  topRutas: TopRutaRecomendada[];
  advertencia: string | null;
}

export interface GetRutasData {
  listarRutas: RutaResumen[];
}

export interface ObtenerRecomendacionRutaVars {
  idUsuario: number;
  presupuesto?: number | null;
}

export interface ObtenerRecomendacionRutaData {
  obtenerRecomendacionRuta: RecomendacionRuta;
}

/** Viaje programado disponible devuelto por CU-01. */
export interface ViajeDisponible {
  idViaje: string;
  idRuta: string;
  ciudadOrigen: string;
  ciudadDestino: string;
  fechaHoraSalida: string;
  fechaHoraLlegada: string;
  duracionEstimadaHoras: number;
  precioBase: number;
  categoriaTuristica?: string | null;
  idBus: string;
  tipoBus: string;
  capacidadTotalAsientos: number;
  estadoViaje: string;
}

export interface BuscarViajesVars {
  origen: string;
  destino: string;
  fecha: string;
  idUsuario?: number;
}

export interface BuscarViajesData {
  buscarRutasYHorariosDisponibles: ViajeDisponible[];
}

/**
 * Consulta para obtener todas las rutas disponibles (Origen y Destino).
 */
export const GET_RUTAS = gql`
  query GetRutas {
    listarRutas {
      id
      ciudadOrigen
      ciudadDestino
    }
  }
`;

/**
 * Consulta para buscar viajes programados disponibles basándose en el origen, destino y fecha.
 */
export const BUSCAR_VIAJES = gql`
  query BuscarViajes($origen: String!, $destino: String!, $fecha: String!, $idUsuario: Int) {
    buscarRutasYHorariosDisponibles(
      origen: $origen
      destino: $destino
      fecha: $fecha
      idUsuario: $idUsuario
    ) {
      idViaje
      idRuta
      ciudadOrigen
      ciudadDestino
      fechaHoraSalida
      fechaHoraLlegada
      duracionEstimadaHoras
      precioBase
      categoriaTuristica
      idBus
      tipoBus
      capacidadTotalAsientos
      estadoViaje
    }
  }
`;

/**
 * Consulta para obtener la recomendación personalizada de ruta (CU-09).
 * Si no se envía presupuesto, el core usa el gasto histórico del usuario.
 */
export const OBTENER_RECOMENDACION_RUTA = gql`
  query ObtenerRecomendacionRuta($idUsuario: Int!, $presupuesto: Float) {
    obtenerRecomendacionRuta(idUsuario: $idUsuario, presupuesto: $presupuesto) {
      rutaRecomendadaId
      perfilUsuario
      categoriaPreferida
      advertencia
      topRutas {
        idRuta
        probabilidad
      }
    }
  }
`;

export const LISTAR_ORIGENES_DESTINO_TURISTICO = gql`
  query ListarOrigenesHaciaDestinoTuristico($nombreDestino: String!) {
    listarOrigenesHaciaDestinoTuristico(nombreDestino: $nombreDestino) {
      departamento
      origenes
    }
  }
`;

export const BUSCAR_VIAJES_DESTINO_TURISTICO = gql`
  query BuscarViajesPorDestinoTuristico($nombreDestino: String!, $page: Int, $size: Int, $fecha: String, $origen: String) {
    buscarViajesPorDestinoTuristico(nombreDestino: $nombreDestino, page: $page, size: $size, fecha: $fecha, origen: $origen) {
      destino {
        id
        nombreTuristico
        departamento
        descripcion
      }
      viajesDisponibles {
        idViaje
        idRuta
        ciudadOrigen
        ciudadDestino
        fechaHoraSalida
        fechaHoraLlegada
        duracionEstimadaHoras
        precioBase
        categoriaTuristica
        idBus
        tipoBus
        capacidadTotalAsientos
        estadoViaje
      }
    }
  }
`;
