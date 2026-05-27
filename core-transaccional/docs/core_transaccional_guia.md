# Microservicio A: Core Transaccional (`core-transaccional`)

## 📌 Descripción General
El directorio `core-transaccional` contiene el código fuente del **Microservicio A**. Este componente es el motor transaccional principal de la plataforma de la Agencia de Viajes. Actúa como el único punto de entrada para las aplicaciones cliente (Frontend Web en Angular y Frontend Móvil en React Native) y orquesta la lógica de negocio central del sistema.

## 🚀 Tecnologías Principales y Arquitectura

### 1. Interfaz de Comunicación: API GraphQL
Toda la comunicación con los frontends se gestiona a través de una **API GraphQL**. 
* **Justificación Arquitectónica:** A diferencia de una API REST tradicional, GraphQL permite a las aplicaciones cliente solicitar exactamente los datos que necesitan en una sola petición. Esto evita el *over-fetching* (traer datos de más) y el *under-fetching* (hacer múltiples peticiones), lo cual es vital para optimizar el consumo de datos en la aplicación móvil de los viajeros y acelerar la carga de los dashboards administrativos.

### 2. Estrategia de Persistencia Políglota
Este microservicio centraliza la gestión de bases de datos del sistema, implementando un enfoque de *persistencia políglota*. Esto significa que utiliza diferentes motores de bases de datos según la estructura y los requerimientos de la información:

#### A. PostgreSQL (Base de Datos Relacional)
Se destina exclusivamente al almacenamiento de datos estructurados que requieren un control estricto y cumplimiento de las propiedades **ACID** (Atomicidad, Consistencia, Aislamiento, Durabilidad).
* **¿Qué datos gestiona?**
  * Cuentas de usuario y roles (Administradores, Clientes).
  * Catálogo oficial de paquetes turísticos, destinos, rutas y flotas.
  * Control de inventario de asientos (para evitar sobreventa de boletos).
  * Historial de pagos y reservas confirmadas.

#### B. Amazon DynamoDB (Base de Datos NoSQL)
Maneja cargas de trabajo masivas y datos no estructurados o flexibles que exigen alta velocidad de lectura/escritura, evitando saturar la base de datos relacional principal.
* **¿Qué datos gestiona?**
  * Seguimiento del historial de navegación, búsquedas y clics del usuario en la plataforma (datos que luego consumirá el Motor de IA para las recomendaciones).
  * Reseñas, calificaciones y comentarios post-viaje.
  * Bitácoras (logs) operativas y auditoría de eventos del sistema.

## ⚙️ Integración con el Resto del Sistema

Aunque este microservicio procesa el flujo transaccional (ej. validar disponibilidad y cobrar), **delega las tareas pesadas** para mantener su alta disponibilidad:
1. **Multimedia:** Coordina el envío de imágenes (fotografías de destinos o capturas para la IA) directamente hacia **Amazon S3**.
2. **Eventos Asíncronos:** Al procesar un pago exitoso, no genera el boleto PDF ni envía correos. En su lugar, emite un mensaje a la cola de **Redis**, delegando esta responsabilidad al **Microservicio C (`automatizacion_auditoria`)**.
3. **Inferencia IA:** Envía los paquetes de datos JSON con las preferencias de los usuarios hacia el **Microservicio B (`motor_ia`)** para obtener recomendaciones de viajes.

---
**Consideraciones de Despliegue:** Este módulo está diseñado para ser empaquetado en un contenedor **Docker** y orquestado mediante **Kubernetes**, idealmente desplegado dentro de la infraestructura de **Amazon Web Services (AWS)**.