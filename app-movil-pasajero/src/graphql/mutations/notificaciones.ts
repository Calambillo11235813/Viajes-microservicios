import { gql } from '@apollo/client';

export const MARCAR_NOTIFICACION_LEIDA = gql`
  mutation MarcarNotificacionLeida($id: ID!) {
    marcarNotificacionLeida(id: $id) {
      id
      leido
    }
  }
`;

export const MARCAR_TODAS_NOTIFICACIONES_LEIDAS = gql`
  mutation MarcarTodasNotificacionesLeidas($idUsuario: Int!) {
    marcarTodasNotificacionesLeidas(idUsuario: $idUsuario)
  }
`;

export const REGISTRAR_DISPOSITIVO_PUSH = gql`
  mutation RegistrarDispositivoPush($input: RegistrarDispositivoPushInput!) {
    registrarDispositivoPush(input: $input)
  }
`;

export const DESACTIVAR_DISPOSITIVO_PUSH = gql`
  mutation DesactivarDispositivoPush($token: String!) {
    desactivarDispositivoPush(token: $token)
  }
`;
