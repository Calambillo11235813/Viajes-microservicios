import { gql } from '@apollo/client';

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
  query BuscarViajes($origen: String!, $destino: String!, $fecha: String!) {
    buscarRutasYHorariosDisponibles(origen: $origen, destino: $destino, fecha: $fecha) {
      idViaje
      idRuta
      ciudadOrigen
      ciudadDestino
      fechaHoraSalida
      fechaHoraLlegada
      duracionEstimadaHoras
      precioBase
      idBus
      tipoBus
      capacidadTotalAsientos
      estadoViaje
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
        idBus
        tipoBus
        capacidadTotalAsientos
        estadoViaje
      }
    }
  }
`;
