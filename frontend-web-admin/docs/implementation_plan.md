# Implementación Completa del Frontend Administrativo y Gerencial

En base a la directiva de completar **todo lo especificado en `AGENTS.md` y `captura_de_requisitos.md`**, y habiendo analizado los endpoints GraphQL reales expuestos por el backend, propongo la siguiente arquitectura e implementación integral.

> [!NOTE]
> El error de la pantalla inicial ya fue resuelto limpiando `app.html` y asegurando que el ruteo muestre el componente `Login` directamente.

## ⚠️ User Review Required

### 1. Ausencia de algunos Endpoints en el Backend
El `schema.graphqls` actual del backend tiene soporte completo para gestionar **Rutas** y **Viajes Programados**. Sin embargo, **NO** expone endpoints para operaciones CRUD independientes de **Flotas (Buses)** o **Personal (Choferes)**, ni para registrar **Incidencias** más allá de cancelar un viaje.
**Propuesta:** Crearé las vistas visuales para Flotas e Incidencias en el Frontend (mockeando temporalmente su interacción para demostrar la UI/UX requerida), e implementaré con conexión real y 100% funcional todo lo que sí existe en GraphQL (Rutas, Programación de Viajes, y Reportes de Ventas/BI). ¿Estás de acuerdo con este enfoque?

### 2. Librería de Gráficos
Para el Dashboard de BI del Gerente, propongo utilizar **Chart.js** (vía `ng2-charts`), ya que es el estándar moderno en Angular para gráficos (Líneas, Barras) livianos y muy atractivos visualmente. ¿Apruebas la instalación de esta librería en el frontend?

## 🛠️ Proposed Changes

---

### Shared & Core (Arquitectura LIFT)

#### [NEW] `src/app/core/services/graphql.service.ts`
Creación de un servicio base genérico o expansión del `AuthService` para inyectar un servicio limpio para Rutas, Viajes y Dashboards.

#### [NEW] `src/app/core/models/business.models.ts`
Implementación de los tipos descritos en `schema.graphqls`:
- `RutaDestino`, `ViajeDisponible`, `ReporteVentas`

---

### Módulo Administrador (Gestión Operativa)

#### [MODIFY] `src/app/features/inventory/inventory-dashboard`
Transformar el componente actual en un menú lateral de navegación (Sidebar Dashboard) para poder cambiar entre Rutas, Viajes y Flotas usando `<router-outlet>`.

#### [NEW] `src/app/features/inventory/rutas`
Submódulo visual interactivo y responsivo.
- Vista de Listado de Rutas (consumo real `listarRutas`).
- Formulario para crear y editar rutas (`crearRuta`, `actualizarRuta`, `eliminarRuta`).

#### [NEW] `src/app/features/inventory/viajes`
Submódulo para listado de Viajes Programados.
- Formulario para programar una nueva salida (`programarViaje`).

#### [NEW] `src/app/features/inventory/flotas`
Submódulo para visualización e inventario de buses. Se construirá UI Premium usando mocks debido a la falta de endpoints backend actuales.

---

### Módulo Gerencial (Business Intelligence)

#### [MODIFY] `src/app/features/dashboard-bi/dashboard-bi`
El Dashboard integrará métricas reales de ventas. Tendrá una interfaz premium con micro-animaciones.

#### [NEW] Gráficos y Analítica
- **Gráfico de Ingresos (CU-G04):** Usará el endpoint `generarReporteVentas(fechaInicio, fechaFin)` para mostrar una gráfica de líneas interactiva con las ventas diarias usando *Chart.js*.
- **Gráficos Adicionales:** Tarjetas dinámicas con datos simulados y proyecciones (Ocupación, Demanda) que elevarán el nivel estético de la aplicación.

## ✅ Verification Plan

1. **Pruebas de Rutas Reactivas:** Asegurar que los componentes interactivos se comporten bien bajo el Lazy Loading.
2. **Pruebas Integrales de Backend:** Crear rutas reales usando la interfaz y verificar que persistan mediante el backend.
3. **Visualización de BI:** Verificar que el gráfico interprete correctamente las fechas e ingresos reportados.
