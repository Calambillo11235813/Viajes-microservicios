# Estado de Implementación - Automatización y Auditoría

> **⚙️ INSTRUCCIÓN CRÍTICA PARA EL AGENTE DE IA:** > Actúas como el desarrollador Backend de este microservicio asíncrono (Background Worker). Cada vez que completes la implementación completa de un Caso de Uso (Módulo de NestJS, configuración del procesador de colas de BullMQ, servicio lógico con su librería correspondiente y manejo de reintentos), debes marcar la casilla correspondiente cambiando `[ ]` por `[x]` y agregar un breve resumen de los archivos modificados o creados justo debajo del caso de uso.

## 📊 Resumen de Progreso
- **Estado General:** 🚧 En Desarrollo
- **Framework Base:** NestJS inicializado con arquitectura modular (Pendiente de verificar)
- **Mensajería:** Redis configurado mediante BullMQ (Pendiente de definir tópicos/colas)

---

## 📄 1. Generación de Documentos y Seguridad

- [ ] **CU05: Emitir boleto electrónico (Comprobante de Viaje)**
  - *Notas:* Pendiente estructurar el diseño visual del boleto usando `pdfkit` y configurar el consumidor de la cola de Redis que se dispara tras el pago exitoso en el Microservicio A.
- [ ] **CU12: Certificar boletos con Blockchain**
  - *Notas:* Pendiente implementar el servicio con `ethers.js` que calcule el hash criptográfico del PDF generado y firme la transacción hacia el Smart Contract para su registro inmutable.

---

## 🔔 2. Orquestación de Notificaciones (Integraciones con n8n)

- [ ] **CU13: Notificaciones push de estado del viaje**
  - *Notas:* Pendiente configurar el módulo HTTP (`@nestjs/axios`) para despachar Webhooks (POST JSON) automáticos hacia la URL de n8n cuando se detecten cambios de itinerario.
- [ ] **CU14: Alerta de proximidad de parada**
  - *Notas:* Pendiente programar la lógica matemática asíncrona que evalúe la telemetría/coordenadas GPS entrantes y dispare el Webhook de alerta crítica a n8n si se cruza el umbral de distancia.