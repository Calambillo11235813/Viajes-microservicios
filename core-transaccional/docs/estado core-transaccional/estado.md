# Estado de Implementación - Core Transaccional

> **⚙️ INSTRUCCIÓN CRÍTICA PARA EL AGENTE DE IA:** > Actúas como el desarrollador de este microservicio. Cada vez que completes la implementación full-stack (Entidad JPA, Repositorio, Servicio y Resolutor GraphQL) de un Caso de Uso, debes marcar la casilla correspondiente cambiando `[ ]` por `[x]` y agregar un breve resumen de los archivos modificados debajo del caso de uso.

## 📊 Resumen de Progreso
- **Estado General:** 🚧 En Desarrollo
- **Framework Base:** Spring Boot configurado con dependencias base de JPA, validación, Web MVC y GraphQL.
- **Base de Datos:** PostgreSQL con Flyway configurado, migración inicial `V1__init_schema_viajes.sql` y datos semilla en `V2__seed_data.sql`.

---

## 🛒 Flujo del Viajero (Cliente)

- [x] **CU-01: Buscar rutas y horarios disponibles**
  - *Resumen:* Implementado con entidades JPA `RutaDestino`, `Flota` y `ViajeProgramado`; repositorio `ViajeProgramadoRepository`; servicio `ViajeConsultaService`; DTO `ViajeDisponibleResponse`; resolver GraphQL `ViajeQueryResolver`; y schema `graphql/schema.graphqls`.
  - *Consulta GraphQL:* `buscarRutasYHorariosDisponibles(origen, destino, fecha)`.
  - *Integración Móvil:* Completada el 2026-06-01.
- [x] **CU-02: Seleccionar asientos y reservar**
  - *Resumen:* Implementado con entidades JPA `Usuario`, `Reserva` y `BoletoAsiento`; repositorios `UsuarioRepository`, `ReservaRepository` y `BoletoAsientoRepository`; bloqueo pesimista en `ViajeProgramadoRepository`; servicio `ReservaAsientoService`; resolver GraphQL `ReservaAsientoResolver`; y ampliación de `graphql/schema.graphqls`.
  - *Consulta GraphQL:* `obtenerMapaAsientos(idViaje)`.
  - *Mutación GraphQL:* `seleccionarAsientoYReservar(idUsuario, idViaje, numeroAsiento, nombrePasajero, tipoPasajero)`.
  - *Integración Móvil:* Completada el 2026-06-01.
- [x] **CU-03: Realizar pago (QR, transferencias)**
  - *Resumen:* Implementado con entidad JPA `Pago`; repositorio `PagoRepository`; bloqueo pesimista de reserva en `ReservaRepository`; emisión de boletos existentes desde `BoletoAsientoRepository`; servicio transaccional `PagoService`; publicador Redis `PagoConfirmadoEventPublisher`; resolver GraphQL `PagoResolver`; dependencia Redis; y ampliación de `graphql/schema.graphqls`.
  - *Mutación GraphQL:* `realizarPago(idReserva, metodoPagoUsado, montoTransaccion, acreditado, cuponDescuentoAplicado)`.
  - *Evento Redis:* publica `PAGO_CONFIRMADO` en el canal `pagos.confirmados` después del commit de la transacción.
- [x] **CU-05: Gestionar perfil de usuario**
  - *Resumen:* Implementado con DTO `UsuarioPerfilResponse`; métodos de búsqueda por email y CI en `UsuarioRepository`; servicio transaccional `UsuarioPerfilService` con validación de unicidad de email y CI/pasaporte; resolver GraphQL `UsuarioPerfilResolver`; y ampliación de `graphql/schema.graphqls`.
  - *Consulta GraphQL:* `obtenerPerfilUsuario(idUsuario)`.
  - *Mutación GraphQL:* `actualizarPerfilUsuario(idUsuario, nombreCompleto, email, telefono, ciPasaporte)`.
- [x] **CU-06: Cancelar reserva**
  - *Resumen:* Implementado con DTO `ReservaCanceladaResponse`; query con bloqueo pesimista `buscarPorIdConBloqueoParaCancelacion` en `ReservaRepository`; método `findByReservaId` en `PagoRepository`; servicio transaccional `CancelacionReservaService` con validación de propiedad del usuario, estados cancelables (PENDIENTE/CONFIRMADA), plazo mínimo de 24h antes de salida, anulación de boletos, reembolso de pago y liberación de asientos; resolver GraphQL `CancelacionReservaResolver`; y ampliación de `graphql/schema.graphqls`.
  - *Mutación GraphQL:* `cancelarReserva(idReserva, idUsuario)`.
- [x] **CU-07: Consultar historial de viajes**
  - *Resumen:* Implementado con DTO `HistorialViajeResponse`; método optimizado `buscarHistorialPorUsuario` en `ReservaRepository`; servicio `HistorialViajesService`; resolutor GraphQL `HistorialViajesResolver`; y ampliación de `graphql/schema.graphqls`.
  - *Consulta GraphQL:* `consultarHistorialViajes(idUsuario)`.


- [x] **CU-09: Recomendación personalizada de rutas**
  - *Resumen:* Se implementó la orquestación entre PostgreSQL y el motor IA (Django) para sugerir rutas según el perfil del pasajero. El servicio calcula el perfil (`Económico`, `Estándar`, `Premium`) con percentiles globales de gasto, obtiene la categoría turística preferida del historial de reservas y llama al endpoint REST del modelo Random Forest. La respuesta incluye la ruta recomendada y el top 3 con probabilidades; si el motor IA falla, devuelve ruta por defecto con advertencia.
  - *Archivos:* `RecomendacionService`, `RecomendacionResolver`, DTOs en `recomendaciones/dto/`, `MotorIaClientConfig`, consultas agregadas en `ReservaRepository`, tipos y query en `schema.graphqls`, propiedades `motor-ia.base-url` y `motor-ia.recomendacion-path` en `application.properties`.
  - *Consulta GraphQL:* `obtenerRecomendacionRuta(idUsuario, presupuesto)`.
  - *Integración Motor IA:* `POST http://localhost:8000/api/recomendar-ruta/api/v1/recomendar-ruta/`.
  - *Detalle técnico:* Ver `CU09_Recomendacion_Personalizada_2026-06-07.md`.

---

## ⚙️ Flujo Administrativo y Operativo

- [x] **CU-08: Gestionar Usuarios**
  - *Notas:* Implementado CRUD de usuarios, entidad `Rol`, servicio `UsuarioAdminService` y controlador GraphQL.
- [x] **CU-10: Gestionar Rutas y Horarios**
  - *Notas:* Implementada lógica de rutas, validación estricta de solapamiento de horarios de viajes en BD, y mutaciones administrativas.
- [x] **CU-11: Generar Reportes de Ventas**
  - *Resumen:* Implementado con DTOs `ReporteVentasResponse` y `VentasPorFechaResponse`; consulta nativa de agregación por fecha `findVentasAgrupadasPorFecha` en `PagoRepository`; servicio `ReportesVentasService` para validar y sumar totales; resolutor GraphQL `ReportesVentasResolver`; y esquema actualizado en `schema.graphqls`.
  - *Consulta GraphQL:* `generarReporteVentas(fechaInicio, fechaFin)`.
- [ ] **CU-12: Gestionar Flota de Buses**
  - *Notas:* Pendiente CRUD de vehículos y asignación de capacidades.