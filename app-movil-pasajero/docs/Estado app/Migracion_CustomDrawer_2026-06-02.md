# Migración de Bottom Tabs a Custom Drawer
**Fecha:** 2026-06-02
**Hora:** 21:56

## Resumen del Cambio
Se reemplazó la navegación por pestañas inferiores (`BottomTabNavigator`) por un **menú lateral deslizable personalizado** (`CustomDrawer`) construido 100% con la API nativa `Animated` de React Native.

## Motivación
El usuario necesitaba descongestionar la pantalla principal separando el formulario de búsqueda del historial de viajes. Un Drawer Navigation es la solución ideal, pero la librería oficial `@react-navigation/drawer` requiere `react-native-reanimated` y `react-native-gesture-handler`, que causan crashes de `TurboModule` en Expo Go con SDK 54 (documentado en `Migracion_Navegacion_2026-06-02.md`).

## Solución Implementada

### Archivos Creados
1. **`src/components/CustomDrawer.tsx`**: Componente visual del panel lateral animado.
   - Usa `Animated.spring` para la animación de entrada/salida.
   - Incluye overlay semitransparente que cierra el drawer al tocarlo.
   - Muestra avatar, nombre y email del usuario autenticado.
   - Contiene los ítems de navegación con indicador visual de sección activa.
   - Botón de cerrar sesión en el pie del panel.

2. **`src/navigation/DrawerNavigator.tsx`**: Orquestador principal.
   - Maneja el estado `isDrawerOpen` y `activeSection`.
   - Renderiza condicionalmente la pantalla activa (`SearchStackNavigator`, `MyTripsScreen`, `ProfileScreen`).
   - Incluye un botón de hamburguesa flotante sobre el header del HomeScreen y un header propio para las demás secciones.

### Archivos Modificados
3. **`src/navigation/AppNavigator.tsx`**: Reemplazó `BottomTabNavigator` por `DrawerNavigator` como ruta principal autenticada.
4. **`src/screens/home/PaymentScreen.tsx`**: Simplificó la navegación post-pago (ya no intenta navegar a un tab que no existe).

### Archivos Obsoletos (pueden eliminarse)
- `src/navigation/BottomTabNavigator.tsx` (ya no se importa en ningún lugar).

## Dependencias
**Cero dependencias nuevas.** Todo se construyó con:
- `Animated` (API nativa de React Native)
- `Ionicons` de `@expo/vector-icons` (ya instalado)
- `TouchableWithoutFeedback` y `TouchableOpacity` (nativos de RN)

## Validación
- `npx tsc --noEmit`: Sin errores de tipado.
- La aplicación funciona correctamente en Expo Go sin crashes de TurboModule.
