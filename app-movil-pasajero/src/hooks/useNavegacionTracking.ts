import { useCallback, useMemo } from 'react';
import { useMutation } from '@apollo/client/react';
import { useAuth } from '@/context/AuthContext';
import {
  REGISTRAR_VISUALIZACION_RUTA,
  RegistrarVisualizacionRutaData,
  RegistrarVisualizacionRutaVars,
} from '@/graphql/mutations/navegacion';

export interface RegistrarVisualizacionInput {
  idRuta?: number;
  origen?: string;
  destino?: string;
  categoriaVista?: string | null;
  ciudadOrigenVista?: string;
  ciudadDestinoVista?: string;
  idRutaVista?: number;
  tiempoPermanenciaSeg?: number;
  dispositivo?: string;
}

/**
 * Hook para tracking de navegación (CU-13) hacia DynamoDB vía GraphQL.
 * Las llamadas son fire-and-forget: errores se loguean sin afectar la UI.
 */
export function useNavegacionTracking() {
  const { user } = useAuth();
  const [registrarVisualizacionMutation] = useMutation<
    RegistrarVisualizacionRutaData,
    RegistrarVisualizacionRutaVars
  >(REGISTRAR_VISUALIZACION_RUTA);

  const idUsuario = useMemo(() => {
    if (user?.idUsuario == null) return undefined;
    const parsed = Number(user.idUsuario);
    return Number.isFinite(parsed) ? parsed : undefined;
  }, [user?.idUsuario]);

  const registrarVisualizacionRuta = useCallback(
    async (input: RegistrarVisualizacionInput) => {
      if (idUsuario == null) return;

      try {
        await registrarVisualizacionMutation({
          variables: {
            idUsuario,
            idRuta: input.idRuta,
            origen: input.origen,
            destino: input.destino,
            canal: 'APP_MOVIL',
            categoriaVista: input.categoriaVista,
            ciudadOrigenVista: input.ciudadOrigenVista,
            ciudadDestinoVista: input.ciudadDestinoVista,
            idRutaVista: input.idRutaVista,
            tiempoPermanenciaSeg: input.tiempoPermanenciaSeg,
            dispositivo: input.dispositivo,
          },
        });
      } catch (error) {
        console.log('[CU-13] No se pudo registrar visualización de ruta:', error);
      }
    },
    [idUsuario, registrarVisualizacionMutation]
  );

  return { idUsuario, registrarVisualizacionRuta };
}
