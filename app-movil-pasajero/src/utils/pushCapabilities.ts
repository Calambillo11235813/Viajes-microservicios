import Constants, { ExecutionEnvironment } from 'expo-constants';
import { Platform } from 'react-native';

/**
 * Entorno Expo Go (SDK 54). Push remotas del SO no están soportadas en Android.
 * Ver docs/GUIA_ENTORNO.md §3.
 */
export function esExpoGo(): boolean {
  return Constants.executionEnvironment === ExecutionEnvironment.StoreClient;
}

/** Push remotas vía Expo Push API / barra del sistema. */
export function pushRemotoSoportado(): boolean {
  if (Platform.OS === 'web') return false;
  return !esExpoGo();
}

export function mensajePushNoDisponible(): string {
  if (Platform.OS === 'web') {
    return 'Las notificaciones del sistema no están disponibles en la versión web.';
  }
  if (esExpoGo()) {
    return 'Modo Expo Go: sin push en la barra del teléfono. Usa la app compilada (npm run android) para push completas. Las alertas siguen en este panel.';
  }
  return 'Push del sistema no disponible en este dispositivo.';
}

/** Intervalo de polling del badge cuando no hay push remota (ms). */
export function intervaloPollingNotificaciones(): number {
  return pushRemotoSoportado() ? 30000 : 10000;
}
