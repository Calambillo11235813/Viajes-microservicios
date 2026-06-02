import { gql } from '@apollo/client';

/**
 * Mutación para realizar el pago de una reserva.
 * Simula la acreditación de pago a través de QR o transferencia bancaria.
 */
export const REALIZAR_PAGO = gql`
  mutation RealizarPago(
    $idReserva: Int!
    $metodoPagoUsado: String!
    $montoTransaccion: Float!
    $acreditado: Boolean!
    $cuponDescuentoAplicado: String
  ) {
    realizarPago(
      idReserva: $idReserva
      metodoPagoUsado: $metodoPagoUsado
      montoTransaccion: $montoTransaccion
      acreditado: $acreditado
      cuponDescuentoAplicado: $cuponDescuentoAplicado
    ) {
      idPago
      idReserva
      montoTransaccion
      metodoPagoUsado
      estadoPago
      estadoReserva
      fechaPago
    }
  }
`;
