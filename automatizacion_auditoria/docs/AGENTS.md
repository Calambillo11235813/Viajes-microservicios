# Reglas de Entorno y Contexto para Agentes de IA (AGENTS.md) - Versión Go + Fiber

## 📌 Contexto del Proyecto
Estás operando en el **Microservicio C (Automatización y Auditoría)** de una plataforma de Agencia de Viajes. Este componente actúa principalmente como un servicio en segundo plano (*background worker*) y expone endpoints ligeros para recibir eventos distribuidos.

* **Responsabilidad Principal:**
  1. Consumir tareas asíncronas desde una cola de mensajería (Redis) usando la librería `asynq`.
  2. Generar boletos electrónicos en formato PDF usando la librería `gofpdf`.
  3. Interactuar con redes Blockchain (compatibles con EVM) para registrar el hash criptográfico de los boletos usando `go-ethereum`.
  4. Disparar Webhooks hacia n8n para orquestar notificaciones y automatizaciones externas utilizando el cliente HTTP nativo de Go.
* **Límites Arquitectónicos:** Queda **estrictamente prohibido** que este servicio se conecte a PostgreSQL o DynamoDB de forma directa. Tampoco debe exponer una API GraphQL ni ejecutar modelos de Machine Learning.

## 🛠️ Stack Tecnológico
* **Lenguaje:** Go (Golang) 1.20 o superior (Tipado estricto, concurrencia nativa).
* **Framework Web (Lanzamiento de Endpoints):** Fiber (`github.com/gofiber/fiber/v2`) - Elegido por su similitud con Express y alto rendimiento.
* **Gestión de Colas (Worker):** Asynq (`github.com/hibiken/asynq`) respaldado por Redis - El equivalente perfecto a BullMQ para el ecosistema de Go.
* **Generación de Documentos:** Gofpdf (`github.com/jung-kurt/gofpdf`).
* **Criptografía y Web3:** Go-Ethereum (`github.com/ethereum/go-ethereum`) para la firma de transacciones y hashing SHA-256 nativo.
* **Despliegue Objetivo:** Contenedores Docker orquestados en DigitalOcean.

## 🚀 Comandos de Ejecución Local
* Inicializar y descargar dependencias: `go mod tidy`
* Ejecutar en modo desarrollo (con recarga en vivo vía Air): `air` (si está instalado) o `go run cmd/main.go`
* Compilar a producción: `go build -o bin/main cmd/main.go`

---

## 📝 Estándares de Código y Arquitectura en Go

### 1. Arquitectura de Paquetes Limpia (Clean Packages)
El código debe organizarse siguiendo las convenciones idiomáticas de Go. Se prohíbe emular estructuras de NestJS con carpetas vacías; en su lugar, se estructurará por dominios funcionales dentro de `internal/`:
* **`internal/pdf`:** Lógica exclusiva para "dibujar" el diseño del boleto usando `gofpdf`. Debe exponer un método como `GenerateTicketPDF(data TicketData) ([]byte, error)`.
* **`internal/blockchain`:** Lógica para generar el hash criptográfico del PDF (`crypto/sha256`) e interactuar con el Smart Contract usando `go-ethereum`.
* **`internal/webhooks`:** Funciones dedicadas a despachar payloads JSON utilizando `net/http` hacia las URLs de n8n.
* **`internal/queue`:** Configuración de los *Handlers* de Asynq para procesar las tareas asíncronas de Redis en segundo plano.

### 2. Inyección de Dependencias Mediante Interfaces
* En Go, las interfaces se implementan implícitamente. Define interfaces en los paquetes de consumo (ej. `type PDFService interface`) para permitir un acoplamiento débil y facilitar las pruebas unitarias (*mocking*).
* Pasa las dependencias (como el cliente de Redis o la configuración) explícitamente a través de funciones constructoras (ej. `NewQueueHandler(cfg *config.Config, pdfSvc pdf.Service)`).

### 3. Manejo de Errores Idiomático e Ininterrumpido
* Queda estrictamente prohibido ignorar errores con el guion bajo `_` en puntos críticos. Siempre evalúa `if err != nil`.
* Dado que el *worker* procesa colas en segundo plano, un error en una tarea (ej. caída temporal de la Blockchain) **nunca debe hacer colapsar (`panic`) el hilo principal**. El error debe ser retornado para que Asynq ejecute la estrategia de reintentos (*retries*) configurada.

### 4. Concurrencia Segura
* Aprovecha las **Goroutines** para tareas independientes que no bloqueen el servidor de Fiber, asegurando el uso de `context.Context` para la propagación de cancelaciones y tiempos límite (*timeouts*).

---
> **⚡ INSTRUCCIÓN CRÍTICA PARA EL AGENTE:** Antes de generar manejadores de tareas o lógica de certificación, lee el archivo `captura_de_requisitos.md` para entender qué datos exactos necesita el PDF y el Smart Contract.
