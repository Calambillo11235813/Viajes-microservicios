import { gql } from '@apollo/client';

export interface RegistrarVisualizacionRutaVars {
  idUsuario: number;
  idRuta?: number;
  origen?: string;
  destino?: string;
  canal?: string;
  categoriaVista?: string | null;
  ciudadOrigenVista?: string;
  ciudadDestinoVista?: string;
  idRutaVista?: number;
  tiempoPermanenciaSeg?: number;
  dispositivo?: string;
}

export interface RegistrarVisualizacionRutaData {
  registrarVisualizacionRuta: boolean;
}

/**
 * Registra en DynamoDB que el usuario visualizó el detalle de una ruta (CU-13).
 */
export const REGISTRAR_VISUALIZACION_RUTA = gql`
  mutation RegistrarVisualizacionRuta(
    $idUsuario: Int!
    $idRuta: Int
    $origen: String
    $destino: String
    $canal: String
    $categoriaVista: String
    $ciudadOrigenVista: String
    $ciudadDestinoVista: String
    $idRutaVista: Int
    $tiempoPermanenciaSeg: Int
    $dispositivo: String
  ) {
    registrarVisualizacionRuta(
      idUsuario: $idUsuario
      idRuta: $idRuta
      origen: $origen
      destino: $destino
      canal: $canal
      categoriaVista: $categoriaVista
      ciudadOrigenVista: $ciudadOrigenVista
      ciudadDestinoVista: $ciudadDestinoVista
      idRutaVista: $idRutaVista
      tiempoPermanenciaSeg: $tiempoPermanenciaSeg
      dispositivo: $dispositivo
    )
  }
`;
