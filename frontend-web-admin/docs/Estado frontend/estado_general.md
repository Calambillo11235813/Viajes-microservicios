# Estado General del Proyecto — Frontend Web Admin

> **Última actualización:** 2026-06-04 21:06 (BOT)

---

## 1. Resumen Ejecutivo

El frontend administrativo (`frontend-web-admin`) es una aplicación **Angular 21** con Standalone Components que se comunica con el backend **Spring Boot** (`core-transaccional`) a través de **GraphQL** en `http://localhost:9090/graphql`.

Se ha completado la implementación integral de los módulos de **Administrador** y **Gerente**, incluyendo autenticación, gestión de rutas, viajes, flotas y un dashboard de Business Intelligence.

---

## 2. Estado de los Módulos

| Módulo | Estado | Descripción |
|--------|--------|-------------|
| **Login / Autenticación** | ✅ Funcional | Login via GraphQL (`login` mutation). Almacena token y perfil en `localStorage`. Redirección por rol. |
| **Rutas (CU-A02)** | ✅ Funcional | Listar, crear y eliminar rutas conectadas al backend real vía `listarRutas`, `crearRuta`, `eliminarRuta`. |
| **Viajes (CU-A04, CU-A05)** | ✅ Funcional | Interfaz dual (sidebar de rutas + tabla de viajes). Programar nuevas salidas con `programarViaje`. |
| **Flotas (CU-A03, CU-A06)** | ✅ Funcional | CRUD completo end-to-end. Backend creado desde cero (`FlotaResolver`, `FlotaService`, schema GraphQL). |
| **Dashboard BI (Gerente)** | ✅ Funcional | Reporte de ventas con `Chart.js`. KPIs de ingresos, cantidad de pagos, ocupación. Gráfico de líneas interactivo. |
| **Guards y Roles** | ✅ Funcional | `AuthGuard` protege rutas. Redirección según rol (Admin → inventario, Gerente → dashboard). |

---

## 3. Arquitectura del Frontend

```
frontend-web-admin/src/app/
├── core/
│   ├── models/business.models.ts      # Interfaces: RutaDestino, ViajeDisponible, ReporteVentas, Flota
│   ├── services/
│   │   ├── auth.service.ts            # Autenticación, token, perfil, rol
│   │   └── graphql.service.ts         # Servicio centralizado de comunicación GraphQL
│   ├── guards/auth.guard.ts           # Protección de rutas
│   └── interceptors/auth.interceptor.ts
├── features/
│   ├── login/                         # Componente de inicio de sesión
│   ├── inventory/
│   │   ├── rutas/                     # CRUD de rutas
│   │   ├── viajes/                    # Programación de viajes
│   │   └── flotas/                    # CRUD de flotas (nuevo)
│   └── dashboard-bi/                  # Panel gerencial con Chart.js
└── app.routes.ts                      # Configuración de rutas con lazy loading
```

---

## 4. Conexión con el Backend

- **Endpoint GraphQL:** `http://localhost:9090/graphql`
- **CORS:** Configurado en `CorsConfig.java` para permitir `http://localhost:4200`.
- **Autenticación:** Mutación `login(email, passwordHash)` con comparación SHA-256 via `PasswordEncoder`.

### Queries Disponibles desde el Frontend
| Query/Mutation | Servicio Angular | Estado |
|---|---|---|
| `login` | `AuthService` | ✅ |
| `listarRutas` | `GraphqlService.listarRutas()` | ✅ |
| `crearRuta` | `GraphqlService.crearRuta()` | ✅ |
| `eliminarRuta` | `GraphqlService.eliminarRuta()` | ✅ |
| `listarViajesPorRuta` | `GraphqlService.listarViajesPorRuta()` | ✅ |
| `programarViaje` | `GraphqlService.programarViaje()` | ✅ |
| `generarReporteVentas` | `GraphqlService.generarReporteVentas()` | ✅ |
| `listarFlotas` | `GraphqlService.listarFlotas()` | ✅ |
| `crearFlota` | `GraphqlService.crearFlota()` | ✅ |
| `actualizarFlota` | `GraphqlService.actualizarFlota()` | ✅ |
| `eliminarFlota` | `GraphqlService.eliminarFlota()` | ✅ |

---

## 5. Credenciales de Prueba

| Rol | Email | Contraseña |
|-----|-------|------------|
| Administrador | `admin@viajes.com` | `admin123` |
| Gerente | `gerente@viajes.com` | `gerente123` |

---

## 6. Cómo Ejecutar

```bash
# Terminal 1 — Backend
cd core-transaccional
./gradlew bootRun

# Terminal 2 — Frontend
cd frontend-web-admin
npm start
```

Abrir navegador en `http://localhost:4200`.

---

## 7. Tecnologías Utilizadas

| Tecnología | Versión | Uso |
|---|---|---|
| Angular | 21 | Framework frontend (Standalone Components) |
| TypeScript | 5.x | Lenguaje de programación |
| Chart.js | 4.x | Gráficos del Dashboard BI |
| Tailwind CSS | 4.x | Estilos y diseño visual |
| Spring Boot | 3.x | Backend con GraphQL |
| PostgreSQL | — | Base de datos relacional |
