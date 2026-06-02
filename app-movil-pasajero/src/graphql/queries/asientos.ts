import { gql } from '@apollo/client';

/**
 * Consulta para obtener el estado de todos los asientos de un viaje.
 */
export const OBTENER_MAPA_ASIENTOS = gql`
  query ObtenerMapaAsientos($idViaje: Int!) {
    obtenerMapaAsientos(idViaje: $idViaje) {
      numeroAsiento
      ocupado
    }
  }
`;

/**
 * Mutación para seleccionar un asiento y generar una reserva provisional.
 */
export const SELECCIONAR_ASIENTO_Y_RESERVAR = gql`
  mutation SeleccionarAsientoYReservar(
    $idUsuario: Int!
    $idViaje: Int!
    $numeroAsiento: String!
    $nombrePasajero: String!
    $tipoPasajero: String
  ) {
    seleccionarAsientoYReservar(
      idUsuario: $idUsuario
      idViaje: $idViaje
      numeroAsiento: $numeroAsiento
      nombrePasajero: $nombrePasajero
      tipoPasajero: $tipoPasajero
    ) {
      idReserva
      idBoleto
      numeroAsiento
      estadoReserva
      montoEstimado
    }
  }
`;
