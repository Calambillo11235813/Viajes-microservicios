# CU-13 — Registrar navegación (visualización y búsqueda)

**Fecha:** 2026-06-07  
**Estado:** Implementado (write-only, tolerante a fallos)

---

## Objetivo

Registrar eventos de navegación del viajero (búsquedas de rutas y visualizaciones de destinos/rutas) en Amazon DynamoDB para alimentar el motor IA y dashboards de BI. La persistencia es **solo escritura**; no se consulta DynamoDB desde este microservicio en esta fase.

---

## Tabla DynamoDB

| Campo | Valor |
|-------|-------|
| Nombre | `NavegacionViajes` |
| Región | `us-east-2` |
| Partition key | `id_usuario` (Number) |
| Sort key | `timestamp` (String, ISO-8601 + UUID para unicidad) |

### Atributos del ítem

| Atributo | Tipo | Descripción |
|----------|------|-------------|
| `id_usuario` | N | PK — identificador del usuario |
| `timestamp` | S | SK — instante UTC + sufijo `#<uuid>` |
| `tipoEvento` | S | `BUSQUEDA_RUTA` o `VISUALIZACION_RUTA` |
| `origen` | S | Ciudad de origen (opcional) |
| `destino` | S | Ciudad de destino (opcional) |
| `fechaBusqueda` | S | Fecha ISO de la búsqueda (solo `BUSQUEDA_RUTA`) |
| `idRuta` | N | Ruta visualizada (solo `VISUALIZACION_RUTA`) |
| `resultadosCount` | N | Cantidad de viajes devueltos (solo `BUSQUEDA_RUTA`) |
| `canal` | S | Origen del evento, p. ej. `APP_MOVIL`, `GRAPHQL` |
| `metadata` | S | JSON opcional para datos flexibles |

---

## API GraphQL

### Mutación explícita — visualización de ruta

| Campo | Valor |
|-------|-------|
| URL | `POST http://localhost:8080/graphql` |
| Operación | `registrarVisualizacionRuta(idUsuario: Int!, idRuta: Int, origen: String, destino: String, canal: String)` |
| Respuesta | `Boolean!` — siempre `true` si la petición fue aceptada |

```graphql
mutation {
  registrarVisualizacionRuta(
    idUsuario: 1
    idRuta: 10
    origen: "La Paz"
    destino: "Uyuni"
    canal: "APP_MOVIL"
  )
}
```

### Integración automática con CU-01 — búsqueda de rutas

| Campo | Valor |
|-------|-------|
| Query | `buscarRutasYHorariosDisponibles(origen, destino, fecha, idUsuario)` |
| Parámetro nuevo | `idUsuario: Int` (opcional) |
| Comportamiento | Tras obtener resultados de PostgreSQL, se invoca `registrarBusquedaRutaAsync` con tipo `BUSQUEDA_RUTA` |
| Condición | Solo registra si `idUsuario` no es nulo |

```graphql
query {
  buscarRutasYHorariosDisponibles(
    origen: "La Paz"
    destino: "Uyuni"
    fecha: "2026-06-15"
    idUsuario: 1
  ) {
    idViaje
    ciudadOrigen
    ciudadDestino
  }
}
```

---

## Archivos creados o modificados

| Archivo | Cambio |
|---------|--------|
| `build.gradle` | BOM AWS SDK v2 (`dynamodb`, `dynamodb-enhanced`) |
| `config/DynamoDbConfig.java` | Beans `DynamoDbClient` y `DynamoDbEnhancedClient`; `@EnableAsync` |
| `navegacion/model/NavegacionItem.java` | Entidad `@DynamoDbBean` mapeada a la tabla existente |
| `navegacion/repository/NavegacionRepository.java` | Write-only con `putItem` |
| `navegacion/service/NavegacionService.java` | `registrarVisualizacion`, `registrarBusquedaRuta`, `registrarBusquedaRutaAsync` |
| `navegacion/graphql/NavegacionResolver.java` | Mutación `@MutationMapping` |
| `viajes/service/ViajeConsultaService.java` | Hook post-búsqueda hacia `NavegacionService` |
| `viajes/graphql/ViajeQueryResolver.java` | Parámetro opcional `idUsuario` en CU-01 |
| `resources/graphql/schema.graphqls` | Mutación y query ampliada |
| `resources/application.properties` | `aws.dynamodb.region`, `table-name`, `endpoint` |
| `test/.../NavegacionServiceTest.java` | Pruebas unitarias de tolerancia a fallos |
| `test/.../ViajeConsultaServiceTest.java` | Prueba de integración con mock de navegación |

---

## Configuración y credenciales

| Entorno | Configuración |
|---------|---------------|
| Desarrollo (AWS real) | `~/.aws/credentials` + `aws.dynamodb.region=us-east-2`; `endpoint` vacío |
| Desarrollo (local) | `aws.dynamodb.endpoint=http://localhost:8000` (DynamoDB Local) o `http://localhost:4566` (LocalStack) |
| Producción | IAM role del runtime (ECS, EKS, EC2); sin credenciales en `application.properties` |

Permiso IAM mínimo: `dynamodb:PutItem` sobre `arn:aws:dynamodb:us-east-2:*:table/NavegacionViajes`.

---

## Manejo de errores

- Si DynamoDB no está disponible o falla `PutItem`, el servicio registra un `WARN` en logs y **no propaga la excepción**.
- La búsqueda CU-01 y la mutación `registrarVisualizacionRuta` siguen respondiendo con normalidad.
- Si `idUsuario` es nulo, el evento se omite silenciosamente (la PK de la tabla lo exige).

---

## Flujo de datos

```
App móvil / Web
    │
    ├─► buscarRutasYHorariosDisponibles (+ idUsuario)
    │       └─► ViajeConsultaService → PostgreSQL
    │       └─► NavegacionService.registrarBusquedaRutaAsync → DynamoDB
    │
    └─► registrarVisualizacionRuta
            └─► NavegacionService.registrarVisualizacion → DynamoDB
```

---

## Pruebas

```bash
cd core-transaccional
.\gradlew.bat test
```

Cobertura relevante:
- `NavegacionServiceTest` — errores de DynamoDB no propagados; omisión sin `idUsuario`.
- `ViajeConsultaServiceTest` — CU-01 invoca tracking y retorna resultados.

---

## Notas

- La escritura de búsquedas es **asíncrona** (`@Async`) para no aumentar la latencia de CU-01.
- No se implementó lectura desde DynamoDB; el consumo lo harán el motor IA y herramientas de BI.
- Los clientes existentes de CU-01 siguen funcionando sin `idUsuario`; simplemente no generan evento de tracking.
