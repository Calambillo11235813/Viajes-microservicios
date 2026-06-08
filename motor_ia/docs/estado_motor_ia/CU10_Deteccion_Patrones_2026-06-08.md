# CU10 — Detección de patrones de viaje

**Fecha:** 2026-06-08  
**Estado:** Implementado y probado localmente

---

## Objetivo

Exponer las reglas de asociación precalculadas entre rutas de viaje (algoritmo Apriori) para que el gerente o el core transaccional descubran patrones del tipo: *“quienes visitan rutas A, B y C suelen elegir también la ruta D”*.

---

## Endpoint

| Campo | Valor |
|-------|-------|
| Método | `GET` |
| URL | `http://127.0.0.1:8000/api/reglas-asociacion/` |
| Body | No aplica |

### Respuesta exitosa

```json
{
  "status": "success",
  "total": 85,
  "reglas": [
    {
      "antecedents": [10, 11, 14],
      "consequents": 9,
      "antecedents_nombre": ["Ruta 10", "Ruta 11", "Ruta 14"],
      "consequents_nombre": "Ruta 9",
      "support": 0.103,
      "confidence": 0.6519,
      "lift": 1.23
    }
  ]
}
```

### Errores frecuentes

| Código | Causa |
|--------|-------|
| 404 | Archivo `reglas_asociacion_final.json` no encontrado |
| 405 | Método distinto de GET |

---

## Modelo / artefacto

- **Técnica:** Reglas de asociación (Apriori), entrenamiento offline
- **Artefacto:** `model_files/reglas_asociacion_final.json`
- **Carga:** Singleton en `apps.py` → `Cu10DeteccionPatronesViajeConfig.ready()`
- **Métricas por regla:** `support`, `confidence`, `lift`

---

## Estructura de archivos

```
machinelearning/CU10_Deteccion_de_patrones/
├── apps.py
├── urls.py
├── views.py
└── model_files/
    └── reglas_asociacion_final.json
```

---

## Configuración Django

- `motor_ia/settings.py` — `machinelearning.CU10_Deteccion_de_patrones.apps.Cu10DeteccionPatronesViajeConfig`
- `motor_ia/urls.py` — `api/reglas-asociacion/` → `CU10_Deteccion_de_patrones.urls` (sub-ruta vacía `''`)
- Mensaje de arranque: `[CU10] Reglas de asociación cargadas correctamente desde model_files/ (N reglas)`

---

## Prueba rápida (PowerShell)

```powershell
Invoke-RestMethod -Uri "http://localhost:8000/api/reglas-asociacion/" -Method GET
```

**curl:**

```bash
curl http://localhost:8000/api/reglas-asociacion/
```

---

## Trabajo realizado (2026-06-08)

1. Corregido `INSTALLED_APPS`: el módulo apuntaba a `CU10_Deteccion_patrones_viaje` (inexistente).
2. Creado `apps.py` con carga del JSON y mensaje `[CU10]` en terminal.
3. Alineada ruta del JSON en `views.py` y `apps.py` → `model_files/`.
4. Endpoint raíz bajo el prefijo `api/reglas-asociacion/` (sin duplicar segmento en la URL).

---

## Pendientes / mejoras futuras

- Integración con `core-transaccional` / dashboard BI del gerente.
- Filtros por ruta, lift mínimo o paginación si el JSON crece mucho.
- Endpoint POST para recalcular reglas con datos nuevos (pipeline batch).
