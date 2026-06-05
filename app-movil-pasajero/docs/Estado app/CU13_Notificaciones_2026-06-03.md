# Implementación CU-13: Centro de Notificaciones Push
**Fecha:** 2026-06-03

---

## 📋 Resumen General

Se implementó un Centro de Notificaciones completo para el caso de uso **CU-13: Notificaciones push de estado del viaje**. Incluye una pantalla de gestión de alertas, integración con notificaciones nativas del sistema operativo, y un indicador visual (campanita con badge) accesible desde cualquier pantalla de la app.

---

## 📦 Dependencias Instaladas

| Paquete | Comando | Propósito |
|---|---|---|
| `expo-notifications` | `npx expo install expo-notifications` | Notificaciones locales y push nativas del SO |

---

## 📁 Archivos Creados

### `src/screens/home/NotificacionesScreen.tsx`
Pantalla principal del Centro de Notificaciones.

**Características:**
- **Datos Mock**: Arreglo de 3 notificaciones de prueba tipificadas (`exito`, `alerta`, `info`), cada una con `id`, `tipo`, `titulo`, `mensaje`, `tiempo` y `leido`.
- **FlatList con tarjetas coloreadas**: Borde lateral verde (éxito), rojo (alerta) o azul (info). Fondo tenue cuando no han sido leídas. Punto de color (`unreadDot`) indicador.
- **Switch nativo** (`<Switch>`) para "Recibir notificaciones push" con ícono dinámico (`notifications` / `notifications-off`).
- **Botón "Marcar todo como leído"**: Aparece condicionalmente solo si hay notificaciones sin leer.
- **Eliminar individual**: Cada tarjeta tiene un botón `✕` (close-circle-outline).
- **Estado vacío**: Cuando no quedan notificaciones, muestra ícono de campana tachada y mensaje amigable.

**Integración con `expo-notifications`:**
- **`setNotificationHandler`** (nivel de módulo): Configura que las alertas se muestren con sonido, banner y badge incluso con la app en primer plano.
- **`solicitarPermisos`**: Función asíncrona ejecutada automáticamente al montar con `useEffect`. En Android crea un canal `"viajes"` con `AndroidImportance.HIGH`. Solicita permisos con `requestPermissionsAsync()`.
- **Automatización reactiva** (`useEffect` + `setInterval`):
  - Dependencias: `[pushActivo, permisosConcedidos]`
  - Si `pushActivo === true` y permisos concedidos → Inicia un `setInterval` cada **5000ms** que programa una notificación local nativa con `scheduleNotificationAsync`.
  - Si `pushActivo === false` → Ejecuta `clearInterval` inmediatamente.
  - Función de cleanup retornada para evitar memory leaks al desmontar.
  - Referencia al intervalo almacenada en `useRef` para acceso estable.

---

## 📁 Archivos Modificados

### `src/navigation/DrawerNavigator.tsx`

**Cambios:**
1. **Import** de `NotificacionesScreen`.
2. **Constante `NOTIF_BADGE_COUNT = 3`** para simular el contador de notificaciones no leídas.
3. **Caso `'Notificaciones'`** en `getHeaderTitle()` y `renderActiveScreen()`.
4. **Campanita en header fijo** (secciones MisViajes, Perfil, etc.): Reemplazó el `headerSpacer` por un `TouchableOpacity` con ícono `notifications-outline` y badge rojo circular con el número `3`. Se oculta el badge cuando la sección activa es Notificaciones.
5. **Campanita flotante** en la sección Buscar: Botón circular absoluto en la esquina superior derecha (simétrico al botón hamburguesa de la izquierda), con el mismo badge rojo.

**Estilos añadidos:**
- `bellButton`, `bellBadge`, `bellBadgeText` (header fijo)
- `floatingBell`, `floatingBellBadge`, `floatingBellBadgeText` (botón flotante)

### `src/components/CustomDrawer.tsx`

**Cambios:**
- Se añadió el ítem `{ key: 'Notificaciones', label: 'Notificaciones', icon: 'notifications-outline', iconFocused: 'notifications' }` al arreglo `DRAWER_ITEMS`.

---

## 🔧 Detalles Técnicos Clave

### Handler Global de Notificaciones
```typescript
Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: true,
    shouldSetBadge: true,
    shouldShowBanner: true,
    shouldShowList: true,
  }),
});
```
> Se declara a nivel de módulo (fuera del componente) para garantizar que se registre una sola vez. Sin esto, las notificaciones no aparecerían cuando la app está en primer plano.

### Canal de Android
```typescript
await Notifications.setNotificationChannelAsync('viajes', {
  name: 'Viajes',
  importance: Notifications.AndroidImportance.HIGH,
  sound: 'default',
  vibrationPattern: [0, 250, 250, 250],
});
```
> Obligatorio desde Android 8 (API 26). Sin un canal, las notificaciones son silenciadas por el SO.

### Trigger de Notificación
```typescript
trigger: {
  type: Notifications.SchedulableTriggerInputTypes.TIME_INTERVAL,
  seconds: 1,
}
```
> Se usa `TIME_INTERVAL` con 1 segundo de delay (la notificación aparece ~1s después de ser programada por el `setInterval` que corre cada 5s).

### Patrón de Cleanup con useRef
```typescript
const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

useEffect(() => {
  if (intervalRef.current) clearInterval(intervalRef.current);
  
  if (pushActivo && permisosConcedidos) {
    intervalRef.current = setInterval(async () => { ... }, 5000);
  }

  return () => {
    if (intervalRef.current) clearInterval(intervalRef.current);
  };
}, [pushActivo, permisosConcedidos]);
```
> Se usa `useRef` en lugar de una variable local para mantener la referencia estable entre renders. La función de cleanup se ejecuta tanto al cambiar las dependencias como al desmontar el componente.

---

## ✅ Compatibilidad

- **Expo Go**: ✅ Totalmente compatible (SDK 54)
- **iOS**: ✅ (requiere conceder permisos en el diálogo nativo)
- **Android**: ✅ (canal de alta prioridad configurado, compatible con Android 8+)
- **Dependencias nativas adicionales**: Ninguna (`expo-notifications` es un paquete oficial de Expo)
