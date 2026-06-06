# Solución: Latencia del Traductor Visual (CU-08) — 05 de Junio de 2026

Documento de referencia con el diagnóstico, las causas de lentitud y las optimizaciones aplicadas al flujo de **Traducción Visual** entre la app móvil (`TraducirVisualScreen.tsx`) y el Motor IA (`api_traductor`).

**Relacionado con:**
- [Solucion_Analisis_Imagen_Motor_IA_2026-06-05.md](./Solucion_Analisis_Imagen_Motor_IA_2026-06-05.md) — conectividad y firewall del puerto 8000
- [errores-2026-06-05.md](./errores-2026-06-05.md) — registro general de errores del día

---

## 1. Contexto

| Componente | Tecnología | Endpoint |
|---|---|---|
| App móvil | React Native + Expo + `expo-camera` | `CONFIG.AI_TRANSLATE_URL` |
| Motor IA (CU-08) | Django + EasyOCR + GoogleTranslator | `POST /api/traducir-imagen/` |

**Flujo CU-08:**

1. El usuario enfoca texto con la cámara y presiona **Traducir**.
2. La app captura una foto (`takePictureAsync`) y envía `FormData` con:
   - `imagen` (archivo JPEG)
   - `idioma_origen` (ej: `en`)
   - `idioma_destino` (ej: `es`)
3. Django guarda la imagen temporalmente, ejecuta OCR (EasyOCR) y traduce cada texto detectado (Google Translate).
4. La app muestra las traducciones y las lee con `expo-speech`.

**Configuración en la app** (`config.ts`):

```typescript
AI_TRANSLATE_URL: `http://${LOCAL_IP}:8000/api/traducir-imagen/`,
```

---

## 2. Síntoma reportado

La app **sí enviaba** la imagen correctamente, pero Django tardaba mucho en responder. En los logs se observó:

**App móvil (Expo):**
```
LOG  Iniciando captura de foto...
LOG  Foto capturada: file:///data/user/0/host.exp.exponent/cache/Camera/....jpg
LOG  Enviando fetch a Django...
```

**Django (tras varios segundos):**
```
[CU-08] Cargando modelo OCR para idioma: Espanol...
[CU-08] Modelo OCR para Espanol cargado.
[05/Jun/2026 23:35:59] "POST /api/traducir-imagen/ HTTP/1.1" 200 5039
```

**App móvil (respuesta final):**
```
LOG  Respuesta recibida. Status: 200
LOG  Traducciones recibidas: 31
```

**Conclusión:** no era un error de red ni de conexión. La petición llegaba, se procesaba y respondía con HTTP 200, pero con **latencia alta** perceptible en el celular.

---

## 3. Pipeline original (antes de optimizar)

```
Imagen del celular
       │
       ▼
┌──────────────────┐
│ Guardar temporal │  views.py
└────────┬─────────┘
         ▼
┌──────────────────┐
│ Cargar EasyOCR   │  ← Solo en 1ª petición por idioma (lazy load)
│ (CRAFT + CRNN)   │     Varias segundos de espera
└────────┬─────────┘
         ▼
┌──────────────────┐
│ OCR a resolución │  ← Foto de cámara a ~0.8 quality, varios MP
│ completa en CPU  │     Inferencia lenta sin GPU
└────────┬─────────┘
         ▼
┌──────────────────┐
│ Filtrar > 50%    │
│ confianza        │
└────────┬─────────┘
         ▼
┌──────────────────┐
│ Traducir 1 a 1   │  ← 31 detecciones = 31 llamadas HTTP
│ con Google       │     secuenciales a Google Translate
└────────┬─────────┘
         ▼
   JSON con detecciones
```

---

## 4. Causas identificadas

### Causa A — Carga lazy del modelo OCR (solo en la primera petición por idioma)

En `apps.py` original, los readers de EasyOCR **no se precargaban** al arrancar Django. El mensaje `[CU-08] Cargando modelo OCR...` aparecía **durante** la primera petición de cada idioma, añadiendo varios segundos de espera.

EasyOCR usa internamente:
- **CRAFT** — detección de regiones de texto
- **CRNN** — reconocimiento de caracteres

Ambas redes se cargan en memoria la primera vez que se usa un idioma.

### Causa B — Traducciones secuenciales a Google (impacto en cada petición)

En `services.py` original, el bucle traducía cada detección **una por una**:

```python
for (caja, texto, confianza) in resultados_ocr:
    traduccion = traductor.translate(texto)  # llamada de red bloqueante
```

Con **31 detecciones** (caso real observado), eso implicaba **31 viajes de red secuenciales** a los servidores de Google Translate. Esta era la causa más grave y **recurrente** en cada traducción.

### Causa C — OCR sobre imagen a resolución completa en CPU

La captura de cámara usa `quality: 0.8` y EasyOCR corría con `gpu=False`. Procesar una imagen de varios megapíxeles en CPU sin redimensionar previo aumenta el tiempo de inferencia OCR.

---

## 5. Optimizaciones aplicadas

### 5.1 Traducción en paralelo (`services.py`)

Se reemplazó el bucle secuencial por un `ThreadPoolExecutor` con hasta **8 workers** concurrentes. Las llamadas a `GoogleTranslator` son I/O-bound (red), por lo que paralelizarlas reduce drásticamente la latencia total.

**Antes:** 31 traducciones × ~200–500 ms cada una ≈ 6–15 segundos solo en traducción.

**Después:** ~4 lotes paralelos (31 ÷ 8) ≈ 1–3 segundos en traducción.

Cambios clave:
- Nueva constante `MAX_WORKERS_TRADUCCION = 8`
- Nueva función `_traducir_seguro()` para capturar errores de red sin romper el lote
- Se mantiene el **orden** de las detecciones en la respuesta

### 5.2 Precarga de modelos OCR al arrancar (`apps.py` + `model_loader.py`)

Se añadió el método `lector_ocr.precargar('es', 'en')` en `ApiTraductorConfig.ready()`.

Al arrancar Django (con autoreloader), ahora se ven estos mensajes **antes** de la primera petición:

```
[CU-08] Cargando modelo OCR para idioma: Espanol...
[CU-08] Modelo OCR para Espanol cargado.
[CU-08] Cargando modelo OCR para idioma: Ingles...
[CU-08] Modelo OCR para Ingles cargado.
```

**Guarda `RUN_MAIN`:** evita cargar los modelos dos veces cuando Django usa el autoreloader de desarrollo. Solo el proceso que atiende peticiones (`RUN_MAIN == 'true'`) precarga.

**Trade-off:** el arranque de Django tarda más, pero la primera traducción del usuario ya no paga ese costo.

### 5.3 Redimensionado de imagen antes del OCR (`model_loader.py`)

En `detectar_texto()`, si el lado mayor de la imagen supera **1600 px**, se reduce con OpenCV (`INTER_AREA`) antes de pasarla a EasyOCR.

Las coordenadas de los bounding boxes se **reescalan** al tamaño original de la imagen, de modo que el contrato de la API no cambia:

```json
{
  "coordenadas": {
    "top_left": [x, y],
    "bottom_right": [x, y]
  }
}
```

---

## 6. Archivos modificados

| Archivo | Cambio |
|---|---|
| `motor_ia/deeplearning/CU08_traductor_visual/api_traductor/services.py` | Traducción paralela con `ThreadPoolExecutor` |
| `motor_ia/deeplearning/CU08_traductor_visual/api_traductor/model_loader.py` | Método `precargar()`, resize a 1600px, reescalado de coordenadas |
| `motor_ia/deeplearning/CU08_traductor_visual/api_traductor/apps.py` | Precarga de OCR para `es` y `en` al arrancar |

**Sin cambios en el frontend** en esta optimización (la app ya funcionaba; el cuello de botella estaba en el backend).

---

## 7. Contrato de la API (referencia)

**Request:** `POST /api/traducir-imagen/` (multipart/form-data)

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `imagen` | File | Sí | JPEG, PNG, BMP, WebP, TIFF, GIF |
| `idioma_origen` | string | No (default `es`) | `es`, `en`, `it`, `fr`, `de` |
| `idioma_destino` | string | No (default `en`) | `es`, `en`, `it`, `fr`, `de` |

**Response exitosa (200):**

```json
{
  "exito": true,
  "mensaje": "Texto detectado y traducido exitosamente.",
  "idioma_origen": "en",
  "idioma_destino": "es",
  "total_detecciones": 31,
  "detecciones": [
    {
      "texto_original": "Hello",
      "traduccion": "Hola",
      "confianza": 0.92,
      "coordenadas": {
        "top_left": [10, 20],
        "bottom_right": [80, 45]
      }
    }
  ]
}
```

**Sin texto detectado (422):**

```json
{
  "exito": false,
  "mensaje": "No se detecto texto en la imagen con suficiente confianza.",
  "total_detecciones": 0,
  "detecciones": []
}
```

---

## 8. Checklist de verificación

- [ ] Reiniciar Django: `python manage.py runserver 0.0.0.0:8000`
- [ ] Ver precarga OCR al arrancar (mensajes `[CU-08]` para Español e Inglés)
- [ ] Abrir **Traducir Visual** en el celular
- [ ] Enfocar texto legible y presionar **Traducir**
- [ ] Confirmar en logs de Expo: `Respuesta recibida. Status: 200`
- [ ] Confirmar en logs de Django: `"POST /api/traducir-imagen/ HTTP/1.1" 200`
- [ ] La espera debe ser notablemente menor que antes (especialmente con muchas detecciones)

### Tiempos esperados (orientativos, CPU sin GPU)

| Etapa | Antes | Después (estimado) |
|---|---|---|
| Carga modelo OCR (1ª vez) | Durante la petición (+5–15 s) | Al arrancar Django (0 s en petición) |
| OCR en imagen grande | 3–8 s | 1–4 s (con resize) |
| Traducción de 31 textos | 6–15 s (secuencial) | 1–3 s (paralelo, 8 workers) |
| **Total percibido** | **15–30+ s** | **3–8 s** |

> Los tiempos exactos dependen de la imagen, la cantidad de texto y la latencia a Google Translate.

---

## 9. Recomendaciones pendientes (no aplicadas)

### Frontend — quitar `Content-Type` manual

En `TraducirVisualScreen.tsx` (línea ~123) aún se envía:

```typescript
headers: {
  'Accept': 'application/json',
  'Content-Type': 'multipart/form-data',  // ← evitar
},
```

En este flujo funcionó (HTTP 200), pero es el mismo patrón que causó `Network request failed` en `BuscarImagenScreen`. Conviene eliminarlo para que React Native genere el `boundary` automáticamente.

### Frontend — reducir calidad de captura

`takePictureAsync({ quality: 0.8 })` genera imágenes pesadas. Bajar a `0.5` reduciría el tiempo de subida por Wi-Fi, aunque el resize en servidor ya mitiga el OCR.

### Backend — precargar más idiomas

Actualmente se precargan `es` y `en`. Si el usuario usa italiano, francés o alemán, la **primera** petición de ese idioma seguirá cargando el modelo OCR bajo demanda. Se puede ampliar:

```python
lector_ocr.precargar('es', 'en', 'it', 'fr', 'de')
```

A costa de un arranque aún más lento.

### Producción — GPU

EasyOCR corre con `gpu=False`. En GCP con GPU (T4, etc.) el OCR sería significativamente más rápido. Ver [NOTAS_DESPLIEGUE_DOCKER.md](../../../motor_ia/docs/estado_motor_ia/NOTAS_DESPLIEGUE_DOCKER.md).

---

## 10. Resumen ejecutivo

El traductor visual **no fallaba en red**: la app enviaba la imagen, Django respondía 200 con decenas de traducciones. El problema era **latencia del pipeline de IA**:

1. **Carga lazy de EasyOCR** en la primera petición por idioma.
2. **31 traducciones secuenciales** a Google Translate por imagen.
3. **OCR en CPU** sobre fotos de alta resolución.

Las optimizaciones mueven la carga del modelo al arranzo, paralelizan las traducciones y reducen la imagen antes del OCR, manteniendo el contrato de la API intacto.
