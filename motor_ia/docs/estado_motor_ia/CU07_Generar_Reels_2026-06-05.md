# CU07 — Generar Videos Turísticos Automáticamente (Reels)

**Fecha:** 2026-06-05  
**Estado:** Implementado y probado localmente

---

## Objetivo

Recibir un video largo y una pista de audio del usuario, analizar cada segmento
del video con una CNN (MobileNetV2) para identificar los fragmentos más
visualmente interesantes, y generar un reel corto (15/30/45/60 segundos)
con los mejores momentos ordenados cronológicamente y musicalizado.

---

## Endpoint

| Campo | Valor |
|-------|-------|
| Método | `POST` |
| URL | `http://127.0.0.1:8000/api/generar-reel/` |
| Body | `multipart/form-data`, campos `video` (File) y `audio` (File) |
| Parámetros opcionales | `duracion_reel` (int: 15, 30, 45 o 60), `duracion_clip` (int, default 10) |
| Límite de upload | 150 MB |

### Respuesta exitosa

```json
{
  "exito": true,
  "mensaje": "Reel generado exitosamente.",
  "duracion_reel": 60.0,
  "clips_seleccionados": 12,
  "clips_analizados": 64,
  "fragmentos": [
    {"inicio": 70.0, "fin": 75.0, "score": 21.92},
    {"inicio": 90.0, "fin": 95.0, "score": 24.71}
  ],
  "archivo_descarga": "/media/reels/reel_20260605_141530.mp4"
}
```

### Error — video demasiado corto

```json
{
  "error": "El video (25.0s) es más corto que la duración solicitada para el reel (60s)."
}
```

---

## Modelo e Inteligencia Artificial

- **Arquitectura:** MobileNetV2 (transfer learning, sin capa top, pooling promedio)
- **Pesos:** Pre-entrenados en ImageNet (se descargan automáticamente)
- **Carga:** Singleton en `model_loader.py`, precargado al arrancar Django vía `apps.py`

### Algoritmo de puntuación de fotogramas

```
score = (riqueza_visual × 0.7) + (nitidez_normalizada × 0.3)
```

| Componente | Técnica | Peso |
|---|---|---|
| Riqueza visual | Magnitud L2 del vector de 1280 features de MobileNetV2 | 70% |
| Nitidez | Varianza del Laplaciano (OpenCV), acotada a máx. 500 | 30% |

### Pipeline de procesamiento

1. Dividir video en bloques de `duracion_clip` segundos.
2. Extraer fotograma central de cada bloque.
3. Puntuar cada fotograma con el algoritmo de IA.
4. Seleccionar los N bloques con mayor puntuación (N = duracion_reel / duracion_clip).
5. Reordenar cronológicamente para coherencia narrativa.
6. Concatenar clips, aplicar pista musical y renderizar a MP4 (H.264/AAC).

---

## Estructura de archivos

```
deeplearning/CU07_generar_reels/
└── api_reels/
    ├── __init__.py
    ├── apps.py            ← AppConfig con precarga del modelo
    ├── model_loader.py    ← Singleton ReelScorer (MobileNetV2)
    ├── services.py        ← Lógica de negocio (pipeline completo)
    ├── urls.py            ← Ruta interna
    └── views.py           ← Vista REST (validación + invocación del servicio)
```

---

## Configuración Django

- `motor_ia/settings.py`:
  - App registrada como `api_reels.apps.ApiReelsConfig`
  - Ruta `CU07_generar_reels` en `sys.path`
  - `MEDIA_ROOT` y `MEDIA_URL` configurados para almacenar reels
  - Límite de upload: 150 MB (`DATA_UPLOAD_MAX_MEMORY_SIZE`, `FILE_UPLOAD_MAX_MEMORY_SIZE`)
- `motor_ia/urls.py`:
  - Ruta `api/generar-reel/` → `api_reels.urls`
  - Servicio de archivos media en modo DEBUG

---

## Trabajo realizado (2026-06-05)

1. **Desarrollo en Colab:** Dos versiones funcionales (mecánica e inteligente con IA).
2. **Integración Django:** Creada app `api_reels` siguiendo el patrón de CU06.
3. **Separación de capas:** Lógica de IA en `services.py` / `model_loader.py`, capa web solo valida y retorna JSON.
4. **Configuración:** Registrada en settings, URLs, media y límites de upload.
5. **Dependencias:** `moviepy`, `opencv-python` y `ffmpeg` instalados.

---

## Librerías utilizadas

- Django 5.2, Django REST Framework
- TensorFlow / Keras (MobileNetV2)
- OpenCV (`cv2.Laplacian` para nitidez)
- MoviePy (corte, concatenación, musicalización, renderizado)
- NumPy
- FFmpeg (dependencia del sistema para MoviePy)

---

## Pendientes / mejoras futuras

- Agregar transiciones entre clips (fade-in/fade-out, crossfade)
- Soporte para múltiples resoluciones de salida (720p, 1080p)
- Cola de procesamiento asíncrono (Celery) para videos pesados
- Integración con Microservicio A (app móvil / backend principal)

---

## Optimizaciones de rendimiento (2026-06-09)

| Mejora | Archivo | Efecto |
|--------|---------|--------|
| Análisis en proxy 480p | `services.py` | Decodificación más rápida al puntuar fotogramas |
| Inferencia batch MobileNetV2 | `model_loader.py` | Una sola llamada `predict` por video |
| `duracion_clip` default 10s | `views.py` | Mitad de bloques a analizar vs 5s |
| Salida 720p + bitrate 800k | `services.py` | Render MoviePy más rápido |
| Sin `Content-Type` manual | `GenerarReelScreen.tsx` | Upload multipart correcto en React Native |
| Tiempos por fase | `services.py`, `views.py` | Campo `tiempos_procesamiento` en respuesta JSON |

**Referencia de prueba:** video fixture 90s, reel 30s → ~48s total en CPU local (test unitario).
