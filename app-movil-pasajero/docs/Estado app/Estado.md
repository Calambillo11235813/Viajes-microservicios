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
