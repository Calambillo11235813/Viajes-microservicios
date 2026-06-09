import { gql } from '@apollo/client';

export const CONSULTAR_HISTORIAL_VIAJES = gql`
  query ConsultarHistorialViajes($idUsuario: Int!, $pagina: Int, $tamanio: Int) {
    consultarHistorialViajes(idUsuario: $idUsuario, pagina: $pagina, tamanio: $tamanio) {
      contenido {
        idReserva
        idViaje
        ciudadOrigen
        ciudadDestino
        fechaHoraSalida
        fechaHoraLlegada
        fechaCreacion
        estadoReserva
        montoTotalPagado
        cantidadPasajeros
      }
      totalPaginas
      paginaActual
      tieneSiguiente
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
