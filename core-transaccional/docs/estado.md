# Estado de Implementación - Core Transaccional

> **⚙️ INSTRUCCIÓN CRÍTICA PARA EL AGENTE DE IA:** > Actúas como el desarrollador de este microservicio. Cada vez que completes la implementación full-stack (Entidad JPA, Repositorio, Servicio y Resolutor GraphQL) de un Caso de Uso, debes marcar la casilla correspondiente cambiando `[ ]` por `[x]` y agregar un breve resumen de los archivos modificados debajo del caso de uso.

## 📊 Resumen de Progreso
- **Estado General:** 🚧 En Desarrollo
- **Framework Base:** Spring Boot configurado con dependencias base de JPA, validación, Web MVC y GraphQL.
- **Base de Datos:** PostgreSQL con Flyway configurado, migración inicial `V1__init_schema_viajes.sql` y datos semilla en `V2__seed_data.sql`.

---

## 🛒 Flujo del Viajero (Cliente)

- [ ] **CU-01: Buscar rutas y horarios disponibles**
  - *Notas:* Pendiente de implementar filtrado por origen, destino y fecha.
- [ ] **CU-02: Seleccionar asiento y reservar**
  - *Notas:* Pendiente de implementar el bloqueo concurrente para evitar sobreventa.
- [ ] **CU-03: Realizar pago (QR, transferencias)**
  - *Notas:* Pendiente de emitir el evento hacia Redis tras pago exitoso.
- [ ] **CU-05: Gestionar perfil de usuario**
  - *Notas:* Pendiente CRUD de actualización de datos.
- [ ] **CU-06: Cancelar reserva**
  - *Notas:* Pendiente lógica de liberación de asientos.
- [ ] **CU-07: Consultar historial de viajes**
  - *Notas:* Pendiente consulta filtrada por ID de usuario logueado.

---

## ⚙️ Flujo Administrativo y Operativo

- [ ] **CU-08: Gestionar Usuarios**
  - *Notas:* Pendiente CRUD y asignación de roles de sistema.
- [ ] **CU-09: Gestionar Rutas y Horarios**
  - *Notas:* Pendiente validación de solapamiento de horarios.
- [ ] **CU-10: Generar Reportes de Ventas**
  - *Notas:* Pendiente queries de agregación (suma total por fechas).
- [ ] **CU-11: Gestionar Flota de Buses**
  - *Notas:* Pendiente CRUD de vehículos y asignación de capacidades.