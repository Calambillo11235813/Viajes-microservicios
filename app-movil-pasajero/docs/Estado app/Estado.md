# Estado de Implementación - App Móvil Pasajero

## 📊 Resumen de Progreso
- **Estado General:** 🚧 En Desarrollo

---

## 📱 Casos de Uso Implementados

- [x] **Login Base**
  - *Fecha de finalización:* 2026-06-01
  - *Resumen:* Configuración del Apollo Client, reparación de las importaciones de React (`@apollo/client/react`) en v4, y creación del usuario de prueba en base de datos.
  
- [x] **CU-01: Buscar rutas y horarios**
  - *Fecha de finalización:* 2026-06-01
  - *Resumen:* Pantalla `HomeScreen` con formulario de búsqueda nativa (modal personalizado para origen y destino), consultas GraphQL dinámicas (`GET_RUTAS` y `BUSCAR_VIAJES`), y pantalla de resultados `SearchResultsScreen` con el componente reutilizable `TripCard`. Todo documentado con JSDoc.

- [x] **CU-02: Seleccionar asiento y reservar**
  - *Fecha de finalización:* 2026-06-01
  - *Resumen:* Pantalla `SeatSelectionScreen` con un mapa visual de asientos (`FlatList` en modo Grid), formulario dinámico de datos del pasajero, e integración con las operaciones GraphQL `OBTENER_MAPA_ASIENTOS` y `SELECCIONAR_ASIENTO_Y_RESERVAR`. El `idUsuario` es fijo (=1) para el usuario de prueba actual.

- [x] **CU-03: Realizar pago (QR, billeteras)**
  - *Fecha de finalización:* 2026-06-01
  - *Resumen:* Pantalla `PaymentScreen` con flujo simulado de 3 pasos (selección de método → procesamiento → comprobante). Soporta pago por QR y transferencia bancaria. Ejecuta la mutación `REALIZAR_PAGO` para cada reserva y muestra comprobante con resumen de monto total, método y asientos.

 - [X] **CU-04: modificar o cancelar pasaje**
  - *Fecha de finalización:* 2026-06-02
  - *Resumen:* Pantalla `MyTripsScreen` con historial de reservas consolidado (agrupación por `idViaje` y `estadoReserva`). Muestra monto total sumado y cantidad de pasajeros por grupo. Cancelación por lotes iterando sobre todos los IDs de reserva del grupo mediante la mutación `CANCELAR_RESERVA`.

- [x] **CU-06: Buscar destinos mediante imágenes (IA)**
  - *Fecha de finalización:* 2026-06-02
  - *Resumen:* Pantalla `BuscarImagenScreen` con integración de `expo-image-picker` para galería y cámara. Simulación de análisis de IA (mock con setTimeout de 3s) que detecta destinos turísticos bolivianos aleatorios con porcentaje de confianza. Integrada en el CustomDrawer como nueva sección de navegación.

- [x] **CU-07: Generación automática de reels turísticos**
  - *Fecha de finalización:* 2026-06-03
  - *Resumen:* Pantalla `GenerarReelScreen` que permite la selección de videos desde la galería (`expo-image-picker`). Incluye simulación de procesamiento inteligente con `ActivityIndicator` nativo. Una vez finalizado el análisis, reproduce el resultado final en bucle continuo utilizando el componente nativo `<Video>` de la librería `expo-av`.

- [x] **CU-08: Traducir texto mediante video (IA)**
  - *Fecha de finalización:* 2026-06-03
  - *Resumen:* Pantalla `TraducirVisualScreen` con integración de `expo-camera` (`CameraView`). Gestiona los permisos de cámara dinámicamente y proporciona una interfaz de realidad aumentada para enfocar texto. Simula procesamiento de OCR y NLP (traducción Inglés ↔ Español) superponiendo el resultado estático directamente sobre la vista de la cámara.

- [x] **CU-13: Notificaciones push de estado del viaje**
  - *Fecha de finalización:* 2026-06-03
  - *Resumen:* Pantalla `NotificacionesScreen` con centro de alertas tipificadas (éxito/alerta/info). Integra `expo-notifications` para notificaciones nativas del SO con `setNotificationHandler` global, canal Android de alta prioridad, permisos dinámicos y automatización reactiva mediante `useEffect` + `setInterval` controlado por un `Switch` nativo. Incluye campanita con badge rojo en el header principal (tanto flotante como inline) y acceso desde el `CustomDrawer`.

- [x] **CU-09: Recomendación personalizada de rutas**
  - *Fecha de finalización:* 2026-06-07
  - *Resumen:* Tarjeta pasiva `RecommendationCard` integrada en `HomeScreen` mediante `FlatList`. Consume la query GraphQL `OBTENER_RECOMENDACION_RUTA` del core transaccional usando `useAuth` para obtener `idUsuario`, mapea `rutaRecomendadaId` contra `GET_RUTAS` para mostrar origen/destino, y navega a `SearchResults` con la ruta preseleccionada. Maneja estados `loading`, `error` y ausencia de recomendación sin interrumpir la búsqueda principal.

---

## 🛠️ Mejoras Arquitectónicas y UX/UI

- [x] **Migración de Navegación: Custom Drawer**
  - *Fecha de finalización:* 2026-06-02
  - *Resumen:* Se reemplazó completamente el `BottomTabNavigator` por un `DrawerNavigator` customizado y nativo (100% implementado con la API `Animated`), resolviendo problemas de inestabilidad (`TurboModule` crashes en Expo Go SDK 54). Esta arquitectura permite inyectar vistas anidadas condicionalmente usando un menú deslizable estético que contiene opciones para Buscar, Mis Viajes, Perfil, Buscar Imagen, Reels y Traducción.

- [x] **Campanita con Badge en Header**
  - *Fecha de finalización:* 2026-06-03
  - *Resumen:* Se añadió un ícono de campana (`notifications-outline`) con un badge rojo circular que muestra el contador de notificaciones no leídas. Se implementó tanto en el header fijo de las secciones internas como en un botón flotante para la sección Buscar (simétrico al botón hamburguesa). Al tocar, navega al centro de notificaciones.
