# CU09 — Recomendación personalizada de destinos

**Fecha:** 2026-06-08  
**Estado:** Implementado y probado localmente

---

## Objetivo

Recibir el perfil del pasajero, su categoría turística preferida, monto pagado y cantidad de pasajeros; predecir la ruta más adecuada con un modelo Random Forest y devolver el top 3 con probabilidades.

---

## Endpoint

| Campo | Valor |
|-------|-------|
| Método | `POST` |
| URL | `http://127.0.0.1:8000/api/recomendar-ruta/api/v1/recomendar-ruta/` |
| Content-Type | `application/json` |

### Body de ejemplo

```json
{
  "perfil_pasajero": "Premium",
  "categoria_preferida": "Interdepartamental",
  "monto_total_pagado": 350.0,
  "cantidad_pasajeros": 2
}
```

### Respuesta exitosa

```json
{
  "status": "success",
  "modulo": "CU-09 Recomendacion Personalizada",
  "datos_recibidos": { "...": "..." },
  "prediccion_id_ruta": 10,
  "top_3_rutas": [10, 3, 5],
  "top_3_probabilidades": [0.42, 0.31, 0.18]
}
```

### Errores frecuentes

| Código | Causa |
|--------|-------|
| 400 | Faltan `perfil_pasajero` / `categoria_preferida` o valor no reconocido por el encoder |
| 503 | Modelos no cargados en memoria |
| 405 | Método distinto de POST |

---

## Modelo

- **Algoritmo:** RandomForestClassifier (scikit-learn)
- **Archivos:** `modelo_recomendador_rutas.pkl`, `le_perfil.pkl`, `le_categoria.pkl`
- **Features (orden):** `perfil_num`, `categoria_num`, `monto_total_pagado`, `cantidad_pasajeros`
- **Carga:** Singleton en `apps.py` → `Cu09RecomendacionPersonalizadaConfig.ready()`
- **Documentación técnica:** `machinelearning/CU09_Recomendacion_personalizada/docs/DOCUMENTACION_MODELOS_IA.md`

---

## Estructura de archivos

```
machinelearning/CU09_Recomendacion_personalizada/
├── apps.py
├── urls.py
├── views.py
├── docs/
│   └── DOCUMENTACION_MODELOS_IA.md
└── model_files/
    ├── modelo_recomendador_rutas.pkl
    ├── le_perfil.pkl
    └── le_categoria.pkl
```

---

## Configuración Django

- `motor_ia/settings.py` — `machinelearning.CU09_Recomendacion_personalizada.apps.Cu09RecomendacionPersonalizadaConfig`
- `motor_ia/urls.py` — `api/recomendar-ruta/` → `CU09_Recomendacion_personalizada.urls`
- Mensaje de arranque: `[CU09] Modelo Random Forest y encoders cargados correctamente desde model_files/`

---

## Integración con Microservicio A

| Componente | Detalle |
|------------|---------|
| Cliente | `core-transaccional` → `RecomendacionService` |
| URL configurada | `motor-ia.recomendacion-path=/api/recomendar-ruta/api/v1/recomendar-ruta/` |
| GraphQL | `obtenerRecomendacionRuta(idUsuario, presupuesto)` |

---

## Prueba rápida (PowerShell)

```powershell
$body = @{
  perfil_pasajero = "Premium"
  categoria_preferida = "Interdepartamental"
  monto_total_pagado = 350.0
  cantidad_pasajeros = 2
} | ConvertTo-Json

Invoke-RestMethod `
  -Uri "http://localhost:8000/api/recomendar-ruta/api/v1/recomendar-ruta/" `
  -Method POST `
  -ContentType "application/json" `
  -Body $body
```

---

## Trabajo realizado (2026-06-08)

1. App registrada en `INSTALLED_APPS` con `name` alineado al paquete real.
2. Carga de modelos en `ready()` con mensaje `[CU09]` en terminal.
3. Endpoint validado con `manage.py check` y pruebas manuales.
4. Integración documentada con `core-transaccional`.

---

## Pendientes / mejoras futuras

- Exponer catálogo de perfiles/categorías válidos en un endpoint de metadatos.
- Sincronizar contrato de respuesta con el DTO Java (`prediccion_id_ruta` vs `rutaRecomendadaId`).
