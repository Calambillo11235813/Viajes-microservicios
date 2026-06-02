# Reglas de Entorno y Contexto para Agentes de IA (AGENTS.md)

## 📌 Contexto del Proyecto
Estás operando en el **Frontend Web (Administrativo)** de una plataforma de Agencia de Viajes. Este componente es la interfaz visual principal para el personal interno de la agencia (Administradores y Gerentes).
* **Responsabilidad Principal:** Proporcionar paneles de control para la gestión del inventario de viajes (rutas, flotas, pasajes) y la visualización interactiva de Dashboards de Business Intelligence (BI).
* **Límites Arquitectónicos:** Este frontend **NO** se conecta a bases de datos de forma directa, **NO** maneja la lógica de validación transaccional compleja y **NO** procesa pasarelas de pago. Todo el flujo de datos se delega mediante consumo de APIs (idealmente GraphQL) hacia el API Gateway / Core Transaccional.

## 🛠️ Stack Tecnológico
* **Lenguaje:** TypeScript (Estricto).
* **Framework:** Angular.
* **Capa de Comunicación:** Cliente GraphQL (ej. Apollo Angular) o en su defecto `HttpClient` nativo de Angular.
* **Diseño y UI:** HTML5, SCSS/CSS puro, complementado con librerías de componentes (TailwindCSS).
* **Gestor de Paquetes:** npm.

## 🚀 Comandos de Ejecución
* Instalar dependencias iniciales: `npm install`
* Ejecutar en modo desarrollo local: `ng serve -o`
* Compilar para producción (Multinube/Docker): `ng build`
* Ejecutar pruebas unitarias: `ng test`

---

## 📝 Estándares de Código y Documentación (Angular)

### 1. Documentación (JSDoc)
Todo el código de servicios y componentes principales debe estar documentado utilizando el estándar **JSDoc**.
* Describe claramente el propósito de los métodos complejos y los flujos de RxJS.
* Utiliza las etiquetas `@param` y `@returns` para definir qué datos entran y salen de los métodos.
* Comenta la razón de las implementaciones asíncronas o el uso de ciertos operadores reactivos.

### 2. Convenciones de Arquitectura de Carpetas y Módulos
Mantén una separación estricta de responsabilidades en la carpeta `src/app/` (Arquitectura LIFT):
* **Core (`core/`):** Contiene servicios *singleton* (una sola instancia), guardias de rutas (`Guards`), interceptores HTTP y modelos globales. No debe importar componentes visuales.
* **Shared (`shared/`):** Contiene componentes visuales reutilizables (botones, modales, tablas, *pipes*, *directives*). Los módulos que usen estos componentes deben importarlos desde aquí.
* **Features/Modules (`features/`):** Agrupa el código por funcionalidad del negocio (ej. `auth`, `dashboard-bi`, `inventory`). Debe implementarse **Lazy Loading** (carga perezosa) en el sistema de enrutamiento para que estos módulos solo se carguen cuando el usuario navegue hacia ellos.

### 3. Buenas Prácticas de Programación
* **Reactividad (RxJS):** Utiliza *Observables* para manejar el flujo de datos asíncronos y eventos de la UI. Evita suscribirte anidadamente (callback hell); usa operadores como `switchMap`, `map` o `catchError`. Prefiere usar el pipe `async` en el HTML en lugar de suscribirte manualmente en el `.ts`.
* **Inyección de Dependencias:** Inyecta servicios estrictamente a través del `constructor` del componente o servicio.
* **Tipado Estricto:** Prohibido el uso de `any`. Toda respuesta de la API debe estar mapeada a una `interface` o `type` de TypeScript en la carpeta correspondiente de modelos (`models/` o `dto/`).
* **Nomenclatura:** Variables y métodos en `camelCase`. Nombres de archivos y carpetas en `kebab-case` (ej. `viaje-list.component.ts`). Clases e interfaces en `PascalCase`.

### 4. Seguridad, Estado y Manejo de Errores
* **Interceptores:** Implementa un `HttpInterceptor` para adjuntar automáticamente el token JWT (o credenciales) en cada petición saliente.
* **Manejo Global de Errores:** Usa un interceptor secundario o un `ErrorHandler` global para capturar errores de red o códigos HTTP de error (401, 500) y mostrar notificaciones amigables al usuario (ej. un *Snackbar* o *Toast*).
* **Protección de Rutas:** Todas las vistas administrativas deben estar protegidas por `AuthGuard` para asegurar que solo personal autenticado acceda al panel.

---
> **⚡ INSTRUCCIÓN CRÍTICA PARA EL AGENTE:** Antes de generar código de interfaces o enrutamiento, debes leer obligatoriamente el archivo `@docs/captura_de_requisitos.md` para entender qué vistas le corresponden al rol de `GERENTE` y cuáles al rol de `ADMINISTRADOR`, garantizando que el diseño de la UI cumpla con las reglas del negocio de la Agencia de Viajes.