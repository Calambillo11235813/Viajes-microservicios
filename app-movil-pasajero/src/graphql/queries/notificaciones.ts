import { gql } from '@apollo/client';

export const OBTENER_NOTIFICACIONES_USUARIO = gql`
  query ObtenerNotificacionesUsuario(
    $idUsuario: Int!
    $estado: FiltroNotificacion
    $pagina: Int
    $tamanio: Int
  ) {
    obtenerNotificacionesUsuario(
      idUsuario: $idUsuario
      estado: $estado
      pagina: $pagina
      tamanio: $tamanio
    ) {
      contenido {
        id
        idUsuario
        tipo
        titulo
        mensaje
        fechaCreacion
        leido
        datosExtraJson
      }
      totalPaginas
      totalElementos
      paginaActual
      tieneSiguiente
      totalNoLeidas
    }
  }
`;

export const CONTAR_NOTIFICACIONES_NO_LEIDAS = gql`
  query ContarNotificacionesNoLeidas($idUsuario: Int!) {
    contarNotificacionesNoLeidas(idUsuario: $idUsuario)
  }
`;
