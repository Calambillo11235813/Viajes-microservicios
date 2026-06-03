# Migración de Navegación: Drawer a Bottom Tabs

**Fecha:** 2026-06-02
**Hora:** 17:07

## Resumen del Problema
Se detectó un crash constante en la aplicación (`Exception in HostFunction: TurboModule method "installTurboModule" called with 1 arguments`) al ejecutarla desde Expo Go en un dispositivo físico. Tras investigar, se concluyó que el error provenía de un desajuste entre las versiones nativas precompiladas de la aplicación Expo Go en el dispositivo del usuario y el código JavaScript del proyecto correspondiente a la librería `react-native-reanimated` (la cual es requerida por defecto en `@react-navigation/drawer`).

## Solución Implementada
Para solucionar el problema sin requerir una compilación nativa personalizada ni hacer un downgrade de todo el SDK de Expo (actualmente en SDK 54), se optó por eliminar el origen del conflicto y simplificar la navegación principal de la app:

1. **Eliminación del Drawer:** 
   - Se borró el archivo `DrawerNavigator.tsx`.
   - Se desinstalaron las librerías dependientes de código nativo problemático: `@react-navigation/drawer`, `react-native-reanimated` y `react-native-gesture-handler`.
   - Se removió el plugin de Reanimated del archivo `babel.config.js`.

2. **Implementación de Bottom Tabs:**
   - Se instalaron los paquetes `@react-navigation/bottom-tabs` y `@expo/vector-icons` utilizando `npx expo install` para asegurar la máxima compatibilidad con el SDK 54.
   - Se creó un nuevo archivo `BottomTabNavigator.tsx` conteniendo las pantallas de la aplicación (`Buscar`, `MisViajes`, `Perfil`) en un formato de pestañas inferiores, asignando iconos representativos.
   - Se trasladó la funcionalidad de "Cerrar Sesión" al header de la pantalla "Perfil".
   - Se modificó `AppNavigator.tsx` para cargar las pestañas como la ruta principal autenticada (`Main`).

3. **Validación:**
   - Se limpió la caché del empaquetador Metro (`npx expo start -c`).
   - Se ejecutó `npx tsc --noEmit` confirmando un tipado estricto sin errores.
   - La aplicación volvió a funcionar perfectamente en Expo Go sin excepciones de TurboModules.

## Estado Actual
La navegación principal de la aplicación móvil de pasajeros se realiza de manera robusta y 100% estable mediante pestañas inferiores, mitigando por completo las limitaciones de entorno presentadas por la versión de Expo Go del usuario.
