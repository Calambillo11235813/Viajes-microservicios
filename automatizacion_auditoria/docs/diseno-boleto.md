# Diseño del boleto electrónico (PDF)

Este documento indica **qué archivo controla la apariencia** del comprobante de viaje y cómo modificarlo en el futuro.

---

## Archivo principal

| Archivo | Responsabilidad |
|---------|-----------------|
| **`internal/pdf/generator.go`** | Diseño visual completo del PDF: colores, layout, tipografía, QR y datos mostrados |

Es el **único archivo** que debes editar para cambiar cómo se ve el boleto. No toca Redis, S3, blockchain ni Telegram.

---

## Punto de entrada

```go
func GenerateTicketPDF(data map[string]interface{}) ([]byte, error)
```

Recibe el payload JSON de la tarea `tasks:pago_exitoso` y devuelve el PDF en bytes.

---

## Estructura interna de `generator.go`

| Función / bloque | Qué controla |
|------------------|--------------|
| Constantes `colorPrimary*`, `colorCard*`, etc. | Paleta de colores (RGB) |
| `drawTicketCard()` | Layout de la tarjeta: cabecera, pasajero, ruta, cuadrícula de datos, QR y pie |
| `drawQRCode()` | Generación e inserción del código QR |
| `stringField()` | Lectura segura de campos del payload JSON |

---

## Campos del payload que alimentan el diseño

| Campo JSON | Uso en el PDF |
|------------|---------------|
| `nombre` | Nombre del pasajero (destacado) |
| `origen` | Ciudad de salida |
| `destino` | Ciudad de llegada |
| `fecha` | Fecha del viaje |
| `hora` | Hora de salida |
| `asiento` | Número de asiento |
| `email` | Correo del pasajero |
| `id_reserva` o `referencia` | Código de referencia (cabecera y QR). Si no vienen, se usa `{fecha}-{asiento}` |

Para mostrar un dato nuevo en el boleto: agrégalo al payload y añade una celda o línea en `drawTicketCard()`.

---

## Acentos y español (UTF-8)

Las fuentes estándar de gofpdf no soportan tildes directamente. El diseño usa:

```go
tr := doc.UnicodeTranslatorFromDescriptor("cp1252")
doc.Cell(..., tr("México"))
```

Todo texto con acentos debe pasar por `tr(...)`.

---

## Código QR

- Librería: `github.com/skip2/go-qrcode`
- Contenido actual: `BOLETO|{referencia}|{nombre}|{origen}|{destino}|{fecha}`
- Tamaño y posición: función `drawQRCode()` (esquina inferior izquierda de la tarjeta)

---

## Cómo probar cambios de diseño

Sin levantar el worker completo:

```powershell
cd automatizacion_auditoria
go run ./cmd/test-telegram
```

Genera el PDF y lo envía a n8n → Telegram. Ideal para iterar el diseño.

Flujo completo (worker + cola):

```powershell
go run ./cmd/worker
go run ./cmd/enqueue-test
```

---

## Librerías relacionadas

| Librería | Uso |
|----------|-----|
| `github.com/jung-kurt/gofpdf` | Dibujo del PDF (texto, rectángulos, imágenes) |
| `github.com/skip2/go-qrcode` | Generación del QR en PNG embebido en el PDF |

---

## Ideas de mejora futura

- [ ] Logo de la agencia (`internal/pdf/assets/logo.png` + `doc.Image()`)
- [ ] Fuente personalizada con `AddUTF8Font()` (DejaVu, Roboto, etc.)
- [ ] Código de barras además del QR
- [ ] Segunda página con términos y condiciones
- [ ] Plantilla distinta por tipo de viaje (autobús, avión, tour)
- [ ] Marca de agua o sello “CERTIFICADO BLOCKCHAIN” con el `txHash`

---

## Archivos que NO son diseño (solo consumen el PDF)

| Archivo | Rol |
|---------|-----|
| `internal/queue/handlers/payment_handler.go` | Orquesta generación → Telegram → blockchain → S3 |
| `internal/webhooks/telegram.go` | Envía el PDF a n8n (campo `file`, MIME `application/pdf`) |
| `internal/storage/s3.go` | Guarda el mismo PDF en S3 |
| `internal/blockchain/hasher.go` | Calcula SHA-256 del PDF generado |

Cambios de diseño en `generator.go` se reflejan automáticamente en Telegram, S3 y blockchain sin tocar esos archivos.
