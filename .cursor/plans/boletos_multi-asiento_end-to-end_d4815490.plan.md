---
name: Boletos multi-asiento end-to-end
overview: Conectar core-transaccional (Java) con automatizacion_auditoria (Go) para que, al confirmar un pago, se emita un boleto PDF por cada asiento de la reserva. core-transaccional publicará un evento enriquecido por Redis Pub/Sub y un bridge en Go lo convertirá en N tareas Asynq.
todos:
  - id: java-publisher
    content: Crear BoletosEmitirEventPublisher.java que arma el JSON con datos de viaje/usuario y array de boletos
    status: completed
  - id: java-service
    content: "Modificar PagoService: construir el payload dentro de la transaccion y publicarlo en afterCommit"
    status: completed
  - id: java-config
    content: Anadir app.events.boletos-emitir-channel=boletos.emitir en application.properties
    status: completed
  - id: go-config
    content: Anadir BoletosEmitirChannel y constructor RedisClient() (go-redis con TLS/auth) en config/config.go
    status: completed
  - id: go-subscriber
    content: "Crear internal/events/payment_subscriber.go: suscribe a boletos.emitir y encola N tareas Asynq (una por boleto)"
    status: completed
  - id: go-worker
    content: Modificar cmd/worker/main.go para arrancar el subscriber junto al servidor Asynq
    status: completed
  - id: pruebas
    content: Probar con un pago de 3 asientos y verificar 3 boletos (Telegram/Blockchain/S3)
    status: completed
isProject: false
---

# Emisión de N boletos por reserva (mobile → core-transaccional → automatizacion_auditoria)

## Contexto y problema

Hoy los dos servicios no se comunican:

- `core-transaccional` al confirmar el pago publica un evento **Redis Pub/Sub** en `pagos.confirmados` solo con IDs (`PagoConfirmadoEventPublisher`).
- `automatizacion_auditoria` usa **Asynq** (cola persistente, otro protocolo en Redis) y espera la tarea `tasks:pago_exitoso` con el payload completo del boleto.
- Una reserva puede tener N `BOLETO_ASIENTO`, pero nada dispara la generación de PDFs.
- `automatizacion_auditoria` no puede leer PostgreSQL (regla de `AGENTS.md`).

## Decisiones (confirmadas)

- **Bridge en Go**: `automatizacion_auditoria` se suscribe a un canal Pub/Sub y desde ahí encola tareas Asynq (1 por asiento), conservando reintentos/durabilidad.
- **core-transaccional enriquece el evento**: incluye todos los datos del PDF, evitando que Go toque la BD.

## Flujo objetivo

```mermaid
sequenceDiagram
    participant App as App movil
    participant Core as core-transaccional
    participant PG as PostgreSQL
    participant Redis
    participant Bridge as Bridge Go (subscriber)
    participant Asynq as Worker Asynq
    participant Dest as Telegram / Blockchain / S3

    App->>Core: realizarPago(idReserva)
    Core->>PG: PAGO CONFIRMADO + boletos EMITIDO
    Core->>Redis: PUBLISH boletos.emitir (payload con array de boletos)
    Bridge->>Redis: SUBSCRIBE boletos.emitir
    Redis-->>Bridge: evento (3 boletos)
    loop por cada boleto
        Bridge->>Redis: Enqueue tasks:pago_exitoso
    end
    Asynq->>Redis: toma cada tarea
    Asynq->>Dest: PDF -> Telegram -> Blockchain -> S3
```

## Lado Java: core-transaccional

Nuevo canal dedicado `boletos.emitir` (no se toca `pagos.confirmados`, que sigue alimentando BI).

- **Nuevo** `pagos/event/BoletosEmitirEventPublisher.java`: publica un JSON con datos del viaje/usuario y un array `boletos`. Una entrada por cada `BOLETO_ASIENTO` de la reserva.
- **Modificar** [PagoService.java](core-transaccional/src/main/java/com/agencia/viajes/transaccional/pagos/service/PagoService.java):
  - Construir el payload del evento **dentro de la transacción** (mientras la sesión JPA está abierta) para evitar `LazyInitializationException`. Los datos salen de objetos ya cargados: `reserva.getUsuario().getEmail()` y `getNombreCompleto()`, `reserva.getViajeProgramado().getRutaDestino()` (`getCiudadOrigen()`/`getCiudadDestino()`), `getFechaHoraSalida()` (separar en fecha y hora `HH:mm`), y `boletoAsientoRepository.findByReservaId(...)` (asiento, nombre pasajero, tipo).
  - En `registrarEventoDespuesDelCommit(...)` (afterCommit) hacer solo el `convertAndSend` del string ya construido, igual que el publisher actual.
- **Modificar** `application.properties`: añadir `app.events.boletos-emitir-channel=boletos.emitir`.

Payload propuesto:

```json
{
  "eventType": "BOLETOS_EMITIR",
  "idPago": 10,
  "idReserva": 5,
  "email": "juan@example.com",
  "origen": "Santa Cruz",
  "destino": "La Paz",
  "fecha": "2026-06-15",
  "hora": "08:30",
  "boletos": [
    { "idBoleto": 1, "asiento": "12A", "nombre": "Juan Perez", "tipoPasajero": "ADULTO" },
    { "idBoleto": 2, "asiento": "12B", "nombre": "Maria Perez", "tipoPasajero": "ADULTO" }
  ]
}
```

## Lado Go: automatizacion_auditoria

- **Modificar** [config/config.go](automatizacion_auditoria/config/config.go): añadir `BoletosEmitirChannel` (env `BOLETOS_EMITIR_CHANNEL`, default `boletos.emitir`) y un constructor `RedisClient()` que devuelva un `*redis.Client` (go-redis) reutilizando `RedisAddr`, usuario, password y TLS (la misma lógica de `AsynqRedisClientOpt`).
- **Nuevo** `internal/events/payment_subscriber.go`: se suscribe con go-redis a `boletos.emitir`, parsea el evento y, **por cada** boleto, encola una tarea `tasks:pago_exitoso` con el payload que ya consume el handler (`nombre`, `origen`, `destino`, `fecha`, `hora`, `asiento`, `email`, `id_reserva`). Usa `asynq.TaskID(idBoleto)` para idempotencia. Maneja errores con log sin tumbar el proceso.
- **Modificar** [cmd/worker/main.go](automatizacion_auditoria/cmd/worker/main.go): crear un `asynq.Client`, lanzar el subscriber en una goroutine con `context.Context`, y seguir corriendo `srv.Run(mux)`. El mismo proceso suscribe y procesa.

No cambia el handler ni el generador de PDF: el multi-asiento se resuelve encolando N tareas.

## Notas

- El diseño funciona aunque la reserva tenga 1 o N boletos (3 asientos = 3 tareas = 3 PDFs), cada uno con su hash blockchain y su archivo en S3.
- Fuera de alcance (posible siguiente paso): que `automatizacion_auditoria` devuelva `hash_blockchain`/`codigo_qr` a core para rellenar esas columnas en `BOLETO_ASIENTO`.

## Pruebas

- Local: publicar manualmente un evento en `boletos.emitir` (con `redis-cli PUBLISH` o un pequeño `cmd/test-emitir`) con 3 boletos y verificar 3 PDFs en Telegram/S3.
- End-to-end: `realizarPago` en core con una reserva de 3 asientos y observar los logs `[pago_exitoso]` del worker.