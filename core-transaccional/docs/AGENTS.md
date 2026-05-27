# Reglas de Entorno y Contexto para Agentes de IA (AGENTS.md)

## 📌 Contexto del Proyecto
Estás operando en el **Microservicio A (Core Transaccional)** de una plataforma de Agencia de Viajes. Este componente actúa como el motor central del sistema.
* **Responsabilidad Principal:** Gestión de cuentas de usuario, historial de pagos, catálogo de viajes y reservas confirmadas.
* **Límites Arquitectónicos:** Este servicio **NO** genera documentos PDF, **NO** interactúa con Blockchain y **NO** ejecuta modelos de IA. Delega estas tareas a otros microservicios mediante mensajes asíncronos en Redis o peticiones de red.

## 🛠️ Stack Tecnológico
* **Lenguaje:** Java (Versión 17 o superior).
* **Framework:** Spring Boot.
* **Capa de Comunicación:** API GraphQL.
* **Persistencia Relacional (ACID):** PostgreSQL (Manejo de inventario, pagos y usuarios).
* **Persistencia NoSQL (Auditoría/Logs):** Amazon DynamoDB (Historial de navegación, reseñas y bitácoras operativas).
* **Gestor de Dependencias:** Gradle.

## 🚀 Comandos de Ejecución
* Compilar el proyecto: `./gradlew build`
* Ejecutar en modo desarrollo local: `./gradlew bootRun`
* Ejecutar banco de pruebas: `./gradlew test`

---

## 📝 Estándares de Código y Documentación (Spring Boot)

### 1. Documentación (Javadoc)
Todo el código público y la lógica de negocio deben estar documentados utilizando el estándar **Javadoc**.
* Describe claramente el propósito de cada clase y método.
* Utiliza obligatoriamente las etiquetas `@param`, `@return` y `@throws` para definir contratos claros.
* Los comentarios deben explicar el *por qué* de las decisiones de negocio, no obviedades del código.

### 2. Convenciones de Arquitectura de Capas
Mantén una separación de responsabilidades estricta utilizando las anotaciones estereotipo de Spring:
* **Capa de Presentación (`@Controller`):** Utiliza anotaciones de Spring GraphQL (`@QueryMapping`, `@MutationMapping`). Solo debe recibir la petición y devolver la respuesta. Queda estrictamente prohibido incluir lógica de negocio aquí.
* **Capa de Negocio (`@Service`):** Contiene toda la lógica transaccional. Debe validar reglas (ej. evitar sobreventa de asientos) antes de persistir.
* **Capa de Datos (`@Repository`):** Acceso exclusivo a las bases de datos. Utiliza Spring Data JPA para PostgreSQL y el SDK oficial de AWS para interactuar con DynamoDB.

### 3. Buenas Prácticas de Programación
* Inyecta dependencias siempre a través del **constructor** (evita usar `@Autowired` directamente en los atributos).
* Utiliza la librería **Lombok** (`@Data`, `@Builder`, `@RequiredArgsConstructor`) para reducir el código repetitivo en Entidades y DTOs (Data Transfer Objects).
* Nomenclatura: Variables y métodos en `camelCase`. Clases e interfaces en `PascalCase`.

### 4. Transaccionalidad y Manejo de Errores
* Aplica la anotación `@Transactional` en los métodos de la capa de Servicio que involucren escritura en PostgreSQL para garantizar las propiedades ACID y revertir cambios si ocurre una excepción.
* Implementa un `@ControllerAdvice` (o su equivalente en Spring GraphQL) para capturar excepciones globales (ej. `SeatAlreadyReservedException`).
* Las respuestas de error hacia el cliente deben ser controladas y estandarizadas; bajo ninguna circunstancia se debe exponer el *stacktrace* interno de Java en la API.

---
> **⚡ INSTRUCCIÓN CRÍTICA PARA EL AGENTE:** Antes de generar código para entidades o repositorios, debes leer obligatoriamente los archivos `@docs/Base_de_datos_SQL.md` y `@docs/Base_de_datos_NoSQL.md` para respetar los esquemas de datos predefinidos.