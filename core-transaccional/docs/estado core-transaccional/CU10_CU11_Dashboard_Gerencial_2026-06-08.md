# Implementación y Ajustes del Dashboard Gerencial (CU10 / CU11)
**Fecha:** 08-Jun-2026

## 1. Resumen
Se ha integrado de forma completa el módulo de inteligencia de negocios para el rol de **Gerente**. El core-transaccional expone ahora la información de inteligencia artificial (Motor IA Python) a través de resolutores **GraphQL**, y se solucionaron errores críticos relacionados con el proceso asíncrono de inicialización en la base de datos y la autenticación por roles.

## 2. Puntos Implementados en el Core Transaccional

### 2.1. Exposición vía GraphQL
Se completó la migración a GraphQL para los endpoints gerenciales. Se establecieron los siguientes resolutores en `GerencialGraphQLController.java` o en su controlador respectivo:
- `obtenerKpisGenerales`: Estadísticas agregadas y porcentajes de conversión.
- `obtenerDistribucionClusters`: Conteo de usuarios por perfil y los centroides de sus intereses.
- `obtenerEvolucionClusters`: Carga el crecimiento de la segmentación en rangos de fechas definidos (diario, semanal, mensual).
- `obtenerReglasAsociacion`: Extrae reglas de Apriori (Cross-Selling) ordenadas dinámicamente y con paginación.
- `obtenerRutasPorCluster`: Extrae las rutas predominantes asociadas a un arquetipo de cliente.
- `obtenerMapaRutasComplementarias`: Matriz cruzada de confianza y soporte entre rutas (Route Heatmap).

### 2.2. Resolución de Bugs de Inicialización (`GerencialService.java`)
Se detectó una "condición de carrera" durante el arranque de Spring Boot (`@EventListener(ApplicationReadyEvent.class)`).
- **Problema:** Si la segmentación inicial asíncrona de usuarios (`ejecutarResegmentacionMasiva()`) se interrumpía pero guardaba el snapshot, el sistema omitía eternamente conectarse al Motor IA para descargar las reglas de asociación, dejando la tabla `regla_asociacion_cache` vacía y el frontend con valores nulos.
- **Solución:** Se desacopló la lógica de inicio. Ahora Spring verifica independientemente `dashboardKpiSnapshotRepository.count() == 0` y `reglaAsociacionCacheRepository.count() == 0`. Si falta el caché de las reglas, obligará al `MotorIaClient` a realizar el requerimiento HTTP GET a `/api/reglas-asociacion/` del modelo Python.

### 2.3. Corrección de Roles (Seed de Base de Datos)
- **Problema:** El usuario `gerente@viajes.com` estaba siendo redirigido incorrectamente a la vista de "cliente" por el interceptor del frontend.
- **Solución:** Se ajustó la asignación de roles en `DatabaseSeeder.java` del backend, garantizando que este usuario adquiera específicamente el `id_rol = 3` (GERENTE) en la base de datos relacional (PostgreSQL).

## 3. Integración con Frontend (Dashboard Bi)
El backend ahora es consumido de manera altamente optimizada por el frontend Angular:
- **Carga Diferida (Lazy Loading) y Caché:** El frontend fue refactorizado para separar la vista en 4 pestañas (Resumen, Evolución, Oportunidades, Mapa). El backend recibe solicitudes de red progresivas, y gracias al operador RxJS `shareReplay(1)`, evita la recarga o saturación del servidor `core-transaccional` ante cambios rápidos de vista del usuario.

## 4. Conclusión
El canal de comunicación `Frontend Angular` <-> `Core Java (GraphQL)` <-> `Motor IA Python` es estable, tolerante a fallos (mediante _Resilience4j_) e inicializa correctamente en frío.
