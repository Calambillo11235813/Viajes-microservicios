import { gql } from '@apollo/client';

export const CONSULTAR_HISTORIAL_VIAJES = gql`
  query ConsultarHistorialViajes($idUsuario: Int!) {
    consultarHistorialViajes(idUsuario: $idUsuario) {
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
  }
`;

export const CANCELAR_RESERVA = gql`
  mutation CancelarReserva($idReserva: Int!, $idUsuario: Int!) {
    cancelarReserva(idReserva: $idReserva, idUsuario: $idUsuario) {
      idReserva
      estadoReserva
      fechaCancelacion
    }
  }
`;
