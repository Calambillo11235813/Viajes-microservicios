# CU-09 — Recomendación personalizada de rutas

**Fecha:** 2026-06-07  
**Estado:** Implementado y probado localmente (GraphQL + motor IA)

---

## Objetivo

Exponer una consulta GraphQL que, a partir del historial de un usuario en PostgreSQL, calcule su perfil de gasto y categoría turística preferida, invoque al motor IA (Random Forest en Django) y devuelva la ruta recomendada junto con un top 3 de alternativas.

---

## Consulta GraphQL

| Campo | Valor |
|-------|-------|
| URL | `POST http://localhost:8080/graphql` |
| Query | `obtenerRecomendacionRuta(idUsuario: Int!, presupuesto: Float)` |

### Respuesta exitosa (ejemplo probado)

```json
{
  "data": {
    "obtenerRecomendacionRuta": {
      "rutaRecomendadaId": 6,
      "perfilUsuario": "Premium",
      "categoriaPreferida": "Interdepartamental",
      "advertencia": null,
      "topRutas": [
        { "idRuta": 6, "probabilidad": 0.82 },
        { "idRuta": 1, "probabilidad": 0.135 },
        { "idRuta": 5, "probabilidad": 0.02 }
      ]
    }
  }
}
```

---

## Archivos creados o modificados

| Archivo | Cambio |
|---------|--------|
| `recomendaciones/service/RecomendacionService.java` | Lógica de percentiles, categoría preferida, llamada HTTP y fallback |
| `recomendaciones/graphql/RecomendacionResolver.java` | Query GraphQL `@QueryMapping` |
| `recomendaciones/config/MotorIaClientConfig.java` | Bean `RestClient` (timeout 3 s, `Connection: close`) y bean `ObjectMapper` |
| `recomendaciones/dto/RecomendacionRutaResponse.java` | DTO de respuesta GraphQL |
| `recomendaciones/dto/TopRuta.java` | DTO del top de rutas |
| `recomendaciones/dto/MotorIaRequest.java` | Contrato JSON hacia Django (snake_case) |
| `recomendaciones/dto/MotorIaResponse.java` | Deserialización de respuesta del motor IA |
| `reservas/repository/ReservaRepository.java` | Consultas JPQL: gasto por usuario, percentiles globales, categoría más frecuente |
| `resources/graphql/schema.graphqls` | Tipos `RecomendacionRutaResponse`, `TopRuta` y query `obtenerRecomendacionRuta` |
| `resources/application.properties` | `motor-ia.base-url` y `motor-ia.recomendacion-path` |

---

## Integración con motor IA

| Campo | Valor |
|-------|-------|
| Método | `POST` |
| URL | `http://localhost:8000/api/recomendar-ruta/api/v1/recomendar-ruta/` |
| Body | JSON con `perfil_pasajero`, `categoria_preferida`, `monto_total_pagado`, `cantidad_pasajeros` |
| Timeout | 3 segundos |
| Fallback | `rutaRecomendadaId = 1` + campo `advertencia` si el motor IA no responde |

---

## Errores encontrados y soluciones aplicadas

| Fecha | Error | Causa | Solución |
|-------|-------|-------|----------|
| 2026-06-07 | `ECONNREFUSED 127.0.0.1:8080` | El core-transaccional no estaba levantado | Ejecutar `.\gradlew.bat bootRun` en el puerto 8080 antes de probar GraphQL |
| 2026-06-07 | `404 Not Found` en `/api/v1/recomendar-ruta/` | URL incompleta; Django monta la ruta en dos niveles (`api/recomendar-ruta/` + `api/v1/recomendar-ruta/`) | Configurar la URL completa en `motor-ia.recomendacion-path` |
| 2026-06-07 | `"Faltan campos: perfil_pasajero y/o categoria_preferida"` | Prueba directa al motor IA sin body JSON correcto, o envío en camelCase | GraphQL va a `:8080/graphql`; hacia Django se envía JSON en snake_case con `ObjectMapper` |
| 2026-06-07 | `Formato JSON inválido` y `Bad request syntax ('7a')` | Conflicto HTTP keep-alive / chunked entre `RestClient` y el servidor de desarrollo de Django | Cabecera `Connection: close` y serialización explícita del body como `String` JSON |
| 2026-06-07 | `No qualifying bean of type ObjectMapper` al arrancar | Spring Boot 4 con `webmvc` no registra `ObjectMapper` automáticamente | Bean `@Bean ObjectMapper objectMapper()` en `MotorIaClientConfig` |
| 2026-06-07 | Fallback con `advertencia` pese a motor IA activo | Combinación de los errores anteriores en la cadena HTTP | Tras aplicar las correcciones anteriores, Django respondió `200` y GraphQL devolvió predicción real |

---

## Notas

- Las reservas con estado `CANCELADA` se excluyen del cálculo de gasto y categoría preferida.
- Si el usuario no tiene historial, el perfil por defecto es `Económico` y la categoría `Desconocido` (puede provocar error 400 en el motor IA si esa categoría no está en el encoder).
- `cantidad_pasajeros` se envía fijo en `1` por simplicidad en esta versión.
