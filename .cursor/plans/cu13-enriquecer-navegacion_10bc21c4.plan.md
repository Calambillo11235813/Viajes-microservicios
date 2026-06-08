---
name: cu13-enriquecer-navegacion
overview: Enriquecer la mutación `registrarVisualizacionRuta` y la app móvil para que los eventos `VISUALIZACION_RUTA` escritos en DynamoDB usen atributos consistentes con los registros sintéticos existentes en `NavegacionViajes`.
todos:
  - id: backend-modelo-ddb
    content: Añadir campos extendidos y mapeos snake_case en NavegacionItem.
    status: completed
  - id: backend-mutacion-servicio
    content: Ampliar schema, resolver y servicio para registrar visualizaciones enriquecidas.
    status: completed
  - id: backend-tests
    content: Actualizar pruebas backend y validar con Gradle.
    status: completed
  - id: app-query-categoria
    content: Agregar categoriaTuristica a tipos y queries de viajes en la app.
    status: completed
  - id: app-mutacion-hook
    content: Ampliar mutación y hook de tracking con campos opcionales.
    status: completed
  - id: app-pantallas
    content: Enviar campos enriquecidos desde SearchResultsScreen y BuscarImagenScreen.
    status: completed
  - id: validacion-final
    content: Ejecutar TypeScript y validar manualmente el registro DynamoDB.
    status: completed
isProject: false
---

# Plan Para Enriquecer CU-13 Navegación

## Decisión Principal

La app debe enviar `categoriaVista` usando `categoriaTuristica`, porque el backend ya expone ese campo en `type ViajeDisponible` y `ViajeDisponibleResponse`. No hace falta modificar el schema de `ViajeDisponible` en `core-transaccional`; sí hace falta que la app lo incluya en `BUSCAR_VIAJES` y en su tipo TypeScript.

En el backend, el código actual de [`core-transaccional/src/main/java/com/agencia/viajes/transaccional/navegacion/model/NavegacionItem.java`](core-transaccional/src/main/java/com/agencia/viajes/transaccional/navegacion/model/NavegacionItem.java) todavía no contiene los campos extendidos mencionados. Por eso el plan incluye añadirlos y mapearlos explícitamente a snake_case para igualar los registros sintéticos.

## 1. Backend: Modelo DynamoDB

Modificar [`core-transaccional/src/main/java/com/agencia/viajes/transaccional/navegacion/model/NavegacionItem.java`](core-transaccional/src/main/java/com/agencia/viajes/transaccional/navegacion/model/NavegacionItem.java).

Agregar campos:

```java
private String idInteraccion;
private String tipoAccion;
private String dispositivo;
private Integer tiempoPermanenciaSeg;
private String categoriaVista;
private Integer idRutaVista;
private String ciudadOrigenVista;
private String ciudadDestinoVista;
```

Agregar getters anotados para snake_case:

```java
@DynamoDbAttribute("id_interaccion")
public String getIdInteraccion() { return idInteraccion; }

@DynamoDbAttribute("tipo_accion")
public String getTipoAccion() { return tipoAccion; }

@DynamoDbAttribute("tiempo_permanencia_seg")
public Integer getTiempoPermanenciaSeg() { return tiempoPermanenciaSeg; }

@DynamoDbAttribute("categoria_vista")
public String getCategoriaVista() { return categoriaVista; }

@DynamoDbAttribute("id_ruta_vista")
public Integer getIdRutaVista() { return idRutaVista; }

@DynamoDbAttribute("ciudad_origen_vista")
public String getCiudadOrigenVista() { return ciudadOrigenVista; }

@DynamoDbAttribute("ciudad_destino_vista")
public String getCiudadDestinoVista() { return ciudadDestinoVista; }
```

También conviene mapear los campos existentes que hoy dependen del nombre Java por defecto, para evitar camelCase en DynamoDB:

```java
@DynamoDbAttribute("tipo_evento")
public String getTipoEvento() { return tipoEvento; }

@DynamoDbAttribute("fecha_busqueda")
public String getFechaBusqueda() { return fechaBusqueda; }

@DynamoDbAttribute("id_ruta")
public Integer getIdRuta() { return idRuta; }

@DynamoDbAttribute("resultados_count")
public Integer getResultadosCount() { return resultadosCount; }
```

## 2. Backend: Contrato GraphQL

Modificar [`core-transaccional/src/main/resources/graphql/schema.graphqls`](core-transaccional/src/main/resources/graphql/schema.graphqls) en la mutación `registrarVisualizacionRuta`.

Contrato propuesto, manteniendo compatibilidad porque todos los nuevos campos son opcionales:

```graphql
registrarVisualizacionRuta(
    idUsuario: Int!
    idRuta: Int
    origen: String
    destino: String
    canal: String
    categoriaVista: String
    ciudadOrigenVista: String
    ciudadDestinoVista: String
    idRutaVista: Int
    tiempoPermanenciaSeg: Int
    dispositivo: String
): Boolean!
```

Notas:
- `idRuta` se mantiene por compatibilidad.
- `idRutaVista` será el atributo preferido para análisis BI/IA porque mapea a `id_ruta_vista`.
- `origen`/`destino` se conservan, pero para visualizaciones se deben poblar también `ciudadOrigenVista` y `ciudadDestinoVista`.

## 3. Backend: Resolver

Modificar [`core-transaccional/src/main/java/com/agencia/viajes/transaccional/navegacion/graphql/NavegacionResolver.java`](core-transaccional/src/main/java/com/agencia/viajes/transaccional/navegacion/graphql/NavegacionResolver.java).

Añadir argumentos opcionales:

```java
@Argument String categoriaVista,
@Argument String ciudadOrigenVista,
@Argument String ciudadDestinoVista,
@Argument Integer idRutaVista,
@Argument Integer tiempoPermanenciaSeg,
@Argument String dispositivo
```

Y pasarlos a `NavegacionService.registrarVisualizacion(...)`.

Para evitar una firma demasiado larga, recomiendo crear un record interno/DTO de servicio:

```java
public record VisualizacionRutaRequest(
        Integer idUsuario,
        Integer idRuta,
        String origen,
        String destino,
        String canal,
        String categoriaVista,
        String ciudadOrigenVista,
        String ciudadDestinoVista,
        Integer idRutaVista,
        Integer tiempoPermanenciaSeg,
        String dispositivo) {
}
```

Ubicación sugerida: [`core-transaccional/src/main/java/com/agencia/viajes/transaccional/navegacion/dto/VisualizacionRutaRequest.java`](core-transaccional/src/main/java/com/agencia/viajes/transaccional/navegacion/dto/VisualizacionRutaRequest.java).

## 4. Backend: Servicio

Modificar [`core-transaccional/src/main/java/com/agencia/viajes/transaccional/navegacion/service/NavegacionService.java`](core-transaccional/src/main/java/com/agencia/viajes/transaccional/navegacion/service/NavegacionService.java).

Cambiar `registrarVisualizacion(...)` para recibir `VisualizacionRutaRequest` y asignar:

```java
item.setIdUsuario(request.idUsuario());
item.setTimestamp(generarTimestampUnico());
item.setIdInteraccion(UUID.randomUUID().toString());
item.setTipoEvento(TIPO_VISUALIZACION_RUTA);
item.setTipoAccion(TIPO_VISUALIZACION_RUTA);
item.setIdRuta(request.idRuta());
item.setIdRutaVista(request.idRutaVista() != null ? request.idRutaVista() : request.idRuta());
item.setOrigen(request.origen());
item.setDestino(request.destino());
item.setCiudadOrigenVista(request.ciudadOrigenVista() != null ? request.ciudadOrigenVista() : request.origen());
item.setCiudadDestinoVista(request.ciudadDestinoVista() != null ? request.ciudadDestinoVista() : request.destino());
item.setCategoriaVista(request.categoriaVista());
item.setTiempoPermanenciaSeg(request.tiempoPermanenciaSeg());
item.setDispositivo(request.dispositivo());
item.setCanal(request.canal() != null && !request.canal().isBlank() ? request.canal().trim() : CANAL_DEFAULT);
```

Compatibilidad:
- Si el cliente antiguo solo envía `idRuta`, `origen`, `destino`, `canal`, el backend seguirá funcionando.
- Si falta `idRutaVista`, se usa `idRuta` como fallback.
- Si faltan `ciudadOrigenVista`/`ciudadDestinoVista`, se usan `origen`/`destino`.
- `tiempoPermanenciaSeg` puede quedar `null`; si se quiere uniformidad total desde la app, enviar `0`.

## 5. Backend: Pruebas

Actualizar [`core-transaccional/src/test/java/com/agencia/viajes/transaccional/navegacion/service/NavegacionServiceTest.java`](core-transaccional/src/test/java/com/agencia/viajes/transaccional/navegacion/service/NavegacionServiceTest.java).

Casos:
- `registrarVisualizacion` guarda item con `categoriaVista`, `ciudadOrigenVista`, `ciudadDestinoVista`, `idRutaVista`, `tiempoPermanenciaSeg`, `dispositivo`.
- Si `idRutaVista` es `null`, usa `idRuta`.
- Si `ciudadOrigenVista`/`ciudadDestinoVista` son `null`, usa `origen`/`destino`.
- Errores del repositorio siguen sin propagarse.

Ejecutar:

```powershell
cd core-transaccional
.\gradlew.bat test --no-daemon
```

## 6. App Móvil: Query De Viajes

Modificar [`app-movil-pasajero/src/graphql/queries/viajes.ts`](app-movil-pasajero/src/graphql/queries/viajes.ts).

Agregar `categoriaTuristica` al tipo:

```ts
export interface ViajeDisponible {
  idViaje: string;
  idRuta: string;
  ciudadOrigen: string;
  ciudadDestino: string;
  fechaHoraSalida: string;
  fechaHoraLlegada: string;
  duracionEstimadaHoras: number;
  precioBase: number;
  categoriaTuristica?: string | null;
  idBus: string;
  tipoBus: string;
  capacidadTotalAsientos: number;
  estadoViaje: string;
}
```

Pedir el campo en `BUSCAR_VIAJES` y `BUSCAR_VIAJES_DESTINO_TURISTICO`:

```graphql
categoriaTuristica
```

Justificación:
- Es el dato más natural para `categoriaVista`.
- Ya existe en el backend para `ViajeDisponible`, no requiere ampliar el DTO de viajes.
- Si viene `null`, la mutación lo omite o lo envía como null sin romper nada.

## 7. App Móvil: Mutación De Navegación

Modificar [`app-movil-pasajero/src/graphql/mutations/navegacion.ts`](app-movil-pasajero/src/graphql/mutations/navegacion.ts).

Ampliar variables:

```ts
export interface RegistrarVisualizacionRutaVars {
  idUsuario: number;
  idRuta?: number;
  origen?: string;
  destino?: string;
  canal?: string;
  categoriaVista?: string | null;
  ciudadOrigenVista?: string;
  ciudadDestinoVista?: string;
  idRutaVista?: number;
  tiempoPermanenciaSeg?: number;
  dispositivo?: string;
}
```

Actualizar la mutación GraphQL con los mismos argumentos opcionales.

## 8. App Móvil: Hook De Tracking

Modificar [`app-movil-pasajero/src/hooks/useNavegacionTracking.ts`](app-movil-pasajero/src/hooks/useNavegacionTracking.ts).

Ampliar `RegistrarVisualizacionInput`:

```ts
export interface RegistrarVisualizacionInput {
  idRuta?: number;
  origen?: string;
  destino?: string;
  categoriaVista?: string | null;
  ciudadOrigenVista?: string;
  ciudadDestinoVista?: string;
  idRutaVista?: number;
  tiempoPermanenciaSeg?: number;
  dispositivo?: string;
}
```

Enviar variables adicionales:

```ts
variables: {
  idUsuario,
  idRuta: input.idRuta,
  origen: input.origen,
  destino: input.destino,
  canal: 'APP_MOVIL',
  categoriaVista: input.categoriaVista,
  ciudadOrigenVista: input.ciudadOrigenVista,
  ciudadDestinoVista: input.ciudadDestinoVista,
  idRutaVista: input.idRutaVista,
  tiempoPermanenciaSeg: input.tiempoPermanenciaSeg,
  dispositivo: input.dispositivo,
}
```

Mantener la política actual:
- Si `idUsuario` no existe, no enviar nada.
- Si falla, solo `console.log('[CU-13] ...')`.
- No bloquear navegación.

## 9. App Móvil: Pantalla De Resultados

Modificar [`app-movil-pasajero/src/screens/home/SearchResultsScreen.tsx`](app-movil-pasajero/src/screens/home/SearchResultsScreen.tsx).

Importar `Platform`:

```ts
import { View, Text, FlatList, ActivityIndicator, TouchableOpacity, Platform } from 'react-native';
```

Actualizar llamada:

```ts
void registrarVisualizacionRuta({
  idRuta: Number(item.idRuta),
  idRutaVista: Number(item.idRuta),
  origen: item.ciudadOrigen,
  destino: item.ciudadDestino,
  ciudadOrigenVista: item.ciudadOrigen,
  ciudadDestinoVista: item.ciudadDestino,
  categoriaVista: item.categoriaTuristica,
  tiempoPermanenciaSeg: 0,
  dispositivo: Platform.OS,
});
```

## 10. App Móvil: Búsqueda Visual

Modificar [`app-movil-pasajero/src/screens/home/BuscarImagenScreen.tsx`](app-movil-pasajero/src/screens/home/BuscarImagenScreen.tsx).

Ya importa `Platform`, así que solo ampliar la llamada:

```ts
void registrarVisualizacionRuta({
  idRuta: Number(viaje.idRuta),
  idRutaVista: Number(viaje.idRuta),
  origen: viaje.ciudadOrigen,
  destino: viaje.ciudadDestino,
  ciudadOrigenVista: viaje.ciudadOrigen,
  ciudadDestinoVista: viaje.ciudadDestino,
  categoriaVista: viaje.categoriaTuristica,
  tiempoPermanenciaSeg: 0,
  dispositivo: Platform.OS,
});
```

## 11. Validación En DynamoDB

Con backend y app levantados:

1. Iniciar sesión.
2. Buscar ruta desde Home.
3. Seleccionar un viaje.
4. Revisar el último item en `NavegacionViajes`.

AWS CLI sugerido:

```powershell
aws dynamodb query `
  --table-name NavegacionViajes `
  --region us-east-2 `
  --key-condition-expression "id_usuario = :u" `
  --expression-attribute-values '{":u":{"N":"1"}}' `
  --scan-index-forward false `
  --limit 5
```

El registro nuevo de visualización debe incluir, al menos:

```json
{
  "id_usuario": { "N": "1" },
  "timestamp": { "S": "..." },
  "tipo_evento": { "S": "VISUALIZACION_RUTA" },
  "tipo_accion": { "S": "VISUALIZACION_RUTA" },
  "id_ruta_vista": { "N": "10" },
  "ciudad_origen_vista": { "S": "La Paz" },
  "ciudad_destino_vista": { "S": "Uyuni" },
  "categoria_vista": { "S": "..." },
  "tiempo_permanencia_seg": { "N": "0" },
  "dispositivo": { "S": "android" },
  "canal": { "S": "APP_MOVIL" }
}
```

## 12. Consideraciones De Compatibilidad

- La mutación conserva los argumentos actuales, por lo que clientes existentes no se rompen.
- Los nuevos argumentos son opcionales.
- Los registros antiguos en DynamoDB no requieren migración.
- Los registros nuevos serán más completos, pero si `categoriaTuristica` viene `null`, `categoria_vista` quedará ausente o null.
- El campo `tipoEvento` actual puede convivir con `tipo_accion`; para BI se recomienda usar `tipo_accion` si los datos sintéticos ya lo usan.
- Si se necesita uniformidad total también para `BUSQUEDA_RUTA`, habría que hacer una segunda fase para mapear `fechaBusqueda` y `resultadosCount` a snake_case y agregar `tipo_accion`/`dispositivo` en búsquedas.

## Orden De Implementación

1. Añadir campos y `@DynamoDbAttribute` snake_case en `NavegacionItem`.
2. Crear `VisualizacionRutaRequest` para evitar firmas largas.
3. Ampliar `schema.graphqls`, `NavegacionResolver` y `NavegacionService`.
4. Actualizar pruebas backend y ejecutar Gradle tests.
5. Añadir `categoriaTuristica` en tipos y queries de la app.
6. Ampliar mutación `navegacion.ts` y hook `useNavegacionTracking`.
7. Enviar campos enriquecidos desde `SearchResultsScreen` y `BuscarImagenScreen`.
8. Ejecutar `npx tsc --noEmit`.
9. Validar manualmente un item nuevo en DynamoDB contra un registro sintético.