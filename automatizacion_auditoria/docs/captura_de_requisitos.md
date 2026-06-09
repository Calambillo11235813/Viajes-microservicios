# Captura de Requisitos Funcionales - Automatización y Auditoría (Microservicio C) - Versión Go

## 📌 Propósito del Documento
Este archivo contiene exclusivamente los Casos de Uso que deben ser implementados en el **Microservicio C (Automatización y Auditoría)** bajo el nuevo ecosistema de Go y Fiber.
* **Responsabilidad:** Este componente actúa como un servidor híbrido: procesa tareas pesadas en segundo plano consumiendo eventos de Redis (`asynq`) y expone endpoints HTTP ultrarrápidos (`Fiber`) para recibir alertas operativas.
* **Límites:** El agente de IA **NO** debe implementar aquí bases de datos transaccionales, **NO** debe programar Inteligencia Artificial y **NO** debe exponer APIs GraphQL.

## ⚙️ Casos de Uso a Implementar (Procesamiento Concurrente)

A continuación, se detallan los flujos exactos que el agente debe mapear a Handlers de Asynq, Endpoints de Fiber y Webhooks de Go:

### 📄 1. Generación de Documentos
* **CU05: Emitir boleto electrónico (Comprobante de Viaje)**
  * *Descripción del Negocio:* Generar un documento digital en PDF que acredite la compra y reserva del pasajero.
  * *Instrucción Técnica:* El agente debe consumir el tipo de tarea `tasks:pago_exitoso` desde la cola de Redis usando Asynq. Utilizará la librería `gofpdf` para estructurar y dibujar el boleto con los datos del viaje recibidos en el payload JSON, guardando el resultado o preparándolo para su distribución.

  enviar el Boleto a Telegram 

  y guardarlo en S3 
  

### ⛓️ 2. Seguridad e Inmutabilidad
* **CU12: Certificar boletos con Blockchain**
  * *Descripción del Negocio:* Garantizar la autenticidad e integridad de los boletos electrónicos mediante un registro inmutable.
  * *Instrucción Técnica:* Inmediatamente después de que se genere el archivo PDF (CU05), el agente debe calcular su hash criptográfico SHA-256 usando el paquete nativo `crypto/sha256` de Go. Luego, utilizando `go-ethereum`, debe firmar y enviar una transacción hacia el Smart Contract en la red compatible con EVM para almacenar este hash como auditoría inmutable.



