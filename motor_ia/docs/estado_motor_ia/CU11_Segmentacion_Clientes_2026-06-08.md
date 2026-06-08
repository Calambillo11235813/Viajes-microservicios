# CU11 — Segmentación de clientes

**Fecha:** 2026-06-08  
**Estado:** Implementado y probado localmente

---

## Objetivo

Asignar cada usuario a un cluster de comportamiento (K-Means) según su historial de gasto y reservas, y exponer los centroides de cada segmento para dashboards del gerente.

---

## Endpoints

### 1. Segmentar usuario

| Campo | Valor |
|-------|-------|
| Método | `POST` |
| URL | `http://127.0.0.1:8000/api/segmentar-usuario/segmentar-usuario/` |
| Content-Type | `application/json` |

#### Body de ejemplo

```json
{
  "total_gastado": 5000.0,
  "num_reservas": 15,
  "rutas_distintas": 10,
  "promedio_pasajeros": 2.0
}
```

#### Respuesta exitosa

```json
{
  "status": "success",
  "cluster": 2,
  "caracteristicas": {
    "total_gastado": 5000.0,
    "num_reservas": 15,
    "rutas_distintas": 10,
    "promedio_pasajeros": 2.0
  }
}
```

### 2. Estadísticas de clusters

| Campo | Valor |
|-------|-------|
| Método | `GET` |
| URL | `http://127.0.0.1:8000/api/segmentar-usuario/estadisticas-clusters/` |

#### Respuesta exitosa

```json
{
  "status": "success",
  "n_clusters": 3,
  "clusters": [
    {
      "cluster": 0,
      "centroide": {
        "total_gastado": 1200.5,
        "num_reservas": 5,
        "rutas_distintas": 3,
        "promedio_pasajeros": 1.8
      }
    }
  ],
  "mensaje": "Para asignación de usuarios a clusters, use /segmentar-usuario/ con POST"
}
```

### Errores frecuentes

| Código | Causa |
|--------|-------|
| 400 | Falta alguna feature requerida o JSON inválido |
| 503 | Modelos no cargados (`kmeans_model`, `scaler`, `features`) |
| 405 | Método HTTP incorrecto |

---

## Modelo

- **Algoritmo:** K-Means + StandardScaler (scikit-learn)
- **Archivos:** `kmeans_model.pkl`, `kmeans_scaler.pkl`, `kmeans_features.json`
- **Features:** `total_gastado`, `num_reservas`, `rutas_distintas`, `promedio_pasajeros`
- **Carga:** Singleton en `apps.py` → `ApiSegmentacionConfig.ready()`
- Mensaje de arranque: `[CU11] Modelo KMeans, escalador y features cargados correctamente`

---

## Estructura de archivos

```
machinelearning/CU11_Segementacion_clientes/
├── __init__.py
├── apps.py
├── urls.py
├── views.py
├── model_loader.py          # reservado (vacío)
└── model_files/
    ├── kmeans_model.pkl
    ├── kmeans_scaler.pkl
    └── kmeans_features.json
```

> **Nota:** el directorio se llama `Segementacion` (typo histórico); usar ese nombre exacto en imports Django.

---

## Configuración Django

- `motor_ia/settings.py` — `machinelearning.CU11_Segementacion_clientes.apps.ApiSegmentacionConfig`
- `motor_ia/urls.py` — `api/segmentar-usuario/` → `CU11_Segementacion_clientes.urls`

---

## Prueba rápida (PowerShell)

```powershell
# Estadísticas de clusters
Invoke-RestMethod -Uri "http://localhost:8000/api/segmentar-usuario/estadisticas-clusters/" -Method GET

# Segmentar usuario
$body = @{
  total_gastado = 5000.0
  num_reservas = 15
  rutas_distintas = 10
  promedio_pasajeros = 2.0
} | ConvertTo-Json

Invoke-RestMethod `
  -Uri "http://localhost:8000/api/segmentar-usuario/segmentar-usuario/" `
  -Method POST `
  -ContentType "application/json" `
  -Body $body
```

---

## Trabajo realizado (2026-06-08)

1. Registro en `INSTALLED_APPS` (faltaba; `ready()` no se ejecutaba).
2. Corregido `name` en `apps.py` → `machinelearning.CU11_Segementacion_clientes`.
3. Corregida ruta de `model_files/` (antes apuntaba a `../model_files` fuera del CU).
4. Endpoints verificados con `manage.py check` y pruebas manuales.

---

## Pendientes / mejoras futuras

- Integración con `core-transaccional` para calcular features desde BD por `id_usuario`.
- Persistir asignación cluster-usuario para reportes históricos.
- Etiquetas de negocio por cluster (ej. “Viajero frecuente”, “Ocasional”).
