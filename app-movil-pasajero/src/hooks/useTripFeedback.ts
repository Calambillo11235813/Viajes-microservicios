import { useCallback, useMemo, useState } from 'react';
import { Alert } from 'react-native';
import { useMutation } from '@apollo/client/react';
import { useAuth } from '@/context/AuthContext';
import {
  REGISTRAR_FEEDBACK_VIAJE,
  RegistrarFeedbackViajeData,
  RegistrarFeedbackViajeVars,
} from '@/graphql/mutations/feedback';
import { loadSentTripFeedback, markTripFeedbackSent } from '@/utils/tripFeedbackStorage';
import { appLog } from '@/utils/logger';

export interface TripFeedbackInput {
  idViaje: number | string;
  idReserva?: number | string;
  calificacion: number;
  comentario?: string;
}

function parseOptionalNumber(value: number | string | undefined): number | undefined {
  if (value === undefined || value === null || value === '') return undefined;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : undefined;
}

/**
 * Encapsula el envío de feedback post-viaje y el marcado local para no repetir la CTA.
 */
export function useTripFeedback() {
  const { user } = useAuth();
  const idUsuario = useMemo(() => {
    if (user?.idUsuario == null) return undefined;
    const parsed = Number(user.idUsuario);
    return Number.isFinite(parsed) ? parsed : undefined;
  }, [user?.idUsuario]);

  const [sentTripIds, setSentTripIds] = useState<Set<string>>(new Set());
  const [registrarFeedbackMutation, { loading }] = useMutation<
    RegistrarFeedbackViajeData,
    RegistrarFeedbackViajeVars
  >(REGISTRAR_FEEDBACK_VIAJE);

  const cargarFeedbackEnviado = useCallback(
    async (idViajes: Array<number | string>) => {
      if (!idUsuario || idViajes.length === 0) {
        setSentTripIds(prev => prev.size === 0 ? prev : new Set());
        return;
      }

      try {
        const sent = await loadSentTripFeedback(idUsuario, idViajes);
        setSentTripIds(sent);
      } catch (error) {
        appLog.warn('Feedback', 'No se pudo leer AsyncStorage:', error);
      }
    },
    [idUsuario]
  );

  const feedbackYaEnviado = useCallback(
    (idViaje: number | string) => sentTripIds.has(String(idViaje)),
    [sentTripIds]
  );

  const registrarFeedback = useCallback(
    async (input: TripFeedbackInput): Promise<boolean> => {
      if (!idUsuario) {
        Alert.alert('Sesión requerida', 'Inicia sesión para calificar el viaje.');
        return false;
      }

      const idViaje = parseOptionalNumber(input.idViaje);
      const idReserva = parseOptionalNumber(input.idReserva);
      if (!idViaje) {
        Alert.alert('Datos incompletos', 'No se pudo identificar el viaje a calificar.');
        return false;
      }
      if (input.calificacion < 1 || input.calificacion > 5) {
        Alert.alert('Calificación requerida', 'Selecciona una calificación entre 1 y 5 estrellas.');
        return false;
      }

      const comentario = input.comentario?.trim();

      try {
        const { data } = await registrarFeedbackMutation({
          variables: {
            idUsuario,
            idViaje,
            idReserva,
            calificacion: input.calificacion,
            comentario: comentario ? comentario : null,
          },
        });

        if (!data?.registrarFeedbackViaje) {
          Alert.alert('No se pudo enviar', 'Intenta enviar tu calificación nuevamente.');
          return false;
        }

        await markTripFeedbackSent(idUsuario, idViaje);
        setSentTripIds((prev) => new Set(prev).add(String(idViaje)));
        Alert.alert('Gracias', 'Tu calificación fue registrada correctamente.');
        return true;
      } catch (error) {
        const message = error instanceof Error ? error.message : 'Ocurrió un error al enviar la calificación.';
        appLog.error('Feedback', 'Error al registrar feedback:', error);
        Alert.alert('Error', message);
        return false;
      }
    },
    [idUsuario, registrarFeedbackMutation]
  );

  return {
    idUsuario,
    feedbackLoading: loading,
    cargarFeedbackEnviado,
    feedbackYaEnviado,
    registrarFeedback,
  };
}
