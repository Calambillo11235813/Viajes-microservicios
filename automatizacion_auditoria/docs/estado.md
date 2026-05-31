# Estado de Implementación - Automatización y Auditoría (Go + Fiber)

> **⚙️ INSTRUCCIÓN CRÍTICA PARA EL AGENTE DE IA:** Actúas como el desarrollador Backend de este microservicio asíncrono y de auditoría escrito en Go. Cada vez que completes la implementación de un Caso de Uso (Módulo interno en Go, configuración del Handler de Asynq, lógica de servicio o rutas en Fiber con su respectivo manejo de errores), debes marcar la casilla correspondiente cambiando `[ ]` por `[x]` y añadir un breve resumen de los archivos modificados o creados.

## 📊 Resumen de Progreso
- **Estado General:** 🚧 En Desarrollo (Migrado de NestJS a Go + Fiber)
- **Framework Base:** Go Modules inicializado con Fiber (`github.com/gofiber/fiber/v2`)
- **Mensajería & Background:** Inicializado el ecosistema con Asynq para procesamiento en segundo plano.

---

## 📄 1. Generación de Documentos y Seguridad

- [ ] **CU05: Emitir boleto electrónico (Comprobante de Viaje)**
  - *Notas:* Pendiente crear la estructura de datos en `internal/pdf` y programar la generación visual del boleto con `gofpdf`, junto con el consumidor de Asynq para la tarea de pago exitoso.
- [ ] **CU12: Certificar boletos con Blockchain**
  - *Notas:* Pendiente implementar las funciones en `internal/blockchain` para calcular el hash SHA-256 del PDF y realizar el envío de la transacción con `go-ethereum` hacia la red compatible con EVM.

---

## 🔔 2. Orquestación de Notificaciones (Integraciones con n8n)

- [ ] **CU13: Notificaciones push de estado del viaje**
  - *Notas:* Pendiente estructurar la ruta `POST /api/v1/notifications/itinerary` en Fiber y programar el cliente HTTP nativo en `internal/webhooks` para redirigir los datos a n8n.
- [ ] **CU14: Alerta de proximidad de parada**
  - *Notas:* Pendiente estructurar la ruta `POST /api/v1/notifications/proximity` en Fiber e implementar la lógica de evaluación de coordenadas antes de disparar el webhook de n8n.
