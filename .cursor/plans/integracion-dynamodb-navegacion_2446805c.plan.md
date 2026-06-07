---
name: integracion-dynamodb-navegacion
overview: Integrar escritura de eventos de navegación en DynamoDB desde `core-transaccional` usando AWS SDK v2, con mutación GraphQL explícita y registro automático desde CU-01 sin romper el flujo principal si DynamoDB falla.
todos:
  - id: deps-config
    content: Añadir dependencias AWS SDK v2 y propiedades DynamoDB.
    status: completed
  - id: ddb-config
    content: Crear configuración Spring para DynamoDB con DefaultCredentialsProvider.
    status: completed
  - id: navigation-domain
    content: Crear modelo, repositorio y servicio write-only de navegación.
    status: completed
  - id: graphql-mutation
    content: Agregar mutación registrarVisualizacionRuta y resolver GraphQL.
    status: completed
  - id: cu01-hook
    content: Integrar registro automático desde ViajeConsultaService para CU-01.
    status: completed
  - id: tests-docs
    content: Definir pruebas unitarias/integración y actualizar documentación de estado.
    status: completed
isProject: false
---

# Plan De Integración DynamoDB Para Navegación

## Alcance Y Decisiones

- La integración será write-only contra la tabla existente `NavegacionViajes` en `us-east-2`.
- Se respetará el esquema real indicado: partition key `id_usuario` de tipo `N` y sort key `timestamp` de tipo `S`. Aunque la guía NoSQL del proyecto recomienda `PK/SK` genéricos, aquí conviene adaptar el mapeo Java a la tabla ya creada para evitar recrear infraestructura.
- Los nuevos archivos vivirán bajo `src/main/java/com/agencia/viajes/transaccional/navegacion/`, manteniendo el paquete real del proyecto `com.agencia.viajes.transaccional`.
- No habrá lectura desde DynamoDB ni migraciones Flyway; DynamoDB se configura por propiedades y credenciales AWS.

## 1. Dependencias En Gradle

Actualizar [`core-transaccional/build.gradle`](core-transaccional/build.gradle) en `dependencies` con AWS SDK v2:

```gradle
implementation platform('software.amazon.awssdk:bom:<version-2.x>')
implementation 'software.amazon.awssdk:dynamodb'
implementation 'software.amazon.awssdk:dynamodb-enhanced'
```

Notas:
- Usar una versión estable actual del BOM de AWS SDK v2 al implementar.
- `dynamodb-enhanced` permite usar `@DynamoDbBean`, `@DynamoDbPartitionKey` y `@DynamoDbSortKey`.
- No añadir dependencia de Spring Cloud AWS si solo se requiere SDK v2 directo.

## 2. Propiedades De Configuración

Agregar en [`core-transaccional/src/main/resources/application.properties`](core-transaccional/src/main/resources/application.properties):

```properties
# DynamoDB - navegación
aws.dynamodb.region=us-east-2
aws.dynamodb.table-name=NavegacionViajes
# Solo para DynamoDB Local/LocalStack; dejar vacío o no definir en AWS real
aws.dynamodb.endpoint=
```

Recomendación:
- En desarrollo contra AWS real, dejar `endpoint` vacío y usar `~/.aws/credentials`.
- En LocalStack/DynamoDB Local, usar `aws.dynamodb.endpoint=http://localhost:4566` o `http://localhost:8000` según la herramienta.

## 3. Configuración Spring `DynamoDbConfig`

Crear [`core-transaccional/src/main/java/com/agencia/viajes/transaccional/config/DynamoDbConfig.java`](core-transaccional/src/main/java/com/agencia/viajes/transaccional/config/DynamoDbConfig.java).

Responsabilidades:
- Crear `DynamoDbClient` con `Region.of(...)`.
- Usar `DefaultCredentialsProvider.create()` para soportar `~/.aws/credentials`, variables de entorno, profiles de AWS CLI e IAM roles.
- Aplicar `endpointOverride` solo si `aws.dynamodb.endpoint` tiene valor.
- Exponer `DynamoDbEnhancedClient`.
- Opcional: habilitar `@EnableAsync` en esta clase o en `CoreTransaccionalApplication` si se usará escritura asíncrona con `@Async`.

Ejemplo orientativo:

```java
@Configuration
@EnableAsync
public class DynamoDbConfig {
    @Bean
    DynamoDbClient dynamoDbClient(
            @Value("${aws.dynamodb.region}") String region,
            @Value("${aws.dynamodb.endpoint:}") String endpoint) {
        var builder = DynamoDbClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create());

        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
    }

    @Bean
    DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient client) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(client)
                .build();
    }
}
```

## 4. Modelo DynamoDB `NavegacionItem`

Crear [`core-transaccional/src/main/java/com/agencia/viajes/transaccional/navegacion/model/NavegacionItem.java`](core-transaccional/src/main/java/com/agencia/viajes/transaccional/navegacion/model/NavegacionItem.java).

Campos recomendados:
- `Integer idUsuario`: partition key, atributo DynamoDB `id_usuario`.
- `String timestamp`: sort key ISO-8601, atributo `timestamp`.
- `String tipoEvento`: por ejemplo `VISUALIZACION_RUTA` o `BUSQUEDA_RUTA`.
- `String origen`, `String destino`, `String fechaBusqueda`.
- `Integer idRuta`: opcional para visualizaciones concretas.
- `Integer resultadosCount`: útil para BI e IA en búsquedas.
- `String canal`: por ejemplo `APP_MOVIL`, `WEB`, `GRAPHQL`.
- `String metadata`: JSON simple opcional si se quiere guardar información flexible sin cambiar el modelo.

Ejemplo mínimo:

```java
@DynamoDbBean
@Data
@NoArgsConstructor
public class NavegacionItem {
    private Integer idUsuario;
    private String timestamp;
    private String tipoEvento;
    private String origen;
    private String destino;
    private String fechaBusqueda;
    private Integer idRuta;
    private Integer resultadosCount;
    private String canal;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("id_usuario")
    public Integer getIdUsuario() {
        return idUsuario;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("timestamp")
    public String getTimestamp() {
        return timestamp;
    }
}
```

## 5. Repositorio Write-Only

Crear [`core-transaccional/src/main/java/com/agencia/viajes/transaccional/navegacion/repository/NavegacionRepository.java`](core-transaccional/src/main/java/com/agencia/viajes/transaccional/navegacion/repository/NavegacionRepository.java).

Responsabilidades:
- Encapsular `DynamoDbEnhancedClient.table(tableName, TableSchema.fromBean(NavegacionItem.class))`.
- Exponer solo `guardar(NavegacionItem item)`.
- No implementar `scan` ni lecturas en esta fase.

## 6. Servicio `NavegacionService`

Crear [`core-transaccional/src/main/java/com/agencia/viajes/transaccional/navegacion/service/NavegacionService.java`](core-transaccional/src/main/java/com/agencia/viajes/transaccional/navegacion/service/NavegacionService.java).

Métodos sugeridos:
- `registrarVisualizacion(Integer idUsuario, Integer idRuta, String origen, String destino, String canal)` para la mutación GraphQL.
- `registrarBusquedaRuta(Integer idUsuario, String origen, String destino, LocalDate fecha, int resultadosCount)` para CU-01.
- Variante asíncrona con `@Async`, por ejemplo `registrarBusquedaRutaAsync(...)`.

Regla clave:
- Capturar `Exception`/`SdkException`, loguear `WARN` y no propagar. La búsqueda y la mutación deben seguir funcionando aunque DynamoDB falle.
- Usar `org.slf4j.Logger`, no `System.err`, aunque el patrón actual de Redis ya muestra la intención de tolerar fallos externos.

## 7. Mutación GraphQL

Actualizar [`core-transaccional/src/main/resources/graphql/schema.graphqls`](core-transaccional/src/main/resources/graphql/schema.graphqls) dentro de `type Mutation`:

```graphql
registrarVisualizacionRuta(
    idUsuario: Int!
    idRuta: Int
    origen: String
    destino: String
    canal: String
): Boolean!
```

Crear [`core-transaccional/src/main/java/com/agencia/viajes/transaccional/navegacion/graphql/NavegacionResolver.java`](core-transaccional/src/main/java/com/agencia/viajes/transaccional/navegacion/graphql/NavegacionResolver.java).

Patrón esperado:
- `@Controller`
- `@RequiredArgsConstructor`
- `@MutationMapping`
- Delegar todo al servicio y devolver `true` si la petición fue aceptada.

Importante:
- Si DynamoDB falla, el servicio debe loguear el problema y la mutación puede devolver `true` como “evento aceptado”. Si se prefiere indicar fallo real de escritura, devolver `false`, pero eso haría visible un fallo que el requisito pide no interrumpir.

## 8. Integración Automática En CU-01

Modificar [`core-transaccional/src/main/resources/graphql/schema.graphqls`](core-transaccional/src/main/resources/graphql/schema.graphqls) para que CU-01 pueda asociar el evento a usuario:

```graphql
buscarRutasYHorariosDisponibles(
    origen: String!
    destino: String!
    fecha: String!
    idUsuario: Int
): [ViajeDisponible!]!
```

Modificar [`core-transaccional/src/main/java/com/agencia/viajes/transaccional/viajes/graphql/ViajeQueryResolver.java`](core-transaccional/src/main/java/com/agencia/viajes/transaccional/viajes/graphql/ViajeQueryResolver.java):

- Añadir `@Argument Integer idUsuario`.
- Pasarlo a `ViajeConsultaService`.
- Mantener compatibilidad con clientes actuales haciendo `idUsuario` opcional.

Modificar [`core-transaccional/src/main/java/com/agencia/viajes/transaccional/viajes/service/ViajeConsultaService.java`](core-transaccional/src/main/java/com/agencia/viajes/transaccional/viajes/service/ViajeConsultaService.java):

- Inyectar `NavegacionService` por constructor.
- Cambiar la firma interna a `buscarRutasYHorariosDisponibles(String origen, String destino, LocalDate fecha, Integer idUsuario)`.
- Guardar el resultado de la consulta en una variable antes de retornar.
- Llamar al tracking después del mapeo.
- No envolver la búsqueda PostgreSQL en dependencias de DynamoDB.

Ejemplo de flujo:

```java
List<ViajeDisponibleResponse> resultados = viajeProgramadoRepository
        .buscarDisponiblesPorRutaYFecha(origen.trim(), destino.trim(), inicioDia, finDia)
        .stream()
        .map(this::mapearRespuesta)
        .toList();

navegacionService.registrarBusquedaRutaAsync(
        idUsuario,
        origen.trim(),
        destino.trim(),
        fecha,
        resultados.size());

return resultados;
```

Si `idUsuario` viene `null`:
- Opción conservadora: no registrar el evento automático porque la tabla exige `id_usuario` como partition key.
- Alternativa: usar un usuario técnico `0` para navegación anónima, solo si BI/IA acepta ese convenio.

## 9. Credenciales Locales Y Producción

Desarrollo local con perfil AWS:

```ini
# ~/.aws/credentials
[default]
aws_access_key_id=...
aws_secret_access_key=...
```

Opcionalmente:

```ini
# ~/.aws/config
[default]
region=us-east-2
```

Variables de entorno soportadas por `DefaultCredentialsProvider`:

```bash
AWS_ACCESS_KEY_ID=...
AWS_SECRET_ACCESS_KEY=...
AWS_REGION=us-east-2
```

Producción:
- No guardar credenciales en `application.properties`.
- Usar IAM role asociado al runtime: ECS task role, EKS IRSA, EC2 instance profile o rol equivalente.
- Permisos mínimos IAM: `dynamodb:PutItem` sobre la tabla `NavegacionViajes`.

## 10. Pruebas Sugeridas

Pruebas unitarias:
- `NavegacionServiceTest`: mock de `NavegacionRepository`, verifica que errores no se propaguen.
- `ViajeConsultaServiceTest`: mock de `NavegacionService`, verifica que CU-01 retorna resultados aunque el tracking lance excepción.

Pruebas de integración opcionales:
- DynamoDB Local o LocalStack con tabla `NavegacionViajes`, `id_usuario` `N` y `timestamp` `S`.
- Ejecutar una mutación `registrarVisualizacionRuta` y validar con AWS CLI/LocalStack que se creó el item.
- Ejecutar `buscarRutasYHorariosDisponibles(..., idUsuario: 1)` y validar que genera un evento `BUSQUEDA_RUTA`.

Ejemplo GraphQL de validación:

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

## 11. Orden De Implementación

1. Añadir dependencias AWS SDK v2 en Gradle.
2. Añadir propiedades `aws.dynamodb.*`.
3. Crear `DynamoDbConfig` con `DefaultCredentialsProvider` y `DynamoDbEnhancedClient`.
4. Crear `NavegacionItem` mapeado a `id_usuario` y `timestamp`.
5. Crear `NavegacionRepository` write-only.
6. Crear `NavegacionService` con manejo de errores tolerante.
7. Añadir mutación GraphQL y `NavegacionResolver`.
8. Extender CU-01 con `idUsuario` opcional y registrar búsquedas desde `ViajeConsultaService`.
9. Probar con credenciales AWS reales o DynamoDB Local/LocalStack.
10. Documentar en `docs/estado core-transaccional/estado.md` la integración de navegación si se decide tratarla como nuevo caso técnico.