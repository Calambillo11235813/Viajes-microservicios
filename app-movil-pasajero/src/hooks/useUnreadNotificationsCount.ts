import { useEffect, useRef } from 'react';
import { useQuery } from '@apollo/client/react';
import * as Notifications from 'expo-notifications';
import { useAuth } from '@/context/AuthContext';
import { CONTAR_NOTIFICACIONES_NO_LEIDAS } from '@/graphql/queries/notificaciones';
import { appLog } from '@/utils/logger';
import { solicitarRefreshNotificaciones } from '@/utils/notificationsEvents';

interface ContarNoLeidasData {
  contarNotificacionesNoLeidas: number;
}

/**
 * Contador real de notificaciones no leídas con polling ligero.
 */
export function useUnreadNotificationsCount(pollInterval = 30000) {
  const { user } = useAuth();
  const idUsuario = user?.idUsuario ? Number(user.idUsuario) : undefined;

  const { data, refetch, error } = useQuery<ContarNoLeidasData>(CONTAR_NOTIFICACIONES_NO_LEIDAS, {
    variables: { idUsuario },
    skip: !idUsuario,
    pollInterval: idUsuario ? pollInterval : 0,
    fetchPolicy: 'cache-and-network',
  });

  const contadorAnterior = useRef<number | null>(null);

  useEffect(() => {
    if (data?.contarNotificacionesNoLeidas !== undefined) {
      appLog.info('Notif Móvil', 'Badge no leídas:', data.contarNotificacionesNoLeidas, '| usuario:', idUsuario);

      if (
        contadorAnterior.current !== null
        && data.contarNotificacionesNoLeidas !== contadorAnterior.current
      ) {
        appLog.info('Notif Móvil', 'Contador cambió, refrescando lista de notificaciones');
        solicitarRefreshNotificaciones();
      }
      contadorAnterior.current = data.contarNotificacionesNoLeidas;
    }
  }, [data?.contarNotificacionesNoLeidas, idUsuario]);

  useEffect(() => {
    if (error) {
      appLog.warn('Notif Móvil', 'Error al contar no leídas:', error.message);
    }
  }, [error]);

  useEffect(() => {
    const subReceived = Notifications.addNotificationReceivedListener((event) => {
      appLog.info('Notif Móvil', 'Push recibida (foreground):', {
        title: event.request.content.title,
        body: event.request.content.body,
        data: event.request.content.data,
      });
      if (idUsuario) {
        refetch().then(() => solicitarRefreshNotificaciones());
      }
    });

    const subResponse = Notifications.addNotificationResponseReceivedListener((event) => {
      appLog.info('Notif Móvil', 'Usuario abrió notificación:', {
        title: event.notification.request.content.title,
        data: event.notification.request.content.data,
      });
    });

    return () => {
      subReceived.remove();
      subResponse.remove();
    };
  }, [idUsuario, refetch]);

  return data?.contarNotificacionesNoLeidas ?? 0;
}
