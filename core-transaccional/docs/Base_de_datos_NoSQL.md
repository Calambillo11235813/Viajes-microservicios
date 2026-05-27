# Base de Datos NoSQL (Amazon DynamoDB) - Guía para el Agente

## 📌 Propósito Arquitectónico
Dentro del enfoque de persistencia políglota del sistema, Amazon DynamoDB se destina exclusivamente a la gestión de datos no estructurados, flexibles y que exigen una altísima velocidad de lectura y escritura. 
* Queda **estrictamente prohibido** utilizar esta base de datos para almacenar información transaccional ACID, como pagos, inventario de asientos o cuentas de usuario centrales (esa responsabilidad corresponde a PostgreSQL).

## 🗂️ Dominios de Datos Gestionados
Los únicos dominios de información que este microservicio debe persistir en DynamoDB son:
* **Historial de Navegación (Tracking):** Registro masivo de búsquedas, clics y visualizaciones de los clientes en la app móvil. Estos datos alimentarán posteriormente al Motor de IA para las recomendaciones.
* **Feedback de Viajeros:** Reseñas, calificaciones de estrellas y comentarios escritos al finalizar un viaje.
* **Bitácoras Operativas (Logs):** Registro inmutable de eventos críticos del sistema, auditoría de acciones de los administradores y logs de errores.

## ⚙️ Patrón de Diseño: Single-Table Design
DynamoDB no debe modelarse como una base de datos relacional normal. El agente debe aplicar las siguientes reglas al generar el código:
* Implementa el patrón **Single-Table Design** (Diseño de Tabla Única) para agrupar entidades relacionadas.
* Utiliza identificadores genéricos para la Clave de Partición (`PK`) y la Clave de Ordenación (`SK`). 
* Ejemplo de registro de una reseña: `PK` = `TRIP#1045`, `SK` = `REVIEW#USER#992`.
* Diseña los accesos utilizando operaciones `Query`; evita a toda costa generar código que ejecute operaciones `Scan` completas, ya que degradan el rendimiento y aumentan el costo.

## 💻 Instrucciones de Implementación en Spring Boot
Al generar los repositorios y servicios para DynamoDB en Java, el agente debe respetar lo siguiente:
* Utilizar el **AWS SDK for Java 2.x** oficial.
* Emplear el `DynamoDbEnhancedClient` para realizar el mapeo objeto-registro de forma limpia.
* Decorar las entidades (DTOs/Modelos) utilizando las anotaciones estándar de AWS (`@DynamoDbBean`, `@DynamoDbPartitionKey`, `@DynamoDbSortKey`).
* Capturar los errores de conexión de AWS mediante el manejo global de excepciones de Spring Boot (`@ControllerAdvice`).

## 🐳 Gestión de Entornos (Local vs. Producción)
El ciclo de vida y la conexión a la base de datos cambian drásticamente dependiendo del entorno en el que se ejecute el microservicio. El agente debe respetar las siguientes configuraciones:

### Entorno de Desarrollo (Local)
* **Infraestructura:** Se utilizará un contenedor de Docker local ejecutando la imagen oficial `amazon/dynamodb-local`.
* **Comportamiento del Código:** * El cliente de AWS SDK en Spring Boot debe configurarse para apuntar al `endpointOverride` local (generalmente `http://localhost:8000`).
  * Se permite (y recomienda) que el código valide si las tablas existen y, de no ser así, ejecute los comandos de creación de tablas al arrancar la aplicación (usando `createTable()` del SDK).

### Entorno de Producción (AWS)
* **Infraestructura:** Se utilizará el servicio administrado real de Amazon DynamoDB alojado en la nube de AWS.
* **Comportamiento del Código:** * El cliente de AWS SDK no debe usar un endpoint personalizado; debe resolver automáticamente la región a través de las variables de entorno o roles IAM de Kubernetes.
  * Queda **estrictamente prohibido** que el código intente crear, modificar o eliminar tablas en este entorno. Las tablas en producción se crearán previamente mediante herramientas de Infraestructura como Código (IaC) como Terraform o scripts de despliegue independientes.