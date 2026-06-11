import { gql } from '@apollo/client';

export interface RegistrarFeedbackViajeVars {
  idUsuario: number;
  idViaje: number;
  idReserva?: number;
  calificacion: number;
  comentario?: string | null;
}

export interface RegistrarFeedbackViajeData {
  registrarFeedbackViaje: boolean;
}

/**
 * Registra en DynamoDB la calificación y comentario post-viaje del pasajero.
 */
export const REGISTRAR_FEEDBACK_VIAJE = gql`
  mutation RegistrarFeedbackViaje(
    $idUsuario: Int!
    $idViaje: Int!
    $idReserva: Int
    $calificacion: Int!
    $comentario: String
  ) {
    registrarFeedbackViaje(
      idUsuario: $idUsuario
      idViaje: $idViaje
      idReserva: $idReserva
      calificacion: $calificacion
      comentario: $comentario
    )
  }
`;
