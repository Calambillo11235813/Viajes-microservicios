# Base de Datos Relacional (PostgreSQL) - Guía para el Agente

## 📌 Propósito Arquitectónico
Este microservicio (`core-transaccional`) es el dueño de la base de datos transaccional del sistema. Utiliza **PostgreSQL** para garantizar la integridad, consistencia y el cumplimiento estricto de las propiedades ACID.
* **Queda estrictamente prohibido** almacenar en esta base de datos información no estructurada, logs masivos o historiales de navegación (eso corresponde a DynamoDB).

## 🗂️ Modelo de Dominio (Esquema Relacional)
El agente debe basar la creación de entidades en los siguientes dominios centrales (derivados del diagrama de clases principal):
1. **Seguridad y Accesos:** `ROL` (1) --- (M) `USUARIO`.
2. **Catálogo y Logística:** `RUTA_DESTINO`, `FLOTA` y `VIAJE_PROGRAMADO` (que asocia rutas con flotas y fechas).
3. **Paquetes Turísticos:** `PAQUETE_TURISTICO`, que se compone de alojamiento (`HOTEL`, `DETALLE_PAQUETE_HOTEL`).
4. **Flujo Transaccional Crítico:** `RESERVA` (asociada a un usuario, un viaje y un paquete), que genera múltiples `BOLETO_ASIENTO` y se consolida a través de un `PAGO`.

## 💻 Instrucciones de Implementación (Spring Data JPA)
Al generar el código para la capa de acceso a datos, el agente debe respetar las siguientes reglas de Spring Boot:
* **Entidades:** Utilizar las anotaciones de JPA (`@Entity`, `@Table`, `@Id`, `@Column`).
* **Relaciones:** Mapear explícitamente las multiplicidades del diagrama usando `@OneToMany`, `@ManyToOne`, `@OneToOne`. 
* **Rendimiento (Fetch Type):** Todas las relaciones de tipo "To-Many" y "To-One" deben configurarse obligatoriamente con `fetch = FetchType.LAZY` para evitar problemas de N+1 consultas.
* **Transaccionalidad:** Las operaciones que involucren guardar una Reserva, generar Boletos y registrar un Pago deben estar envueltas en una única transacción (`@Transactional`) en la capa de Servicio. Si el pago falla, la reserva completa debe hacer *rollback*.

## 🛠️ Gestión de Esquemas (Migraciones con Flyway)
El agente **NO DEBE** depender de la autogeneración de esquemas de Hibernate (`spring.jpa.hibernate.ddl-auto=validate` debe ser la configuración por defecto).
* Para crear o modificar la base de datos, el agente debe generar scripts SQL versionados utilizando **Flyway**.
* Los archivos `.sql` deben ubicarse en `src/main/resources/db/migration/`.
* Nomenclatura obligatoria: `V<Version>__<Descripcion>.sql` (ej. `V1__init_schema_viajes.sql`).
* Los scripts estructurales contendrán el código DDL (Data Definition Language) estándar de PostgreSQL derivado del diagrama de clases (CREATE TABLE, ALTER TABLE, ADD CONSTRAINT).
* Los datos semilla o de prueba deben ir en una migración separada posterior, por ejemplo `V2__seed_data.sql`.