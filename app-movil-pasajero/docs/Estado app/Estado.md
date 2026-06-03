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
