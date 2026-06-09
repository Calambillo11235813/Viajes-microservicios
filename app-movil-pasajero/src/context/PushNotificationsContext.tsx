import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
  type ReactNode,
} from 'react';
import { Alert, DeviceEventEmitter, Platform } from 'react-native';
import * as Notifications from 'expo-notifications';
import Constants from 'expo-constants';
import { useMutation } from '@apollo/client/react';
import { useAuth } from '@/context/AuthContext';
import {
  REGISTRAR_DISPOSITIVO_PUSH,
  DESACTIVAR_DISPOSITIVO_PUSH,
} from '@/graphql/mutations/notificaciones';
import { appLog } from '@/utils/logger';
import { configurarCanalAndroid } from '@/notifications/setupNotifications';
import { solicitarRefreshNotificaciones } from '@/utils/notificationsEvents';
import {
  mensajePushNoDisponible,
  pushRemotoSoportado,
} from '@/utils/pushCapabilities';

interface PushNotificationsContextValue {
  pushActivo: boolean;
  permisosConcedidos: boolean;
  pushRemotoDisponible: boolean;
  avisoPush: string | null;
  togglePush: (activo: boolean) => Promise<void>;
  solicitarPermisos: () => Promise<boolean>;
}

const PushNotificationsContext = createContext<PushNotificationsContextValue | null>(null);

export function PushNotificationsProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const [pushActivo, setPushActivo] = useState(true);
  const [permisosConcedidos, setPermisosConcedidos] = useState(false);
  const tokenRef = useRef<string | null>(null);

  const [registrarDispositivo] = useMutation<any>(REGISTRAR_DISPOSITIVO_PUSH);
  const [desactivarDispositivo] = useMutation<any>(DESACTIVAR_DISPOSITIVO_PUSH);

  const pushRemotoDisponible = pushRemotoSoportado();
  const avisoPush = pushRemotoDisponible ? null : mensajePushNoDisponible();

  const solicitarPermisos = useCallback(async () => {
    if (!pushRemotoDisponible) {
      appLog.warn('Notif Móvil', avisoPush);
      return false;
    }

    await configurarCanalAndroid();

    const { status: existente } = await Notifications.getPermissionsAsync();
    const { status } = existente === 'granted'
      ? { status: existente }
      : await Notifications.requestPermissionsAsync({
          ios: { allowAlert: true, allowBadge: true, allowSound: true },
        });

    const granted = status === 'granted';
    setPermisosConcedidos(granted);
    appLog.info('Notif Móvil', 'Permisos push:', status);

    if (!granted) {
      Alert.alert(
        'Permisos denegados',
        'No podrás recibir notificaciones push. Actívalas desde los ajustes de tu dispositivo.',
      );
    }

    return granted;
  }, [pushRemotoDisponible, avisoPush]);

  const registrarTokenEnBackend = useCallback(async () => {
    if (!user?.idUsuario || !pushRemotoDisponible) return;

    try {
      const projectId =
        Constants.expoConfig?.extra?.eas?.projectId ??
        (Constants as { easConfig?: { projectId?: string } }).easConfig?.projectId;

      const tokenData = projectId
        ? await Notifications.getExpoPushTokenAsync({ projectId })
        : await Notifications.getExpoPushTokenAsync();

      tokenRef.current = tokenData.data;
      appLog.info('Notif Móvil', 'Token Expo obtenido:', tokenData.data.substring(0, 20) + '...');

      const result = await registrarDispositivo({
        variables: {
          input: {
            idUsuario: Number(user.idUsuario),
            token: tokenData.data,
            plataforma: Platform.OS.toUpperCase(),
          },
        },
      });
      appLog.info('Notif Móvil', 'Token registrado en backend para usuario', user.idUsuario, result.data);
    } catch (error) {
      const msg = error instanceof Error ? error.message : String(error);
      if (msg.includes('Expo Go') || msg.includes('development build')) {
        appLog.warn('Notif Móvil', 'Push remoto requiere development build:', msg);
        return;
      }
      appLog.warn('Notif Móvil', 'No se pudo registrar token:', error);
    }
  }, [user?.idUsuario, registrarDispositivo, pushRemotoDisponible]);

  const desactivarTokenEnBackend = useCallback(async () => {
    if (!tokenRef.current) return;
    try {
      await desactivarDispositivo({ variables: { token: tokenRef.current } });
      appLog.info('Notif Móvil', 'Token desactivado en backend');
      tokenRef.current = null;
    } catch (error) {
      appLog.warn('Notif Móvil', 'No se pudo desactivar token:', error);
    }
  }, [desactivarDispositivo]);

  const togglePush = useCallback(
    async (activo: boolean) => {
      appLog.info('Notif Móvil', 'Push activo:', activo);
      setPushActivo(activo);
      if (!activo) {
        await desactivarTokenEnBackend();
      } else if (permisosConcedidos) {
        await registrarTokenEnBackend();
      }
    },
    [desactivarTokenEnBackend, registrarTokenEnBackend, permisosConcedidos],
  );

  useEffect(() => {
    if (!pushRemotoDisponible) {
      appLog.warn('Notif Móvil', avisoPush);
      return;
    }
    solicitarPermisos();
  }, [solicitarPermisos, pushRemotoDisponible, avisoPush]);

  useEffect(() => {
    if (!pushActivo || !permisosConcedidos || !user?.idUsuario) {
      if (!pushActivo && tokenRef.current) {
        desactivarTokenEnBackend();
      }
      return;
    }
    registrarTokenEnBackend();
  }, [pushActivo, permisosConcedidos, user?.idUsuario, registrarTokenEnBackend, desactivarTokenEnBackend]);

  useEffect(() => {
    if (!pushRemotoDisponible) return;

    const abrirNotificaciones = () => {
      DeviceEventEmitter.emit('NAVIGATE_DRAWER', 'Notificaciones');
      solicitarRefreshNotificaciones();
    };

    const subResponse = Notifications.addNotificationResponseReceivedListener(() => {
      appLog.info('Notif Móvil', 'Usuario abrió notificación del sistema');
      abrirNotificaciones();
    });

    Notifications.getLastNotificationResponseAsync().then((response) => {
      if (response) {
        appLog.info('Notif Móvil', 'App abierta desde notificación');
        abrirNotificaciones();
      }
    });

    return () => subResponse.remove();
  }, [pushRemotoDisponible]);

  return (
    <PushNotificationsContext.Provider
      value={{
        pushActivo,
        permisosConcedidos,
        pushRemotoDisponible,
        avisoPush,
        togglePush,
        solicitarPermisos,
      }}
    >
      {children}
    </PushNotificationsContext.Provider>
  );
}

export function usePushNotifications(): PushNotificationsContextValue {
  const ctx = useContext(PushNotificationsContext);
  if (!ctx) {
    throw new Error('usePushNotifications debe usarse dentro de PushNotificationsProvider');
  }
  return ctx;
}
