import { DeviceEventEmitter } from 'react-native';

export const NOTIFICACIONES_REFRESH_EVENT = 'NOTIFICACIONES_REFRESH';

/** Dispara un refresh de la lista de notificaciones en pantalla. */
export function solicitarRefreshNotificaciones(): void {
  DeviceEventEmitter.emit(NOTIFICACIONES_REFRESH_EVENT);
}
