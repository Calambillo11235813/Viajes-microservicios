# Guía de Entorno y Buenas Prácticas (Expo)

Este documento establece las reglas fundamentales para el desarrollo y mantenimiento del entorno de la aplicación móvil, garantizando que el proyecto no sufra problemas de compatibilidad (como los errores de `TurboModule`).

## 1. Versión del SDK
El proyecto está configurado para utilizar **Expo SDK 54**. 
> [!IMPORTANT]
> No intentes hacer un downgrade o upgrade manual modificando el `package.json`. Si en el futuro se necesita actualizar el SDK, debes usar el comando oficial de migración de Expo (`npx expo upgrade`).

## 2. Instalación de Dependencias
**Regla de oro:** NUNCA utilices `npm install <paquete>` directamente cuando instales librerías que contengan código nativo (como navegación, cámara, mapas, sensores, etc.).

Debes utilizar SIEMPRE el siguiente comando:
```bash
npx expo install <nombre_del_paquete>
```
**¿Por qué?** `npx expo install` se encarga de buscar y descargar la versión exacta de la librería que es 100% compatible con el SDK 54 y con la versión de Expo Go que los desarrolladores tienen instalada desde la Play Store/App Store.

## 3. Uso de Expo Go vs Development Builds
El proyecto actualmente está diseñado para correr fluidamente en la aplicación **Expo Go**. 
- Evita incluir librerías que requieran configuración profunda de código nativo (archivos en Java/Kotlin o Swift/Objective-C), como versiones muy experimentales de `react-native-reanimated`.
- Si en el futuro es estrictamente obligatorio usar librerías nativas incompatibles, el equipo deberá abandonar Expo Go y migrar a *Development Builds* (compilar la app con Android Studio usando `npx expo run:android`).

## 4. Solución de Problemas Comunes (Troubleshooting)
- **Errores extraños al arrancar / "Private Properties not supported":** Suele ocurrir cuando Babel se desincroniza o actualizas librerías. Para solucionarlo, detén el servidor y arráncalo limpiando la caché:
  ```bash
  npx expo start -c
  ```
- **Error de conexión a la API (GraphQL):** Recuerda que al probar la app en tu celular físico, no puedes usar `localhost` en la URI de tu cliente Apollo. Debes usar la dirección IP local de tu computadora en la red Wi-Fi (ej. `http://192.168.0.X:8080/graphql`).

## 5. Estructura y Limpieza
- Mantén el uso de **Alias de Rutas** (`@/components`, `@/screens`).
- No dejes imports sin utilizar (usa `npx tsc --noEmit` frecuentemente para validar el tipado).
