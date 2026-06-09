import { Platform } from 'react-native';
import * as Notifications from 'expo-notifications';

/**
 * Configuración global de notificaciones del SO.
 * Debe cargarse al iniciar la app (import en App.tsx), no solo en NotificacionesScreen.
 */
Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: true,
    shouldSetBadge: true,
    shouldShowBanner: true,
    shouldShowList: true,
  }),
});

export async function configurarCanalAndroid(): Promise<void> {
  if (Platform.OS !== 'android') return;

  await Notifications.setNotificationChannelAsync('viajes', {
    name: 'Viajes y alertas',
    importance: Notifications.AndroidImportance.MAX,
    sound: 'default',
    vibrationPattern: [0, 250, 250, 250],
    enableVibrate: true,
    showBadge: true,
  });
}
