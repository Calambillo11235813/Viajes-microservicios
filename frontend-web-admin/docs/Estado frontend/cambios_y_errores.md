# Registro de Cambios y Errores Resueltos

> **Proyecto:** viajes-microservicios (frontend-web-admin + core-transaccional)  
> **Fecha:** 2026-06-04  
> **Sesión de desarrollo asistida por IA**

---

## Índice de Cambios

1. [Implementación del Core y Servicios](#1-implementación-del-core-y-servicios)
2. [Módulo de Login y Autenticación](#2-módulo-de-login-y-autenticación)
3. [CORS — Configuración Backend](#3-cors--configuración-backend)
4. [Bug de Contraseña en AuthService](#4-bug-de-contraseña-en-authservice)
5. [Módulos de Administrador (Rutas, Viajes, Flotas)](#5-módulos-de-administrador)
6. [CRUD de Flotas — Implementación End-to-End](#6-crud-de-flotas--implementación-end-to-end)
7. [Error de Import Missing (Flota)](#7-error-de-import-missing-flota)
8. [Error de Tipo: idRuta String vs Int](#8-error-de-tipo-idruta-string-vs-int)
9. [Error GraphQL: listarFlotas undefined](#9-error-graphql-listarflotas-undefined)
10. [Renombramiento de Flota.id → Flota.idBus](#10-renombramiento-de-flotaid--flotaidbus)
11. [Dashboard BI con Chart.js](#11-dashboard-bi-con-chartjs)

---

## 1. Implementación del Core y Servicios

### Archivos creados
- `src/app/core/models/business.models.ts` — Interfaces TypeScript (`RutaDestino`, `ViajeDisponible`, `ReporteVentas`, `Flota`).
- `src/app/core/services/graphql.service.ts` — Servicio centralizado de comunicación GraphQL con `HttpClient`.
- `src/app/core/services/auth.service.ts` — Gestión de autenticación (token, perfil, rol).
- `src/app/core/guards/auth.guard.ts` — Protección de rutas según autenticación.
- `src/app/core/interceptors/auth.interceptor.ts` — Inyección automática del token en headers.

### Decisiones de diseño
- Se usó `HttpClient` directamente en vez de `apollo-angular` para simplificar la arquitectura.
- El servicio GraphQL centraliza todas las llamadas POST al endpoint `http://localhost:9090/graphql`.

---

## 2. Módulo de Login y Autenticación

### Archivos creados/modificados
- `src/app/features/login/` — Componente de login con `ReactiveFormsModule`.
- `src/app/app.routes.ts` — Configuración de rutas con lazy loading y guards.

### Flujo implementado
1. Usuario ingresa email + contraseña.
2. Frontend envía mutación `login(email, passwordHash)` al backend.
3. Backend valida credenciales y devuelve token + perfil de usuario.
4. Frontend almacena en `localStorage` y redirige según rol.

---

## 3. CORS — Configuración Backend

### ❌ Error encontrado
```
Access to XMLHttpRequest at 'http://localhost:9090/graphql' from origin 
'http://localhost:4200' has been blocked by CORS policy
```

### ✅ Solución aplicada
**Archivo creado:** `core-transaccional/src/main/java/com/agencia/viajes/transaccional/config/CorsConfig.java`

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("http://localhost:4200")
                    .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
```

---

## 4. Bug de Contraseña en AuthService

### ❌ Error encontrado
Al intentar iniciar sesión con las credenciales `admin@viajes.com / admin123`, el backend respondía siempre con **"Credenciales incorrectas"**.

### 🔍 Causa raíz
El `DatabaseSeeder.java` guardaba las contraseñas **encriptadas con SHA-256** usando `PasswordEncoder.encode()`. Sin embargo, `AuthService.java` hacía una comparación **en texto plano**:

```java
// ❌ ANTES (bug)
if (!usuario.getPasswordHash().equals(password)) {
    throw new IllegalArgumentException("Credenciales incorrectas");
}
```

### ✅ Solución aplicada
**Archivo modificado:** `AuthService.java`

```java
// ✅ DESPUÉS (corregido)
private final PasswordEncoder passwordEncoder;

if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
    throw new IllegalArgumentException("Credenciales incorrectas");
}
```

---

## 5. Módulos de Administrador

### Archivos creados
- `src/app/features/inventory/rutas/` — Tabla dinámica con modal para crear rutas.
- `src/app/features/inventory/viajes/` — Interfaz dual (sidebar rutas + tabla viajes).

### Queries/Mutations conectadas
- `listarRutas`, `crearRuta`, `eliminarRuta`
- `listarViajesPorRuta`, `programarViaje`

---

## 6. CRUD de Flotas — Implementación End-to-End

### Contexto
El módulo de flotas inicialmente era **un mock visual** con datos estáticos, ya que el backend no exponía endpoints GraphQL para la entidad `Flota`.

### Archivos creados (Backend)
| Archivo | Descripción |
|---------|-------------|
| `schema.graphqls` (modificado) | Añadido `type Flota`, `listarFlotas`, `crearFlota`, `actualizarFlota`, `eliminarFlota` |
| `flotas/service/FlotaService.java` | Lógica de negocio CRUD sobre `FlotaRepository` |
| `flotas/graphql/FlotaResolver.java` | Controlador GraphQL con `@QueryMapping` y `@MutationMapping` |

### Archivos modificados (Frontend)
| Archivo | Cambio |
|---------|--------|
| `business.models.ts` | Añadida interfaz `Flota` |
| `graphql.service.ts` | Añadidos métodos `listarFlotas`, `crearFlota`, `actualizarFlota`, `eliminarFlota` |
| `flotas/flotas.ts` | Reescrito: inyección de `GraphqlService`, formulario reactivo, lógica CRUD |
| `flotas/flotas.html` | Reescrito: cards dinámicas con `*ngFor`, modal de creación/edición, botón eliminar |

---

## 7. Error de Import Missing (Flota)

### ❌ Error encontrado
```
src/app/core/services/graphql.service.ts:161:68:
  161 │ ...rFlota(idBus: number, flota: Partial<Flota>): Observable<Flota> {
```
Angular no compilaba porque el tipo `Flota` no estaba importado.

### ✅ Solución aplicada
**Archivo modificado:** `graphql.service.ts`
```diff
- import { RutaDestino, ViajeDisponible, ReporteVentas } from '../models/business.models';
+ import { RutaDestino, ViajeDisponible, ReporteVentas, Flota } from '../models/business.models';
```

---

## 8. Error de Tipo: idRuta String vs Int

### ❌ Error encontrado
```
Error: Variable 'idRuta' has an invalid value: Expected a value that can be 
converted to type 'Int' but it was a 'String'
```

### 🔍 Causa raíz
GraphQL devuelve los campos de tipo `ID!` como **strings** (ej: `"1"`, `"2"`). El frontend pasaba `ruta.id` (string) directamente a `listarViajesPorRuta`, pero el schema espera `Int!`.

### ✅ Solución aplicada
**Archivos modificados:**

`viajes.ts`:
```diff
- this.seleccionarRuta(this.rutas[0].id);
+ this.seleccionarRuta(Number(this.rutas[0].id));

- this.graphqlService.listarViajesPorRuta(idRuta).subscribe({
+ this.graphqlService.listarViajesPorRuta(Number(idRuta)).subscribe({
```

`viajes.html`:
```diff
- (click)="seleccionarRuta(ruta.id)"
+ (click)="seleccionarRuta(+ruta.id)"

- rutaSeleccionadaId === ruta.id
+ rutaSeleccionadaId === +ruta.id
```

---

## 9. Error GraphQL: listarFlotas undefined

### ❌ Error encontrado
```
Error: Validation error (FieldUndefined@[listarFlotas]) : 
Field 'listarFlotas' in type 'Query' is undefined
```

### 🔍 Causa raíz
Los archivos nuevos de Java (`FlotaResolver.java`, `FlotaService.java`) y el schema actualizado (`schema.graphqls`) **no fueron recogidos por Spring Boot** porque el backend no fue reiniciado.

### ✅ Solución
Reiniciar el backend con `Ctrl+C` → `./gradlew bootRun`.

---

## 10. Renombramiento de Flota.id → Flota.idBus

### ❌ Error potencial detectado
La entidad JPA `Flota.java` tenía el campo llamado `id`, pero el schema GraphQL esperaba `idBus`. Spring for GraphQL mapea por nombre de getter (`getId()` vs `getIdBus()`), lo que causaría un error de mapeo.

### ✅ Solución aplicada
**Archivos modificados:**

| Archivo | Cambio |
|---------|--------|
| `Flota.java` | Campo `id` → `idBus` |
| `ViajeConsultaService.java` | `flota.getId()` → `flota.getIdBus()` |
| `RutasAdminService.java` | `getFlota().getId()` → `getFlota().getIdBus()` (2 ocurrencias) |
| `ViajeProgramadoRepository.java` | JPQL: `v.flota.id` → `v.flota.idBus` |

---

## 11. Dashboard BI con Chart.js

### Archivos creados
- `src/app/features/dashboard-bi/` — Panel gerencial con métricas y gráficos.

### Funcionalidades
- Tarjetas KPI: Ingresos totales, cantidad de pagos, ocupación promedio.
- Gráfico de líneas interactivo (Chart.js) con datos del endpoint `generarReporteVentas`.
- Selector de rango de fechas.

---

## Resumen de Archivos Tocados

### Backend (core-transaccional)
| Archivo | Acción |
|---------|--------|
| `config/CorsConfig.java` | **NUEVO** |
| `config/PasswordEncoder.java` | Existente (sin cambios) |
| `usuarios/service/AuthService.java` | **MODIFICADO** — Inyección de PasswordEncoder |
| `flotas/model/Flota.java` | **MODIFICADO** — Renombrar `id` → `idBus` |
| `flotas/service/FlotaService.java` | **NUEVO** |
| `flotas/graphql/FlotaResolver.java` | **NUEVO** |
| `resources/graphql/schema.graphqls` | **MODIFICADO** — Añadir type Flota + queries + mutations |
| `viajes/service/ViajeConsultaService.java` | **MODIFICADO** — `getId()` → `getIdBus()` |
| `rutas/service/RutasAdminService.java` | **MODIFICADO** — `getId()` → `getIdBus()` |
| `viajes/repository/ViajeProgramadoRepository.java` | **MODIFICADO** — JPQL `v.flota.id` → `v.flota.idBus` |

### Frontend (frontend-web-admin)
| Archivo | Acción |
|---------|--------|
| `core/models/business.models.ts` | **MODIFICADO** — Añadir interfaz `Flota` |
| `core/services/graphql.service.ts` | **MODIFICADO** — Métodos CRUD Flota + import |
| `core/services/auth.service.ts` | **NUEVO** |
| `core/guards/auth.guard.ts` | **NUEVO** |
| `core/interceptors/auth.interceptor.ts` | **NUEVO** |
| `features/login/` | **NUEVO** |
| `features/inventory/rutas/` | **NUEVO** |
| `features/inventory/viajes/viajes.ts` | **MODIFICADO** — `Number()` conversion para idRuta |
| `features/inventory/viajes/viajes.html` | **MODIFICADO** — `+ruta.id` para conversión |
| `features/inventory/flotas/flotas.ts` | **REESCRITO** — De mock a CRUD reactivo |
| `features/inventory/flotas/flotas.html` | **REESCRITO** — De cards estáticas a dinámicas |
| `features/dashboard-bi/` | **NUEVO** |
| `app.routes.ts` | **MODIFICADO** |
