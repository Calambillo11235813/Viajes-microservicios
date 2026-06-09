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
**¿Por qué?** `npx expo install` descarga la versión exacta compatible con **SDK 54**.

Dependencias nativas ya incluidas para push y development build:
- `expo-notifications` — notificaciones push
- `expo-dev-client` — development build (app propia, no Expo Go)

## 3. Dos modos de ejecutar la app (SDK 54)

| Modo | Comando de arranque | Push en barra del teléfono (Android) | Cuándo usarlo |
|------|---------------------|--------------------------------------|---------------|
| **Development build** (recomendado) | `npm start` → app compilada + Metro | **Sí** | Notificaciones push, pruebas reales |
| **Expo Go** (opcional) | `npm run start:go` | **No** (limitación SDK 53+) | Pruebas rápidas sin compilar |

### 3.1 Development build — flujo estándar del proyecto

El proyecto incluye `expo-dev-client` y `eas.json`. Tras compilar **una vez**, instalas tu APK en el celular y usas Metro como siempre.

#### Requisitos (solo la primera vez)
1. **Cuenta Expo** (gratis): [expo.dev](https://expo.dev)
2. **Android Studio** (para build local) o **EAS Build** (build en la nube)
3. Vincular el proyecto y obtener `projectId`:
   ```bash
   cd app-movil-pasajero
   npx eas login
   npx eas init
   ```
   `eas init` añade en `app.json` → `extra.eas.projectId` (necesario para el token push).

#### Compilar e instalar (elige una opción)

**Opción A — Local (Windows + Android Studio + emulador o USB):**
```bash
cd app-movil-pasajero
npx expo run:android
```
Genera e instala la app de desarrollo en el emulador o dispositivo conectado.

**Opción B — EAS en la nube (sin Android Studio):**
```bash
cd app-movil-pasajero
npm run build:dev:android
```
Descarga el APK desde el enlace que devuelve EAS e instálalo en el celular.

#### Día a día (después del primer build)
```bash
cd app-movil-pasajero
npm start
```
1. Abre la app **Viajes Pasajero** instalada (no Expo Go).
2. Escanea el QR o usa la URL de Metro si el dispositivo está en la misma red.
3. En `src/utils/config.ts` usa la **IP de tu PC** (`GRAPHQL_URL`), no `localhost`.

### 3.2 Expo Go — modo opcional (sin push remota)

```bash
npm run start:go
```
- Panel in-app de notificaciones y badge **sí funcionan** (GraphQL + polling).
- **No** aparecerá el banner del sistema en Android; es normal en SDK 54.
- La app detecta Expo Go y no llama a `getExpoPushTokenAsync` (evita el error rojo).

### Notificaciones push (SDK 54)

| Funcionalidad | Expo Go | Development build |
|---------------|---------|-------------------|
| Panel in-app (GraphQL) | Sí | Sí |
| Badge / contador | Sí (polling) | Sí + push |
| Banner del sistema (app cerrada / otra vista) | No (Android) | **Sí** |

El backend ya envía push vía Expo Push API. Con development build, al iniciar sesión se registra el token automáticamente.

## 4. Scripts npm útiles

| Script | Descripción |
|--------|-------------|
| `npm start` | Metro con **dev client** (usar con la app compilada) |
| `npm run start:go` | Metro para **Expo Go** |
| `npm run android` | Compila e instala debug en Android (`expo run:android`) |
| `npm run build:dev:android` | Build development en EAS (nube) |

## 5. Solución de Problemas Comunes (Troubleshooting)

- **Errores extraños al arrancar / "Private Properties not supported":**
  ```bash
  npx expo start -c
  ```

- **Error `Android Push notifications ... removed from Expo Go`:**
  Estás en Expo Go. Usa la **app compilada** (`expo run:android`) y `npm start`, no Expo Go.

- **Push no llega en development build:**
  1. Verifica `extra.eas.projectId` en `app.json` (ejecuta `npx eas init` si falta).
  2. Permisos de notificaciones activos en el celular.
  3. Usuario logueado (el token se registra tras login).
  4. Backend en marcha y `config.ts` con IP correcta.

- **Error de conexión GraphQL:** En celular físico usa `http://192.168.0.X:9090/graphql`, no `localhost`.

- **Metro no conecta con la app compilada:** Mismo Wi‑Fi; firewall de Windows puede bloquear el puerto 8081.

## 6. Estructura y Limpieza
- Mantén el uso de **Alias de Rutas** (`@/components`, `@/screens`).
- No dejes imports sin utilizar (usa `npx tsc --noEmit` frecuentemente para validar el tipado).
