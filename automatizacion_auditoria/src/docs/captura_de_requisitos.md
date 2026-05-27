# Captura de Requisitos Funcionales - Automatización y Auditoría (Microservicio C)

## 📌 Propósito del Documento
Este archivo contiene exclusivamente los Casos de Uso que deben ser implementados en el **Microservicio C (Automatización y Auditoría)**. 
* **Responsabilidad:** Este componente es un *background worker* basado en NestJS. Su trabajo es escuchar eventos desde una cola de Redis (BullMQ) y ejecutar procesos asíncronos pesados sin bloquear al usuario.
* **Límites:** El agente de IA **NO** debe implementar aquí bases de datos transaccionales, **NO** debe programar Inteligencia Artificial y **NO** debe exponer APIs GraphQL.

## ⚙️ Casos de Uso a Implementar (Procesamiento en Segundo Plano)

A continuación, se detallan los flujos exactos que el agente debe mapear a Consumidores de Redis (`@Processor`), Servicios de NestJS y Webhooks:

### 📄 1. Generación de Documentos
* **CU05: Emitir boleto electrónico (Comprobante de Viaje)**
  * [cite_start]*Descripción del Negocio:* Generar un documento digital en PDF que acredite la compra y reserva del pasajero[cite: 1586].
  * *Instrucción Técnica:* El agente debe consumir el evento de "Pago Exitoso" desde Redis, utilizar la librería `pdfkit` para dibujar el boleto con los datos del viaje, y prepararlo para su subida a Amazon S3 o envío por correo.

### ⛓️ 2. Seguridad e Inmutabilidad
* **CU12: Certificar boletos con Blockchain**
  * [cite_start]*Descripción del Negocio:* Garantizar la autenticidad e integridad de los boletos electrónicos mediante un registro inmutable[cite: 1602].
  * [cite_start]*Instrucción Técnica:* Una vez generado el PDF (CU05), el agente debe utilizar la librería `ethers` para generar un hash criptográfico único (SHA-256) del archivo[cite: 1602]. [cite_start]Luego, debe firmar y enviar una transacción a un Smart Contract en una red compatible con EVM para registrar dicho hash[cite: 1602].

### 🔔 3. Orquestación de Notificaciones (Integración con n8n)
Estos casos de uso no envían los mensajes directamente, sino que disparan Webhooks hacia la herramienta **n8n** mediante `@nestjs/axios`.

* **CU13: Notificaciones push de estado del viaje**
  * [cite_start]*Descripción del Negocio:* Enviar alertas automáticas y en tiempo real al usuario sobre cambios importantes en su itinerario o el estado del bus (ej. retrasos o llegada a terminal)[cite: 1604].
  * *Instrucción Técnica:* Consumir el evento de actualización operativa y disparar un Webhook POST hacia el flujo de n8n correspondiente con el payload del usuario.
* **CU14: Alerta de proximidad de parada**
  * [cite_start]*Descripción del Negocio:* Notificar al pasajero para que se prepare para bajar cuando el bus se aproxima a su punto de descenso[cite: 1606].
  * *Instrucción Técnica:* Al recibir las coordenadas del viaje, si la distancia supera el umbral, emitir un evento/webhook de alerta crítica para que n8n despache la notificación push.