# Reglas de Entorno y Contexto para Agentes de IA (AGENTS.md)

## 📌 Contexto del Proyecto
[cite_start]Estás operando en el **Microservicio C (Automatización y Auditoría)** de una plataforma de Agencia de Viajes[cite: 1628, 1845]. [cite_start]Este componente actúa exclusivamente como un servicio en segundo plano (*background worker*).
* [cite_start]**Responsabilidad Principal:** 1. Consumir tareas asíncronas desde una cola de mensajería (Redis)[cite: 1643].
 Generar boletos electrónicos en formato PDF[cite: 1629, 1846].
Interactuar con redes Blockchain (compatibles con EVM) para registrar el hash criptográfico de los boletos[cite: 1630, 1664, 1846].
  4. [cite_start]Disparar Webhooks hacia n8n para orquestar notificaciones y automatizaciones externas[cite: 1631, 1671, 1846].
* **Límites Arquitectónicos:** Queda **estrictamente prohibido** que este servicio se conecte a PostgreSQL o DynamoDB. Tampoco debe exponer una API GraphQL ni ejecutar modelos de Machine Learning.

## 🛠️ Stack Tecnológico
**Lenguaje:** Node.js con TypeScript (Tipado estricto obligatorio)[cite: 1641, 1647].
**Framework:** NestJS[cite: 1641, 1845].
**Gestión de Colas:** Redis gestionado a través de las librerías `bullmq` e `ioredis`[cite: 1661].
**Generación de Documentos:** Librería `pdfkit`[cite: 1663].
**Criptografía y Web3:** Librería `ethers` para la conexión y firma de transacciones en la red Blockchain[cite: 1664].
**Despliegue Objetivo:** Contenedores Docker orquestados en DigitalOcean[cite: 1633, 2076].

## 🚀 Comandos de Ejecución Local
* Instalar dependencias: `npm install`
* Ejecutar en modo desarrollo: `npm run start:dev`
* Compilar a producción: `npm run build`

---

## 📝 Estándares de Código y Arquitectura (NestJS)

### 1. Arquitectura Modular Estricta
El código debe estar rigurosamente organizado utilizando los decoradores de NestJS (`@Module`, `@Injectable`, `@Controller`). Mantén la separación de responsabilidades:
* [cite_start]**`PdfGeneratorModule`:** Lógica exclusiva para "dibujar" el diseño del boleto usando `pdfkit`[cite: 1646, 1663].
* [cite_start]**`BlockchainModule`:** Lógica exclusiva para generar el hash del documento e interactuar con el Smart Contract usando `ethers`[cite: 1646, 1664].
* [cite_start]**`WebhooksModule`:** Controladores asíncronos (`@nestjs/axios`) para disparar eventos hacia los flujos de n8n[cite: 1646, 1660].
* [cite_start]**`QueueModule`:** Configuración de los consumidores (`@Processor`) que escuchan los eventos de Redis[cite: 1643, 1661].

### 2. Manejo de Errores en Colas Asíncronas
Al ser un servicio de procesamiento en segundo plano, las fallas de red (ej. caída temporal de la Blockchain o de n8n) son posibles. 
* [cite_start]El agente debe implementar lógicas de reintento automático (*retries*) apoyándose en la configuración nativa de BullMQ[cite: 1644].
* Las excepciones deben ser capturadas globalmente y logueadas de forma descriptiva; el hilo principal del *worker* nunca debe colapsar por una tarea fallida.

### 3. Buenas Prácticas de TypeScript
* Queda prohibido el uso de `any`. Define interfaces claras (`interfaces/` o `dto/`) para los mensajes que se esperan recibir de la cola de Redis (ej. `BoletoPayloadData`).
* Utiliza inyección de dependencias en los constructores de los servicios.

---
> **⚡ INSTRUCCIÓN CRÍTICA PARA EL AGENTE:** Antes de generar consumidores de colas o lógica de certificación, lee el archivo `@docs/captura_de_requisitos_automatizacion.md` para entender qué datos exactos necesita el PDF y el Smart Contract.